package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.ClientHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundEquipSoundPacket(SoundSource source, Item item)
        implements CustomPacketPayload {

    public static final Type<ClientboundEquipSoundPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(IronsArtifice.MODID, "equip_sound"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundEquipSoundPacket> STREAM_CODEC =
            StreamCodec.of(ClientboundEquipSoundPacket::encode, ClientboundEquipSoundPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, ClientboundEquipSoundPacket msg) {
        buf.writeEnum(msg.source);
        ByteBufCodecs.registry(Registries.ITEM).encode(buf, msg.item);
    }

    private static ClientboundEquipSoundPacket decode(RegistryFriendlyByteBuf buf) {
        return new ClientboundEquipSoundPacket(
                buf.readEnum(SoundSource.class),
                ByteBufCodecs.registry(Registries.ITEM).decode(buf)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundEquipSoundPacket payload, IPayloadContext context) {
        ClientHelper.handleEquipSound(payload);
    }
}
