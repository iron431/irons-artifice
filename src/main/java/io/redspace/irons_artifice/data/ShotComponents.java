package io.redspace.irons_artifice.data;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.sounds.GunShotSoundSettings;
import io.redspace.irons_artifice.modifier.OnHitEffects;
import io.redspace.irons_artifice.modifier.PostHitEffects;
import io.redspace.irons_artifice.registry.SoundRegistry;
import net.minecraft.sounds.SoundEvents;

import java.util.Optional;

/**
 * Everything about a shot that is not a number. Numeric stats and toggles are attributes, see {@link io.redspace.irons_artifice.registry.AttributeRegistry}.
 */
public final class ShotComponents {
    // Bullet Behavior
    public static final ComponentType<OnHitEffects> ON_HIT = new ComponentType<>(IronsArtifice.id("on_hit"), OnHitEffects::new);
    public static final ComponentType<PostHitEffects> POST_HIT_EFFECTS = new ComponentType<>(IronsArtifice.id("post_hit_effects"), PostHitEffects::new);

    // UX/VFX
    public static final ComponentType<RecoilProfile> CAMERA_RECOIL = new ComponentType<>(IronsArtifice.id("camera_recoil"), () -> RecoilProfile.simple(10, 0));
    public static final ComponentType<GunShotSoundStack> GUNSHOT_SOUND = new ComponentType<>(IronsArtifice.id("gunshot_sound"), () -> new GunShotSoundStack(
            GunShotSoundSettings.of(SoundEvents.FIREWORK_ROCKET_BLAST, 0.9f, 1.1f, -1f, 0f, 48f),
            PlayableSound.of(PlayableSound.holder(SoundEvents.DISPENSER_FAIL), 0.75f, 1.4f, 1.6f)));
    public static final ComponentType<ImpactSoundStack> IMPACT_SOUND = new ComponentType<>(IronsArtifice.id("impact_sound"), () -> new ImpactSoundStack(
            Optional.of(PlayableSound.of(SoundRegistry.BULLET_IMPACT_GENERIC, 2f, .8f, 1.2f)), Optional.empty()
    ));
    public static final ComponentType<ParticleStack> PARTICLE_TRAIL = new ComponentType<>(IronsArtifice.id("particle_trail"), ParticleStack::new);
    public static final ComponentType<MuzzleFlashSettings> MUZZLE_FLASH = new ComponentType<>(IronsArtifice.id("muzzle_flash"), MuzzleFlashSettings.DEFAULT);

}
