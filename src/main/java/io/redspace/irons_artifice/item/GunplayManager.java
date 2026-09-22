package io.redspace.irons_artifice.item;

import software.bernie.geckolib.animatable.GeoItem;
import io.redspace.irons_artifice.api.AmmoEvent;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.api.GunAboutToShootEvent;
import io.redspace.irons_artifice.api.GunAnimations;
import io.redspace.irons_artifice.api.GunShootEvent;
import io.redspace.irons_artifice.advancement.ShotRecord;
import io.redspace.irons_artifice.client.ClientHelper;
import io.redspace.irons_artifice.data.MuzzleFlashSettings;
import io.redspace.irons_artifice.data.MuzzleFlashType;
import io.redspace.irons_artifice.data.RecoilState;
import io.redspace.irons_artifice.data.ReloadResult;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.GunProfile;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.menu.GunContainer;
import io.redspace.irons_artifice.modifier.ModifierItem;
import io.redspace.irons_artifice.network.packets.ClientboundCancelGunAnimationPacket;
import io.redspace.irons_artifice.network.packets.ClientboundGunAnimationPacket;
import io.redspace.irons_artifice.network.packets.ClientboundMuzzleFlashPacket;
import io.redspace.irons_artifice.network.packets.MuzzleFlashVisuals;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.utils.IronsArtificeTags;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import javax.annotation.Nullable;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

public final class GunplayManager {

    public static final int EARLY_SHOT_TOLERANCE_TICKS = 1;

    public static FireOutcome tryFire(LivingEntity shooter, Vec3 direction) {
        if (!shooter.isAlive() || shooter.isSpectator()) {
            return FireOutcome.INVALID_SHOOTER;
        }
        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack stack = shooter.getItemInHand(hand);
        if (!(stack.getItem() instanceof GunItem gunItem)) {
            return FireOutcome.NO_GUN;
        }
        if (FireDelayState.isActive(shooter, stack)) {
            return FireOutcome.FIRE_DELAY_ACTIVE;
        }
        if (GunItem.isReloading(stack)) {
            return FireOutcome.RELOADING;
        }
        MagazineContents magazine = GunItem.getMagazine(stack);
        GunProfile gunProfile = gunItem.getGun();
        ShotProfile profile = compose(shooter, gunProfile, stack);

        int ammoToConsume = NeoForge.EVENT_BUS.post(new AmmoEvent.Amount(shooter, profile, 1)).getAmmoToConsume();
        if (magazine.count() < ammoToConsume) {
            if (shooter instanceof Player player && player.level().isClientSide()) {
                ClientHelper.handleLocalDryFire(player, profile.peek(ShotComponents.GUNSHOT_SOUND).getDryFireSound());
            }
            return FireOutcome.EMPTY_MAGAZINE;
        }
        if (NeoForge.EVENT_BUS.post(new GunAboutToShootEvent(shooter, profile)).isCanceled()) {
            return FireOutcome.EVENT_CANCELLED;
        }
        beginFireDelay(shooter, stack, profile.fireDelayTicks(), pitchMultiplierForFire(profile));
        if (!(shooter.level() instanceof ServerLevel level)) {
            return FireOutcome.FIRED;
        }

        long now = level.getGameTime();

        // order is important since these calls mutate state that affect gun performance
        //  - calculate direction before adding recoil
        //  - update recoil
        //  - fire shot from fixed direction
        //  - then apply character motion
        RecoilState offset = RecoilState.current(shooter, now);
        Vec2 rotation = direction.rotation();
        float pitch = rotation.x - offset.pitch();
        float yaw = rotation.y + offset.yaw();
        depleteMagazine(shooter, profile, stack, magazine, ammoToConsume);
        profile.peek(ShotComponents.GUNSHOT_SOUND).playGunShotSound(level, shooter.position());
        RecoilState.addImpulse(shooter, now, profile);
        fireShot(level, shooter, shooter.getEyePosition(), Vec3.directionFromRotation(pitch, yaw), profile);
        applyCharacterBlowback(shooter, profile);
        playFireAnimation(shooter, stack, gunItem, profile);
        if (hand == InteractionHand.MAIN_HAND && shooter.isUsingItem() && shooter.getUseItem() != stack && GunItem.isOffhandItemUseBlocked(shooter)) {
            shooter.stopUsingItem();
        }
        return FireOutcome.FIRED;
    }

    /**
     * Holds a shot that missed the gate by no more than {@link #EARLY_SHOT_TOLERANCE_TICKS}.
     *
     * @return whether the shot was held rather than discarded
     */
    public static boolean queueEarlyShot(LivingEntity shooter, Vec3 direction) {
        int remaining = FireDelayState.remaining(shooter);
        if (remaining <= 0 || remaining > EARLY_SHOT_TOLERANCE_TICKS) {
            return false;
        }
        PendingShot.set(shooter, new PendingShot(direction, shooter.level().getGameTime()));
        return true;
    }

    /**
     * Fires a held shot, if there is a valid one
     */
    public static void flushPendingShot(LivingEntity shooter) {
        PendingShot pending = PendingShot.get(shooter);
        if (pending.isEmpty()) {
            return;
        }
        PendingShot.clear(shooter);
        if (pending.hasExpired(shooter.level().getGameTime())) {
            return;
        }
        tryFire(shooter, pending.direction());
    }

    private static void depleteMagazine(LivingEntity shooter, ShotProfile profile, ItemStack stack, MagazineContents magazine, int ammoToConsume) {
        if (ammoToConsume <= 0) {
            return;
        }
        var event = new AmmoEvent.Consume(shooter, profile, ammoToConsume);
        if (!NeoForge.EVENT_BUS.post(event).isCanceled()) {
            GunItem.setMagazine(stack, magazine.deplete(event.getAmmoToConsume()));
        }
    }

    public static boolean debugFire(ServerLevel level, ServerPlayer player, Vec3 origin, Vec3 direction) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem gunItem)) {
            return false;
        }

        GunProfile gunProfile = gunItem.getGun();
        ShotProfile profile = compose(player, gunProfile, stack);

        fireShot(level, player, origin, direction, profile);
        profile.peek(ShotComponents.GUNSHOT_SOUND).playGunShotSound(level, origin);
        playFireAnimation(player, stack, gunItem, profile);
        return true;
    }

    private static float pitchMultiplierForFire(ShotProfile profile) {
        return (float) ((profile.value(ShotComponents.FIRE_RATE) + 2) / 3);
    }

    private static void beginFireDelay(LivingEntity shooter, ItemStack stack, int ticks, float pitchMultiplier) {
        FireDelayState.start(shooter, stack, ticks, pitchMultiplier);
    }

    private static void applyCharacterBlowback(LivingEntity living, ShotProfile profile) {
        float strength = (float) profile.value(ShotComponents.CHARACTER_BLOWBACK);
        if (strength <= 0.0F) {
            return;
        }
        Vec3 look = living.getForward();
        // todo: factor in recoil to push direction (looking down to counter recoil makes blast push us up)
        living.push(-look.x * strength, -look.y * strength * 0.5 + 0.05, -look.z * strength);
        if (living instanceof Player) {
            double fallDistanceMultiplier = Utils.mapClamped(living.getDeltaMovement().y, -0.5, -0.1, 1, 0);
            living.fallDistance *= fallDistanceMultiplier;
        }
        living.hurtMarked = true;
    }

    private static void playFireAnimation(LivingEntity living, ItemStack stack, GunItem gunItem, ShotProfile profile) {
        double fireSpeedMultiplier = profile.peek(ShotComponents.FIRE_DELAY).base() / profile.fireDelayTicks();
        ClientboundGunAnimationPacket packet = new ClientboundGunAnimationPacket(living.getId(), GeoItem.getOrAssignId(stack, (ServerLevel) living.level()), stack == living.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                GunAnimations.FIRE, (fireSpeedMultiplier + 1) / 2, 0);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(living, packet);
    }

    public static void playReloadAnimation(LivingEntity living, ItemStack stack) {
        ReloadState state = ReloadState.get(stack);
        if (state == null) {
            return;
        }
        ClientboundGunAnimationPacket packet = new ClientboundGunAnimationPacket(living.getId(), GeoItem.getOrAssignId(stack, (ServerLevel) living.level()), stack == living.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                GunAnimations.RELOAD, state.speed(), state.progress(), state.skipAt(), state.skipTo());
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(living, packet);
    }

    public static void cancelGunAnimation(LivingEntity living, ItemStack stack) {
        if (!(living.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        InteractionHand hand = stack == living.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ClientboundCancelGunAnimationPacket packet = new ClientboundCancelGunAnimationPacket(
                living.getId(),
                GeoItem.getOrAssignId(stack, serverLevel),
                hand
        );
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(living, packet);
    }

    private static void fireShot(ServerLevel level, LivingEntity shooter, Vec3 origin, Vec3 direction, ShotProfile profile) {
        var event = NeoForge.EVENT_BUS.post(new GunShootEvent.Pre(shooter, profile, origin, direction));
        origin = event.getOrigin();
        direction = event.getDirection();
        UUID fireId = UUID.randomUUID();
        boolean fullMagazine = profile.magazineContents().count() == profile.gun().magazineCapacity();
        int projectileCount = Math.max(1, (int) Math.round(profile.value(ShotComponents.PROJECTILE_COUNT)));
        float speed = (float) profile.value(ShotComponents.BULLET_SPEED);
        float spread = getSpreadForEntity(profile, shooter);
        for (int i = 0; i < projectileCount; i++) {
            Bullet bullet = new Bullet(EntityRegistry.BULLET.get(), level);
            bullet.setOwner(shooter);
            bullet.applyProfile(profile.copy());
            bullet.setShotRecord(ShotRecord.of(fireId, fullMagazine));
            bullet.setPos(origin);
            bullet.shoot(direction.x, direction.y, direction.z, speed, spread);
            level.addFreshEntity(bullet);
        }
        spawnMuzzleFlash(level, shooter, direction, profile);
        NeoForge.EVENT_BUS.post(new GunShootEvent.Post(shooter, profile));
    }

    private static void spawnMuzzleFlash(ServerLevel level, LivingEntity shooter, Vec3 direction, ShotProfile profile) {
        MuzzleFlashSettings settings = profile.peek(ShotComponents.MUZZLE_FLASH);
        if (!settings.hasVisuals()) {
            return;
        }
        Optional<ParticleOptions> flash = Optional.empty();
        if (!settings.types().isEmpty()) {
            MuzzleFlashType type = settings.pick(level.getRandom());
            flash = Optional.of(type.particle(settings.pickTint(level.getRandom())));
        }
        float muzzleOffset = (float) profile.value(ShotComponents.MUZZLE_OFFSET);
        float offsetDirection = shooter.getMainArm() == HumanoidArm.LEFT ? -1.0F : 1.0F;
        Vec3 backupPos = shooter.getEyePosition()
                .add(direction.normalize().scale(1.25 + muzzleOffset))
                .add(shooter.getForward().cross(new Vec3(0, 1, 0)).scale(0.5 * offsetDirection));
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(shooter, new ClientboundMuzzleFlashPacket(
                new MuzzleFlashVisuals(flash, List.copyOf(settings.airBursts()), List.copyOf(settings.underwaterBursts())),
                shooter.getId(),
                shooter.getDeltaMovement(),
                muzzleOffset,
                backupPos
        ));
    }

    public static float getSpreadForEntity(ShotProfile shotProfile, Entity entity) {
        float crouchingMultiplier = 0.667f;
        float penaltyPerMovement = 7.5f;
        float maxMovementPenalty = 20f;
        Vec3 reconstructedDeltaMovement = new Vec3(entity.getX(), entity.getY(), entity.getZ()).subtract(entity.xOld, entity.yOld, entity.zOld);
        float spread = (float) shotProfile.value(ShotComponents.SPREAD);
        if (GunItem.isChargingBayonet(entity)) {
            spread += 4;
        }
        if (entity.isCrouching()) {
            spread *= crouchingMultiplier;
        }
        if (!entity.onGround()) {
            // fixme: technically doesn't work if spread is zero
            spread *= (float) shotProfile.value(ShotComponents.IN_AIR_PENALTY);
        }

        float entitySpeed = (float) reconstructedDeltaMovement.length();
        if (entitySpeed > 0.1) {
            float penalty = Mth.clamp(penaltyPerMovement * entitySpeed - 0.05f, 0, maxMovementPenalty);
            spread += penalty;
        }
        return Math.max(0, spread);
    }

    public static ShotProfile compose(@Nullable LivingEntity living, GunProfile gunProfile, ItemStack gunStack) {
        GunContainer modifiers = new GunContainer(gunStack);
        ShotComponentMap components = gunProfile.baseProfile();
        for (int slot = 0; slot < modifiers.getContainerSize(); slot++) {
            ItemStack stack = modifiers.getItem(slot);
            if (stack.getItem() instanceof ModifierItem modifierItem) {
                modifierItem.getModifier().apply(components);
            }
        }
        ShotProfile profile = new ShotProfile(gunStack, gunProfile, MagazineContents.get(gunStack), components);
        if (living != null) {
            if (living instanceof Player player && GunItem.isScoping(player)) {
                profile.modifyValue(ShotComponents.CAMERA_RECOIL_MULTIPLIER, new ValueModifier(-0.5, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL));
            }
            NeoForge.EVENT_BUS.post(new ComposeShotEvent(living, profile));
        }
        return profile;
    }

    private static boolean requiresAmmo(LivingEntity living) {
        return living instanceof Player player && !player.hasInfiniteMaterials();
    }

    public static ReloadResult attemptFinishReload(LivingEntity living, ItemStack gun, int roundsToLoad) {
        if (!(gun.getItem() instanceof GunItem gunItem)) {
            return ReloadResult.NO_AMMO;
        }
        int capacity = gunItem.magazineCapacity();
        MagazineContents magazine = GunItem.getMagazine(gun);
        int missing = magazine.missing(capacity);
        if (missing <= 0) {
            return ReloadResult.ALREADY_FULL;
        }
        boolean needsAmmo = requiresAmmo(living);
        int available = needsAmmo ? countBullets((Player) living) : missing;
        if (needsAmmo && available <= 0) {
            return ReloadResult.NO_AMMO;
        }
        int toLoad = Math.min(missing, available);
        if (roundsToLoad > 0) {
            toLoad = Math.min(toLoad, roundsToLoad);
        }
        if (needsAmmo) {
            consumeBullets((Player) living, toLoad);
        }
        GunItem.setMagazine(gun, magazine.with(magazine.count() + toLoad));
        return ReloadResult.FINISHED_RELOAD;
    }

    public static ReloadResult attemptStartReload(LivingEntity living, ItemStack gun) {
        if (!(gun.getItem() instanceof GunItem gunItem)) {
            return ReloadResult.NO_AMMO;
        }

        int capacity = gunItem.magazineCapacity();
        MagazineContents magazine = GunItem.getMagazine(gun);
        int missing = magazine.missing(capacity);
        if (missing <= 0) {
            return ReloadResult.ALREADY_FULL;
        }

        boolean needsAmmo = requiresAmmo(living);
        if (needsAmmo) {
            int available = countBullets((Player) living);
            if (available <= 0) {
                return ReloadResult.NO_AMMO;
            }
            missing = Math.min(missing, available);
        }
        if (!living.level().isClientSide()) {
            ShotProfile shotProfile = compose(living, gunItem.getGun(), gun);
            double speed = shotProfile.value(ShotComponents.RELOAD_SPEED_MULTIPLIER);
            TopLoadConfig topLoad = gunItem.getGun().topLoadConfig();
            boolean topOff = topLoad != null && missing < capacity;
            ReloadState state = ReloadState.start(gun, gunItem.getGun().reloadTimeTicks(), speed, missing, topOff ? topLoad : null);
            playReloadAnimation(living, gun);
        }
        if (living.isUsingItem()) {
            living.stopUsingItem();
        }
        return ReloadResult.STARTING_RELOAD;
    }

    public static int countBullets(Player player) {
        Inventory inventory = player.getInventory();
        int total = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(IronsArtificeTags.AMMO)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void consumeBullets(Player player, int amount) {
        Inventory inventory = player.getInventory();
        int remaining = amount;
        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(IronsArtificeTags.AMMO)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
        }
    }
}
