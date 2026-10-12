package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.data.ParticleBurst;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record MuzzleFlashVisuals(
        Optional<ParticleOptions> flash,
        List<ParticleBurst> airBursts,
        List<ParticleBurst> underwaterBursts
) {
    public static void write(FriendlyByteBuf buf, MuzzleFlashVisuals visuals) {
        buf.writeBoolean(visuals.flash.isPresent());
        visuals.flash.ifPresent(particle -> ParticleBurst.writeParticle(buf, particle));
        writeBursts(buf, visuals.airBursts);
        writeBursts(buf, visuals.underwaterBursts);
    }

    public static MuzzleFlashVisuals read(FriendlyByteBuf buf) {
        Optional<ParticleOptions> flash = buf.readBoolean()
                ? Optional.of(ParticleBurst.readParticle(buf))
                : Optional.empty();
        return new MuzzleFlashVisuals(flash, readBursts(buf), readBursts(buf));
    }

    private static void writeBursts(FriendlyByteBuf buf, List<ParticleBurst> bursts) {
        buf.writeVarInt(bursts.size());
        for (ParticleBurst burst : bursts) {
            ParticleBurst.write(buf, burst);
        }
    }

    private static List<ParticleBurst> readBursts(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ParticleBurst> bursts = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            bursts.add(ParticleBurst.read(buf));
        }
        return bursts;
    }
}
