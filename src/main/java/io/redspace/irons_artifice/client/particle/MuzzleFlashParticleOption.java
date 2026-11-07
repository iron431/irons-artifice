package io.redspace.irons_artifice.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class MuzzleFlashParticleOption implements ParticleOptions {
    public static MapCodec<MuzzleFlashParticleOption> codec(ParticleType<MuzzleFlashParticleOption> type) {
        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.FLOAT.fieldOf("r").forGetter(MuzzleFlashParticleOption::r),
                Codec.FLOAT.fieldOf("g").forGetter(MuzzleFlashParticleOption::g),
                Codec.FLOAT.fieldOf("b").forGetter(MuzzleFlashParticleOption::b),
                Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(MuzzleFlashParticleOption::scale)
        ).apply(builder, (r, g, b, scale) -> new MuzzleFlashParticleOption(type, r, g, b, scale)));
    }

    public static StreamCodec<? super RegistryFriendlyByteBuf, MuzzleFlashParticleOption> streamCodec(
            ParticleType<MuzzleFlashParticleOption> type
    ) {
        return StreamCodec.composite(
                ByteBufCodecs.FLOAT, MuzzleFlashParticleOption::r,
                ByteBufCodecs.FLOAT, MuzzleFlashParticleOption::g,
                ByteBufCodecs.FLOAT, MuzzleFlashParticleOption::b,
                ByteBufCodecs.FLOAT, MuzzleFlashParticleOption::scale,
                (r, g, b, scale) -> new MuzzleFlashParticleOption(type, r, g, b, scale)
        );
    }

    private final ParticleType<MuzzleFlashParticleOption> type;
    private final float r;
    private final float g;
    private final float b;
    private final float scale;

    public MuzzleFlashParticleOption(ParticleType<MuzzleFlashParticleOption> type, float r, float g, float b) {
        this(type, r, g, b, 1f);
    }

    public MuzzleFlashParticleOption(ParticleType<MuzzleFlashParticleOption> type, float r, float g, float b, float scale) {
        this.type = type;
        this.r = r;
        this.g = g;
        this.b = b;
        this.scale = scale;
    }

    public float r() {
        return r;
    }

    public float g() {
        return g;
    }

    public float b() {
        return b;
    }

    public float scale() {
        return scale;
    }

    public boolean isTinted() {
        return r != 1f || g != 1f || b != 1f;
    }

    @Override
    public ParticleType<MuzzleFlashParticleOption> getType() {
        return type;
    }
}
