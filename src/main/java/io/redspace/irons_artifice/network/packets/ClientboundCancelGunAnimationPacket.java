package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.client.ClientHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundCancelGunAnimationPacket(int entityId, long instanceId, InteractionHand interactionHand) {

    public static void encode(ClientboundCancelGunAnimationPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeLong(msg.instanceId);
        buf.writeBoolean(msg.interactionHand == InteractionHand.MAIN_HAND);
    }

    public static ClientboundCancelGunAnimationPacket decode(FriendlyByteBuf buf) {
        return new ClientboundCancelGunAnimationPacket(
                buf.readInt(),
                buf.readLong(),
                buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND
        );
    }

    public static void handle(ClientboundCancelGunAnimationPacket payload, Supplier<NetworkEvent.Context> context) {
        ClientHelper.handleCancelGunAnimationPacket(payload);
    }
}
