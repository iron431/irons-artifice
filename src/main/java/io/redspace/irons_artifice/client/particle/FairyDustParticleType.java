package io.redspace.irons_artifice.client.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.Mth;

public class FairyDustParticleType extends ParticleType<FairyDustParticleOption> implements ITrailParticle {
    public FairyDustParticleType(boolean overrideLimiter) {
        super(overrideLimiter, FairyDustParticleOption.DESERIALIZER);
    }

    @Override
    public Codec<FairyDustParticleOption> codec() {
        return FairyDustParticleOption.codec(this);
    }

    @Override
    public ParticleOptions applyTrailInterpolation(ParticleOptions base, float percent) {
        if (!(base instanceof FairyDustParticleOption trail)) return base;
        return new FairyDustParticleOption(trail.getType(), trail.getPhase() + Mth.TWO_PI * percent, trail.getRadius(), trail.getAxis());
    }
}
