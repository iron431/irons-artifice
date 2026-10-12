package io.redspace.irons_artifice.data;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public record ParticleBurst(
        ParticleOptions particle,
        int count,
        float velocityScale,
        boolean force
) {
    public static final ParticleBurst SMOKE =
            new ParticleBurst(ParticleTypes.SMOKE, 12, 0.25f, true);
    public static final ParticleBurst BUBBLES =
            new ParticleBurst(ParticleTypes.BUBBLE, 40, 1.75f, false);

    public static void write(FriendlyByteBuf buf, ParticleBurst burst) {
        writeParticle(buf, burst.particle);
        buf.writeVarInt(burst.count);
        buf.writeFloat(burst.velocityScale);
        buf.writeBoolean(burst.force);
    }

    public static ParticleBurst read(FriendlyByteBuf buf) {
        return new ParticleBurst(
                readParticle(buf),
                buf.readVarInt(),
                buf.readFloat(),
                buf.readBoolean()
        );
    }

    public static void writeParticle(FriendlyByteBuf buf, ParticleOptions particle) {
        buf.writeId(BuiltInRegistries.PARTICLE_TYPE, particle.getType());
        particle.writeToNetwork(buf);
    }

    public static <T extends ParticleOptions> T readParticle(FriendlyByteBuf buf) {
        @SuppressWarnings("unchecked")
        ParticleType<T> type = (ParticleType<T>) buf.readById(BuiltInRegistries.PARTICLE_TYPE);
        return type.getDeserializer().fromNetwork(type, buf);
    }
}
