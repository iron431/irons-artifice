package io.redspace.irons_artifice.data;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.sounds.GunShotSoundSettings;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.modifier.OnHitEffects;
import io.redspace.irons_artifice.modifier.PostHitEffects;
import io.redspace.irons_artifice.registry.SoundRegistry;
import net.minecraft.sounds.SoundEvents;

import java.util.Optional;

public final class ShotComponents {
    // Shot Mechanics
    public static final ComponentType<Value> PROJECTILE_COUNT = new ComponentType<>(IronsArtifice.id("projectile_count"), () -> Value.of(1));
    public static final ComponentType<Value> SPREAD = new ComponentType<>(IronsArtifice.id("spread"), () -> Value.of(0));
    public static final ComponentType<Value> IN_AIR_PENALTY = new ComponentType<>(IronsArtifice.id("in_air_penalty"), () -> Value.of(1.5));
    public static final ComponentType<Value> FIRE_DELAY = new ComponentType<>(IronsArtifice.id("fire_delay"), () -> Value.of(1));
    public static final ComponentType<Value> FIRE_RATE = new ComponentType<>(IronsArtifice.id("fire_rate"), () -> Value.of(1));
    public static final ComponentType<Value> AMMO_CONSUME_CHANCE = new ComponentType<>(IronsArtifice.id("ammo_consume_chance"), () -> Value.of(1));
    public static final ComponentType<Boolean> FORCE_AUTO_FIRE = new ComponentType<>(IronsArtifice.id("force_auto_fire"), () -> false);
    public static final ComponentType<Value> ACCELERATING = new ComponentType<>(IronsArtifice.id("accelerating"), () -> Value.of(0));

    // Attributes
    public static final ComponentType<Value> DAMAGE = new ComponentType<>(IronsArtifice.id("damage"), () -> Value.of(0));
    public static final ComponentType<Value> BULLET_SPEED = new ComponentType<>(IronsArtifice.id("bullet_speed"), () -> Value.of(Bullet.BASE_SPEED));
    public static final ComponentType<Value> GRAVITY = new ComponentType<>(IronsArtifice.id("gravity"), () -> Value.of(0.05));
    public static final ComponentType<Value> KNOCKBACK = new ComponentType<>(IronsArtifice.id("knockback"), () -> Value.of(0));
    public static final ComponentType<Value> BULLET_DRAG = new ComponentType<>(IronsArtifice.id("bullet_drag"), () -> Value.of(.98));
    public static final ComponentType<Value> BLOCK_DAMAGE_MULTIPLIER = new ComponentType<>(IronsArtifice.id("block_damage_multiplier"), () -> Value.of(1));
    public static final ComponentType<Value> RELOAD_SPEED_MULTIPLIER = new ComponentType<>(IronsArtifice.id("reload_speed_multiplier"), () -> Value.of(1));

    // Bullet Behavior
    public static final ComponentType<Value> PIERCING = new ComponentType<>(IronsArtifice.id("piercing"), () -> Value.of(0));
    public static final ComponentType<Value> RICOCHET = new ComponentType<>(IronsArtifice.id("ricochet"), () -> Value.of(0));
    public static final ComponentType<OnHitEffects> ON_HIT = new ComponentType<>(IronsArtifice.id("on_hit"), OnHitEffects::new);
    public static final ComponentType<PostHitEffects> POST_HIT_EFFECTS = new ComponentType<>(IronsArtifice.id("post_hit_effects"), PostHitEffects::new);
    public static final ComponentType<Boolean> BREAKS_BLOCKS = new ComponentType<>(IronsArtifice.id("breaks_blocks"), () -> false);
    public static final ComponentType<Value> SEEKING = new ComponentType<>(IronsArtifice.id("seeking"), () -> Value.of(0));

    // Effects
    public static final ComponentType<RecoilProfile> CAMERA_RECOIL = new ComponentType<>(IronsArtifice.id("camera_recoil"), () -> RecoilProfile.simple(10, 0));
    public static final ComponentType<Value> CAMERA_RECOIL_MULTIPLIER = new ComponentType<>(IronsArtifice.id("camera_recoil_multiplier"), () -> Value.of(1));
    public static final ComponentType<Value> CHARACTER_BLOWBACK = new ComponentType<>(IronsArtifice.id("character_blowback"), () -> Value.of(0));
    public static final ComponentType<GunShotSoundStack> GUNSHOT_SOUND = new ComponentType<>(IronsArtifice.id("gunshot_sound"), () -> new GunShotSoundStack(
            GunShotSoundSettings.of(SoundEvents.FIREWORK_ROCKET_BLAST, 0.9f, 1.1f, -1f, 0f, 48f),
            PlayableSound.of(PlayableSound.holder(SoundEvents.DISPENSER_FAIL), 0.75f, 1.4f, 1.6f)));
    public static final ComponentType<ImpactSoundStack> IMPACT_SOUND = new ComponentType<>(IronsArtifice.id("impact_sound"), () -> new ImpactSoundStack(
            Optional.of(PlayableSound.of(SoundRegistry.BULLET_IMPACT_GENERIC, 2f, .8f, 1.2f)), Optional.empty()
    ));
    public static final ComponentType<ParticleStack> PARTICLE_TRAIL = new ComponentType<>(IronsArtifice.id("particle_trail"), ParticleStack::new);
    public static final ComponentType<MuzzleFlashSettings> MUZZLE_FLASH = new ComponentType<>(IronsArtifice.id("muzzle_flash"), MuzzleFlashSettings.DEFAULT);

}
