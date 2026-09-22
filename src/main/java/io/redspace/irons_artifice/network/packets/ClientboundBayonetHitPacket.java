package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.ClientHelper;
import io.redspace.irons_artifice.item.BayonetLunge;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Tells the attacking player that their bayonet charge connected, so the client can give feedback
 * (hit sound + first person flinch, the 1.21.1 stand-in for vanilla's {@code ticks_since_last_kinetic_weapon_hit}).
 *
 * @see BayonetLunge
 */
public record ClientboundBayonetHitPacket() implements CustomPacketPayload {
    public static final ClientboundBayonetHitPacket INSTANCE = new ClientboundBayonetHitPacket();

    public static final Type<ClientboundBayonetHitPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(IronsArtifice.MODID, "bayonet_hit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBayonetHitPacket> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundBayonetHitPacket payload, IPayloadContext context) {
        ClientHelper.handleBayonetHitFeedback();
    }
}
