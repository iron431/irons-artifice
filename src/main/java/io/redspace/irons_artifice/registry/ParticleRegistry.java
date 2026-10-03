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
 * @deprecated Use {@link IronsArtificeParticles}.
 */
@Deprecated
public final class ParticleRegistry {

    @Deprecated
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = IronsArtificeParticles.PARTICLE_TYPES;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<BlockParticleOption>> BLOCK_IMPACT = IronsArtificeParticles.BLOCK_IMPACT;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<BlockParticleOption>> BLOCK_DUST = IronsArtificeParticles.BLOCK_DUST;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorTransitionParticleOption>> BULLET_TRAIL = IronsArtificeParticles.BULLET_TRAIL;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorTransitionParticleOption>> BULLET_IMPACT = IronsArtificeParticles.BULLET_IMPACT;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<MuzzleFlashParticleOption>> MUZZLE_FLASH_LARGE = IronsArtificeParticles.MUZZLE_FLASH_LARGE;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<MuzzleFlashParticleOption>> MUZZLE_FLASH_TRIANGLE = IronsArtificeParticles.MUZZLE_FLASH_TRIANGLE;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<MuzzleFlashParticleOption>> MUZZLE_FLASH_SMALL_STAR = IronsArtificeParticles.MUZZLE_FLASH_SMALL_STAR;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<MuzzleFlashParticleOption>> EXPLOSION_96 = IronsArtificeParticles.EXPLOSION_96;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorTransitionParticleOption>> LIGHTNING_TRAIL = IronsArtificeParticles.LIGHTNING_TRAIL;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorTransitionParticleOption>> FIRE_TRAIL = IronsArtificeParticles.FIRE_TRAIL;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<FairyDustParticleOption>> FAIRY_DUST = IronsArtificeParticles.FAIRY_DUST;

    @Deprecated
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> SPLASH = IronsArtificeParticles.SPLASH;
}
