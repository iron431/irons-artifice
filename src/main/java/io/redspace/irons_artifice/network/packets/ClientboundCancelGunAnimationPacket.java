package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.ClientHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundCancelGunAnimationPacket(int entityId, long instanceId, InteractionHand interactionHand)
        implements CustomPacketPayload {

    public static final Type<ClientboundCancelGunAnimationPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(IronsArtifice.MODID, "cancel_gun_animation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCancelGunAnimationPacket> STREAM_CODEC =
            StreamCodec.of(ClientboundCancelGunAnimationPacket::encode, ClientboundCancelGunAnimationPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, ClientboundCancelGunAnimationPacket msg) {
        buf.writeInt(msg.entityId);
        buf.writeLong(msg.instanceId);
        buf.writeBoolean(msg.interactionHand == InteractionHand.MAIN_HAND);
    }

    private static ClientboundCancelGunAnimationPacket decode(RegistryFriendlyByteBuf buf) {
        return new ClientboundCancelGunAnimationPacket(
                buf.readInt(),
                buf.readLong(),
                buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundCancelGunAnimationPacket payload, IPayloadContext context) {
        ClientHelper.handleCancelGunAnimationPacket(payload);
    }
}
