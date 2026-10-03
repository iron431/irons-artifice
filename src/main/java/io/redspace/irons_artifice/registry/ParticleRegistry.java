package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.client.particle.ColorTransitionParticleOption;
import io.redspace.irons_artifice.client.particle.FairyDustParticleOption;
import io.redspace.irons_artifice.client.particle.MuzzleFlashParticleOption;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @deprecated Use {@link IronsArtificeRegistries.Particles}.
 */
@Deprecated
public final class ParticleRegistry {
    private ParticleRegistry() {
    }

    @Deprecated
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = IronsArtificeRegistries.Particles.PARTICLE_TYPES;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<BlockParticleOption>> BLOCK_IMPACT = IronsArtificeRegistries.Particles.BLOCK_IMPACT;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<BlockParticleOption>> BLOCK_DUST = IronsArtificeRegistries.Particles.BLOCK_DUST;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorTransitionParticleOption>> BULLET_TRAIL = IronsArtificeRegistries.Particles.BULLET_TRAIL;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorTransitionParticleOption>> BULLET_IMPACT = IronsArtificeRegistries.Particles.BULLET_IMPACT;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<MuzzleFlashParticleOption>> MUZZLE_FLASH_LARGE = IronsArtificeRegistries.Particles.MUZZLE_FLASH_LARGE;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<MuzzleFlashParticleOption>> MUZZLE_FLASH_TRIANGLE = IronsArtificeRegistries.Particles.MUZZLE_FLASH_TRIANGLE;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<MuzzleFlashParticleOption>> MUZZLE_FLASH_SMALL_STAR = IronsArtificeRegistries.Particles.MUZZLE_FLASH_SMALL_STAR;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<MuzzleFlashParticleOption>> EXPLOSION_96 = IronsArtificeRegistries.Particles.EXPLOSION_96;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorTransitionParticleOption>> LIGHTNING_TRAIL = IronsArtificeRegistries.Particles.LIGHTNING_TRAIL;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorTransitionParticleOption>> FIRE_TRAIL = IronsArtificeRegistries.Particles.FIRE_TRAIL;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<FairyDustParticleOption>> FAIRY_DUST = IronsArtificeRegistries.Particles.FAIRY_DUST;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> SPLASH = IronsArtificeRegistries.Particles.SPLASH;
}
