package io.redspace.irons_artifice.data;

import io.redspace.irons_artifice.client.particle.MuzzleFlashParticleOption;
import io.redspace.irons_artifice.registry.ParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import org.joml.Vector3f;

public enum MuzzleFlashType {
    LARGE,
    TRIANGLE,
    SMALL_STAR,
    ;

    public ParticleType<MuzzleFlashParticleOption> particleType() {
        return switch (this) {
            case LARGE -> ParticleRegistry.MUZZLE_FLASH_LARGE.get();
            case TRIANGLE -> ParticleRegistry.MUZZLE_FLASH_TRIANGLE.get();
            case SMALL_STAR -> ParticleRegistry.MUZZLE_FLASH_SMALL_STAR.get();
        };
    }

    public ParticleOptions particle(Vector3f tint) {
        return new MuzzleFlashParticleOption(particleType(), tint.x, tint.y, tint.z);
    }
}
