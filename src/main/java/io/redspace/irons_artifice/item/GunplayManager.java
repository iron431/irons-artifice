package io.redspace.irons_artifice.item;

import com.geckolib.animatable.GeoItem;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.api.AmmoEvent;
import io.redspace.irons_artifice.api.GunAboutToShootEvent;
import io.redspace.irons_artifice.api.GunShootEvent;
import io.redspace.irons_artifice.client.ClientHelper;
import io.redspace.irons_artifice.data.MuzzleFlashSettings;
import io.redspace.irons_artifice.data.MuzzleFlashType;
import io.redspace.irons_artifice.data.PlayableSound;
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
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import io.redspace.irons_artifice.registry.SoundRegistry;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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
import org.jspecify.annotations.Nullable;

public final class GunplayManager {

    public static boolean tryFire(LivingEntity shooter, Vec3 direction) {
        if (!shooter.isAlive() || shooter.isSpectator()) {
            return false;
        }
        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack stack = shooter.getItemInHand(hand);
        if (!(stack.getItem() instanceof GunItem gunItem)) {
            return false;
        }
        if (FireDelayState.isActive(stack) || GunItem.isReloading(stack)) {
            return false;
        }
        MagazineContents magazine = GunItem.getMagazine(stack);
        GunProfile gunProfile = gunItem.getGun();
        ShotProfile profile = compose(shooter, gunProfile, stack);

        int ammoToConsume = NeoForge.EVENT_BUS.post(new AmmoEvent.Amount(shooter, profile, 1)).getAmmoToConsume();
        if (magazine.count() < ammoToConsume) {
            if (shooter instanceof Player player && player.level().isClientSide()) {
                ClientHelper.handleLocalDryFire(player, profile.get(ShotComponents.GUNSHOT_SOUND).getDryFireSound());
            }
            return false;
        }
        if (NeoForge.EVENT_BUS.post(new GunAboutToShootEvent(shooter, profile)).isCanceled()) {
            return false;
        }
        beginFireDelay(shooter, stack, (int) Math.round(profile.fireDelayTicks()), pitchMultiplierForFire(profile));
        if (!(shooter.level() instanceof ServerLevel level)) {
            return true;
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
        profile.get(ShotComponents.GUNSHOT_SOUND).playGunShotSound(level, shooter.position());
        RecoilState.addImpulse(shooter, now, profile);
        fireShot(level, shooter, shooter.getEyePosition(), Vec3.directionFromRotation(pitch, yaw), profile);
        applyCharacterBlowback(shooter, profile);
        playFireAnimation(shooter, stack, gunItem, profile);
        if (hand == InteractionHand.MAIN_HAND && shooter.isUsingItem() && shooter.getUseItem() != stack && GunItem.isOffhandItemUseBlocked(shooter)) {
            shooter.stopUsingItem();
        }
        return true;
    }

    private static void depleteMagazine(LivingEntity shooter, ShotProfile profile, ItemStack stack, MagazineContents magazine, int ammoToConsume) {
        if (ammoToConsume <= 0) {
            return;
        }
        var event = new AmmoEvent.Consume(shooter, profile, ammoToConsume);
        if (!NeoForge.EVENT_BUS.post(event).isCanceled()){
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
        profile.get(ShotComponents.GUNSHOT_SOUND).playGunShotSound(level, origin);
        playFireAnimation(player, stack, gunItem, profile);
        return true;
    }

    private static float pitchMultiplierForFire(ShotProfile profile) {
        return (float) ((profile.value(ShotComponents.FIRE_RATE) + 2) / 3);
    }

    private static void beginFireDelay(LivingEntity shooter, ItemStack stack, int ticks, float pitchMultiplier) {
        if (ticks > 0) {
            FireDelayState.start(stack, ticks, pitchMultiplier);
        }
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
        double fireSpeedMultiplier = profile.get(ShotComponents.FIRE_DELAY).base() / profile.fireDelayTicks();
        ClientboundGunAnimationPacket packet = new ClientboundGunAnimationPacket(living.getId(), GeoItem.getOrAssignId(stack, (ServerLevel) living.level()), stack == living.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                "fire", (fireSpeedMultiplier + 1) / 2, 0);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(living, packet);
    }

    public static void playReloadAnimation(LivingEntity living, ItemStack stack) {
        ReloadState state = ReloadState.get(stack);
        if (state == null) {
            return;
        }
        ClientboundGunAnimationPacket packet = new ClientboundGunAnimationPacket(living.getId(), GeoItem.getOrAssignId(stack, (ServerLevel) living.level()), stack == living.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                "reload", state.speed(), state.progress(), state.skipAt(), state.skipTo());
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
        int projectileCount = Math.max(1, (int) Math.round(profile.value(ShotComponents.PROJECTILE_COUNT)));
        float speed = (float) profile.value(ShotComponents.BULLET_SPEED);
        float spread = getSpreadForEntity(profile, shooter);
        for (int i = 0; i < projectileCount; i++) {
            Bullet bullet = new Bullet(EntityRegistry.BULLET.get(), level);
            bullet.setOwner(shooter);
            bullet.applyProfile(profile.copy());
            bullet.setPos(origin);
            bullet.shoot(direction.x, direction.y, direction.z, speed, spread);
            level.addFreshEntity(bullet);
        }
        spawnMuzzleFlash(level, shooter, direction, profile);
        NeoForge.EVENT_BUS.post(new GunShootEvent.Post(shooter, profile));
    }

    private static void spawnMuzzleFlash(ServerLevel level, LivingEntity shooter, Vec3 direction, ShotProfile profile) {
        MuzzleFlashSettings settings = profile.get(ShotComponents.MUZZLE_FLASH);
        if (settings.types().isEmpty()) {
            return;
        }
        MuzzleFlashType type = settings.pick(level.getRandom());
        Vec3 position = shooter.getEyePosition();
        Vec3 offset = direction.normalize();
        double length = settings.muzzleDistanceScalar();
        float offsetDirection = shooter.getMainArm() == HumanoidArm.LEFT ? -1.0F : 1.0F;
        offset = offset.scale(Math.max(1.25, 0.75 * length));
        offset = offset.add(shooter.getForward().cross(new Vec3(0, 1, 0))
                .scale(0.5 * offsetDirection));
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(shooter, new ClientboundMuzzleFlashPacket(
                type.particle(settings.pickTint(level.getRandom())),
                shooter.getId(),
                shooter.getDeltaMovement(),
                position,
                offset
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
                profile.components().getOrCreate(ShotComponents.CAMERA_RECOIL_MULTIPLIER).addModifier(new ValueModifier(-0.5, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL));
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
            if (stack.is(ItemRegistry.BULLET.get())) {
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
            if (stack.is(ItemRegistry.BULLET.get())) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
        }
    }
}
