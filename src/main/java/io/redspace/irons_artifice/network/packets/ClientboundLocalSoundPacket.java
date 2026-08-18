package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.ClientHelper;
import io.redspace.irons_artifice.data.PlayableSound;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundLocalSoundPacket(SoundSource source, PlayableSound sound)
        implements CustomPacketPayload {

    public static final Type<ClientboundLocalSoundPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(IronsArtifice.MODID, "playable_sound"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLocalSoundPacket> STREAM_CODEC =
            StreamCodec.of(ClientboundLocalSoundPacket::encode, ClientboundLocalSoundPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, ClientboundLocalSoundPacket msg) {
        buf.writeEnum(msg.source);
        PlayableSound.STREAM_CODEC.encode(buf, msg.sound);
    }

    private static ClientboundLocalSoundPacket decode(RegistryFriendlyByteBuf buf) {
        return new ClientboundLocalSoundPacket(
                buf.readEnum(SoundSource.class),
                PlayableSound.STREAM_CODEC.decode(buf)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundLocalSoundPacket payload, IPayloadContext context) {
        ClientHelper.handleLocalSound(payload);
    }
}
