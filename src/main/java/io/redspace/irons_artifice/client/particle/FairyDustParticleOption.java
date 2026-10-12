package io.redspace.irons_artifice.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public class FairyDustParticleOption implements ParticleOptions {
    public static Codec<FairyDustParticleOption> codec(ParticleType<FairyDustParticleOption> type) {
        return RecordCodecBuilder.create(builder -> builder.group(
                Codec.FLOAT.fieldOf("phase").forGetter(o -> o.phase),
                Codec.FLOAT.fieldOf("radius").forGetter(o -> o.radius),
                Vec3.CODEC.fieldOf("axis").forGetter(o -> o.axis)
        ).apply(builder, (phase, radius, axis) -> new FairyDustParticleOption(type, phase, radius, axis)));
    }

    public static final ParticleOptions.Deserializer<FairyDustParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public FairyDustParticleOption fromCommand(ParticleType<FairyDustParticleOption> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float phase = reader.readFloat();
            reader.expect(' ');
            float radius = reader.readFloat();
            reader.expect(' ');
            double x = reader.readDouble();
            reader.expect(' ');
            double y = reader.readDouble();
            reader.expect(' ');
            double z = reader.readDouble();
            return new FairyDustParticleOption(type, phase, radius, new Vec3(x, y, z));
        }

        @Override
        public FairyDustParticleOption fromNetwork(ParticleType<FairyDustParticleOption> type, FriendlyByteBuf buf) {
            return new FairyDustParticleOption(type, buf.readFloat(), buf.readFloat(),
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }
    };

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

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(this.phase);
        buf.writeFloat(this.radius);
        buf.writeDouble(this.axis.x);
        buf.writeDouble(this.axis.y);
        buf.writeDouble(this.axis.z);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f",
                BuiltInRegistries.PARTICLE_TYPE.getKey(this.type),
                this.phase, this.radius, this.axis.x, this.axis.y, this.axis.z);
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
