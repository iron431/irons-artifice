package io.redspace.irons_artifice.client.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public class BulletTrailParticleType extends ParticleType<ColorTransitionParticleOption>implements ITrailParticle {
    public BulletTrailParticleType(boolean overrideLimiter) {
        super(overrideLimiter, ColorTransitionParticleOption.DESERIALIZER);
    }

    @Override
    public Codec<ColorTransitionParticleOption> codec() {
        return ColorTransitionParticleOption.codec(this);
    }


    @Override
    public ParticleOptions applyTrailInterpolation(ParticleOptions base, float percent) {
        if (!(base instanceof ColorTransitionParticleOption trail)) return base;
        return new ColorTransitionParticleOption(trail.getType(),
                trail.getFromColorPacked(), trail.getToColorPacked(),
                trail.getFromIntensity(), trail.getToIntensity(),
                trail.getFromAlpha(), trail.getToAlpha(),
                trail.getFromScale(), trail.getToScale(),
                percent);
    }
}
