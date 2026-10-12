package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.client.ClientHelper;
import io.redspace.irons_artifice.client.sounds.GunShotSoundSettings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record ClientboundGunshotSoundPacket(SoundSource source, double x, double y, double z,
                                             List<GunShotSoundSettings> sounds) {

    public static void encode(ClientboundGunshotSoundPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.source);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeVarInt(msg.sounds.size());
        for (GunShotSoundSettings sound : msg.sounds) {
            GunShotSoundSettings.write(buf, sound);
        }
    }

    public static ClientboundGunshotSoundPacket decode(FriendlyByteBuf buf) {
        SoundSource source = buf.readEnum(SoundSource.class);
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        int size = buf.readVarInt();
        List<GunShotSoundSettings> sounds = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            sounds.add(GunShotSoundSettings.read(buf));
        }
        return new ClientboundGunshotSoundPacket(source, x, y, z, sounds);
    }

    public static void handle(ClientboundGunshotSoundPacket payload, Supplier<NetworkEvent.Context> context) {
        ClientHelper.handleGunshotSound(payload);
    }
}
