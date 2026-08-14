package io.redspace.irons_artifice.item;

import com.geckolib.animatable.GeoItem;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.client.ClientHelper;
import io.redspace.irons_artifice.data.ReloadResult;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.GunProfile;
import io.redspace.irons_artifice.gun.MuzzleFlashSettings;
import io.redspace.irons_artifice.gun.MuzzleFlashType;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.menu.GunContainer;
import io.redspace.irons_artifice.modifier.ModifierItem;
import io.redspace.irons_artifice.network.ClientboundGunAnimationPacket;
import io.redspace.irons_artifice.network.ClientboundMuzzleFlashPacket;
import io.redspace.irons_artifice.network.ClientboundReloadCrosshairAnimationPacket;
import io.redspace.irons_artifice.recoil.RecoilState;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
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

        if (magazine.isEmpty()) {
            if (shooter instanceof Player player && player.level().isClientSide()) {
                ClientHelper.handleLocalDryFire(player, profile.get(ShotComponents.GUNSHOT_SOUND).getDryFireSound());
            }
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
        GunItem.setMagazine(stack, magazine.deplete());
        profile.get(ShotComponents.GUNSHOT_SOUND).playGunShotSound(level, shooter.position());
        RecoilState.addImpulse(shooter, now, profile);
        fireShot(level, shooter, shooter.getEyePosition(), Vec3.directionFromRotation(pitch, yaw), profile);
        applyCharacterBlowback(shooter, profile);
        playFireAnimation(shooter, stack, gunItem, profile);
        return true;
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
        // item cooldowns seem to tick before inventory tick. new system uses latter
        // advance one tick in order to maintain intended parity, and one-tick cooldowns meaning "can shoot next tick"
        ticks -= 1;
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
                "fire", (fireSpeedMultiplier + 2) / 3, 0);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(living, packet);
    }

    private static void playReloadAnimation(LivingEntity living, ItemStack stack, GunItem gunItem, ShotProfile profile) {
        ReloadState existingState = ReloadState.get(stack);
        double speed = profile.value(ShotComponents.RELOAD_SPEED_MULTIPLIER);
        double offsetSeconds = 0;
        if (existingState != null) {
            offsetSeconds = existingState.animationProgressSeconds(gunItem.getGunProfile());
        }
        ClientboundGunAnimationPacket packet = new ClientboundGunAnimationPacket(living.getId(), GeoItem.getOrAssignId(stack, (ServerLevel) living.level()), stack == living.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                "reload", speed, offsetSeconds);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(living, packet);
    }

    private static void fireShot(ServerLevel level, LivingEntity shooter, Vec3 origin, Vec3 direction, ShotProfile profile) {
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
    }

    private static void spawnMuzzleFlash(ServerLevel level, LivingEntity shooter, Vec3 direction, ShotProfile profile) {
        MuzzleFlashSettings settings = profile.get(ShotComponents.MUZZLE_FLASH);
        if (settings.types().isEmpty()) {
            return;
        }
        MuzzleFlashType type = settings.pick(level.getRandom());
        Vec3 position = shooter.getEyePosition();
        Vec3 offset = direction.normalize().scale(settings.muzzleDistanceScalar());
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
        float penaltyPerMovement = 12f;
        float maxMovementPenalty = 25f;

        float spread = (float) shotProfile.value(ShotComponents.SPREAD);
        if (entity.isCrouching()) {
            spread *= crouchingMultiplier;
        }
        if (!entity.onGround()) {
            spread *= (float) shotProfile.value(ShotComponents.IN_AIR_PENALTY);
        }
        float entitySpeed = (float) entity.getDeltaMovement().length();
        if (entitySpeed > 0.1) {
            float penalty = Mth.clamp(penaltyPerMovement * entitySpeed - 0.05f, 0, maxMovementPenalty);
            spread += penalty;
        }
        return spread;
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
            if (living instanceof Player player && player.isScoping()) {
                profile.components().getOrCreate(ShotComponents.CAMERA_RECOIL_MULTIPLIER).addModifier(new ValueModifier(-0.75, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL));
            }
            NeoForge.EVENT_BUS.post(new ComposeShotEvent(living, profile));
        }
        return profile;
    }

    private static boolean requiresAmmo(LivingEntity living) {
        return living instanceof Player player && !player.hasInfiniteMaterials();
    }

    public static ReloadResult attemptFinishReload(LivingEntity living, ItemStack gun) {
        // fixme: lots of duplicated checks with attemptStartReload
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
        if (needsAmmo) {
            consumeBullets((Player) living, toLoad);
        } else {
            toLoad = missing;
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
        if (needsAmmo && countBullets((Player) living) <= 0) {
            return ReloadResult.NO_AMMO;
        }
        if (!living.level().isClientSide()) {
            ShotProfile shotProfile = compose(living, gunItem.getGunProfile(), gun);
            int ticks = (int) (gunItem.getGunProfile().reloadTimeTicks() / shotProfile.value(ShotComponents.RELOAD_SPEED_MULTIPLIER));
            GunItem.startReload(gun, gunItem.getGunProfile().reloadTimeTicks(), shotProfile.value(ShotComponents.RELOAD_SPEED_MULTIPLIER));
            if (living instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new ClientboundReloadCrosshairAnimationPacket(ticks));
            }
            playReloadAnimation(living, gun, gunItem, shotProfile);
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
