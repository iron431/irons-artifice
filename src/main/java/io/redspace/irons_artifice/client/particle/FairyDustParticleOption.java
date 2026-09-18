package io.redspace.irons_artifice.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public class FairyDustParticleOption implements ParticleOptions {
    /**
     * {@code Vec3} carries no stream codec of its own, so the three components are written by hand.
     */
    private static final StreamCodec<ByteBuf, Vec3> VEC3_STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeDouble(value.x);
                buf.writeDouble(value.y);
                buf.writeDouble(value.z);
            },
            buf -> new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
    );

    public static MapCodec<FairyDustParticleOption> codec(ParticleType<FairyDustParticleOption> type) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.FLOAT.fieldOf("phase").forGetter(o -> o.phase),
                Codec.FLOAT.fieldOf("radius").forGetter(o -> o.radius),
                Vec3.CODEC.fieldOf("axis").forGetter(o -> o.axis)
        ).apply(builder, (phase, radius, axis) -> new FairyDustParticleOption(type, phase, radius, axis)));
    }

    public static StreamCodec<? super RegistryFriendlyByteBuf, FairyDustParticleOption> streamCodec(ParticleType<FairyDustParticleOption> type) {
        return StreamCodec.composite(
                ByteBufCodecs.FLOAT, o -> o.phase,
                ByteBufCodecs.FLOAT, o -> o.radius,
                VEC3_STREAM_CODEC, o -> o.axis,
                (phase, radius, axis) -> new FairyDustParticleOption(type, phase, radius, axis)
        );
    }

    private final ParticleType<FairyDustParticleOption> type;
    private final float phase;
    private final float radius;
    private final Vec3 axis;

    public FairyDustParticleOption(ParticleType<FairyDustParticleOption> type, float phase, float radius, Vec3 axis) {
        this.type = type;
        this.phase = phase;
        this.radius = radius;
        this.axis = axis;
    }

    public float getPhase() {
        return phase;
    }

    public float getRadius() {
        return radius;
    }

    public Vec3 getAxis() {
        return axis;
    }

    @Override
    public ParticleType<FairyDustParticleOption> getType() {
        return type;
    }
}
