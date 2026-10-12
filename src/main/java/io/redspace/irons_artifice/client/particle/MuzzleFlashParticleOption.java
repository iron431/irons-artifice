package io.redspace.irons_artifice.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Locale;

public class MuzzleFlashParticleOption implements ParticleOptions {
    public static Codec<MuzzleFlashParticleOption> codec(ParticleType<MuzzleFlashParticleOption> type) {
        return RecordCodecBuilder.create(builder -> builder.group(
                Codec.FLOAT.fieldOf("r").forGetter(MuzzleFlashParticleOption::r),
                Codec.FLOAT.fieldOf("g").forGetter(MuzzleFlashParticleOption::g),
                Codec.FLOAT.fieldOf("b").forGetter(MuzzleFlashParticleOption::b),
                Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(MuzzleFlashParticleOption::scale)
        ).apply(builder, (r, g, b, scale) -> new MuzzleFlashParticleOption(type, r, g, b, scale)));
    }

    public static final ParticleOptions.Deserializer<MuzzleFlashParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public MuzzleFlashParticleOption fromCommand(ParticleType<MuzzleFlashParticleOption> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float r = reader.readFloat();
            reader.expect(' ');
            float g = reader.readFloat();
            reader.expect(' ');
            float b = reader.readFloat();
            float scale = 1f;
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip();
                scale = reader.readFloat();
            }
            return new MuzzleFlashParticleOption(type, r, g, b, scale);
        }

        @Override
        public MuzzleFlashParticleOption fromNetwork(ParticleType<MuzzleFlashParticleOption> type, FriendlyByteBuf buf) {
            return new MuzzleFlashParticleOption(type, buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
        }
    };

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

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(this.r);
        buf.writeFloat(this.g);
        buf.writeFloat(this.b);
        buf.writeFloat(this.scale);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f",
                BuiltInRegistries.PARTICLE_TYPE.getKey(this.type), this.r, this.g, this.b, this.scale);
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
