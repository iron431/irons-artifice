package io.redspace.irons_artifice.client.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;

public class MuzzleFlashParticleType extends ParticleType<MuzzleFlashParticleOption> {
    public MuzzleFlashParticleType(boolean overrideLimiter) {
        super(overrideLimiter, MuzzleFlashParticleOption.DESERIALIZER);
    }

    @Override
    public Codec<MuzzleFlashParticleOption> codec() {
        return MuzzleFlashParticleOption.codec(this);
    }
}
