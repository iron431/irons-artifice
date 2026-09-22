package io.redspace.irons_artifice.client.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public class FairyDustParticleType extends ParticleType<FairyDustParticleOption> implements ITrailParticle {
    public FairyDustParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }

    @Override
    public MapCodec<FairyDustParticleOption> codec() {
        return FairyDustParticleOption.codec(this);
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, FairyDustParticleOption> streamCodec() {
        return FairyDustParticleOption.streamCodec(this);
    }

    @Override
    public ParticleOptions applyTrailInterpolation(ParticleOptions base, float percent) {
        if (!(base instanceof FairyDustParticleOption trail)) return base;
        return new FairyDustParticleOption(trail.getType(), trail.getPhase() + ((float) (Math.PI * 2)) * percent, trail.getRadius(), trail.getAxis());
    }
}
