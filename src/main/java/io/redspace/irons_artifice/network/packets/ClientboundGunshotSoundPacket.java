package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.ClientHelper;
import io.redspace.irons_artifice.client.sounds.GunShotSoundSettings;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record ClientboundGunshotSoundPacket(SoundSource source, double x, double y, double z,
                                             List<GunShotSoundSettings> sounds)
        implements CustomPacketPayload {

    public static final Type<ClientboundGunshotSoundPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(IronsArtifice.MODID, "gunshot_sound"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundGunshotSoundPacket> STREAM_CODEC =
            StreamCodec.of(ClientboundGunshotSoundPacket::encode, ClientboundGunshotSoundPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, ClientboundGunshotSoundPacket msg) {
        buf.writeEnum(msg.source);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeVarInt(msg.sounds.size());
        for (GunShotSoundSettings sound : msg.sounds) {
            GunShotSoundSettings.STREAM_CODEC.encode(buf, sound);
        }
    }

    private static ClientboundGunshotSoundPacket decode(RegistryFriendlyByteBuf buf) {
        SoundSource source = buf.readEnum(SoundSource.class);
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        int size = buf.readVarInt();
        List<GunShotSoundSettings> sounds = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            sounds.add(GunShotSoundSettings.STREAM_CODEC.decode(buf));
        }
        return new ClientboundGunshotSoundPacket(source, x, y, z, sounds);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundGunshotSoundPacket payload, IPayloadContext context) {
        ClientHelper.handleGunshotSound(payload);
    }
}
