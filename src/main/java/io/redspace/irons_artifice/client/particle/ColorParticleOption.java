package io.redspace.irons_artifice.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.FastColor;

public class ColorParticleOption implements ParticleOptions {
    private final ParticleType<ColorParticleOption> type;
    private final int color;

    public static Codec<ColorParticleOption> codec(ParticleType<ColorParticleOption> type) {
        return Codec.INT.fieldOf("color").codec().xmap(color -> new ColorParticleOption(type, color), o -> o.color);
    }

    public static final ParticleOptions.Deserializer<ColorParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public ColorParticleOption fromCommand(ParticleType<ColorParticleOption> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            return new ColorParticleOption(type, reader.readInt());
        }

        @Override
        public ColorParticleOption fromNetwork(ParticleType<ColorParticleOption> type, FriendlyByteBuf buf) {
            return new ColorParticleOption(type, buf.readInt());
        }
    };

    private ColorParticleOption(ParticleType<ColorParticleOption> type, int color) {
        this.type = type;
        this.color = color;
    }

    @Override
    public ParticleType<ColorParticleOption> getType() {
        return this.type;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeInt(this.color);
    }

    @Override
    public String writeToString() {
        return BuiltInRegistries.PARTICLE_TYPE.getKey(this.type) + " " + this.color;
    }

    public float getRed() {
        return FastColor.ARGB32.red(this.color) / 255.0F;
    }

    public float getGreen() {
        return FastColor.ARGB32.green(this.color) / 255.0F;
    }

    public float getBlue() {
        return FastColor.ARGB32.blue(this.color) / 255.0F;
    }

    public float getAlpha() {
        return FastColor.ARGB32.alpha(this.color) / 255.0F;
    }

    public static ColorParticleOption create(ParticleType<ColorParticleOption> type, int color) {
        return new ColorParticleOption(type, color);
    }

    public static ColorParticleOption create(ParticleType<ColorParticleOption> type, float r, float g, float b) {
        return create(type, FastColor.ARGB32.color(255, (int) (r * 255.0F), (int) (g * 255.0F), (int) (b * 255.0F)));
    }
}
