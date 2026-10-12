package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.client.ClientHelper;
import io.redspace.irons_artifice.data.ParticleStack;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record ClientboundBulletTrailPacket(Vec3 from, Vec3 to, List<ParticleOptions> particles,
                                           List<ParticleStack.ParticleAccent> accents) {

    public static void encode(ClientboundBulletTrailPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.from.x);
        buf.writeDouble(msg.from.y);
        buf.writeDouble(msg.from.z);
        buf.writeDouble(msg.to.x);
        buf.writeDouble(msg.to.y);
        buf.writeDouble(msg.to.z);
        buf.writeVarInt(msg.particles.size());
        for (ParticleOptions particle : msg.particles) {
            writeParticle(buf, particle);
        }
        buf.writeVarInt(msg.accents.size());
        for (ParticleStack.ParticleAccent accent : msg.accents) {
            writeParticle(buf, accent.options());
            buf.writeDouble(accent.chance());
        }
    }

    public static ClientboundBulletTrailPacket decode(FriendlyByteBuf buf) {
        Vec3 from = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        Vec3 to = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        int size = buf.readVarInt();
        List<ParticleOptions> particles = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            particles.add(readParticle(buf));
        }
        size = buf.readVarInt();
        List<ParticleStack.ParticleAccent> accents = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            accents.add(new ParticleStack.ParticleAccent(
                    readParticle(buf),
                    buf.readDouble()
            ));
        }
        return new ClientboundBulletTrailPacket(from, to, particles, accents);
    }

    private static void writeParticle(FriendlyByteBuf buf, ParticleOptions particle) {
        buf.writeId(BuiltInRegistries.PARTICLE_TYPE, particle.getType());
        particle.writeToNetwork(buf);
    }

    private static <T extends ParticleOptions> T readParticle(FriendlyByteBuf buf) {
        @SuppressWarnings("unchecked")
        ParticleType<T> type = (ParticleType<T>) buf.readById(BuiltInRegistries.PARTICLE_TYPE);
        return type.getDeserializer().fromNetwork(type, buf);
    }

    public static void handle(ClientboundBulletTrailPacket payload, Supplier<NetworkEvent.Context> context) {
        ClientHelper.handleBulletTrail(payload);
    }
}
