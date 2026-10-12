package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.client.ClientHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundGunAnimationPacket(int entityId, long instanceId, InteractionHand interactionHand,
                                            String animName,
                                            double speed, double offsetSeconds,
                                            double skipAtSeconds, double skipToSeconds) {

    public ClientboundGunAnimationPacket(int entityId, long instanceId, InteractionHand interactionHand,
                                         String animName, double speed, double offsetSeconds) {
        this(entityId, instanceId, interactionHand, animName, speed, offsetSeconds, 0, 0);
    }

    public static void encode(ClientboundGunAnimationPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeLong(msg.instanceId);
        buf.writeBoolean(msg.interactionHand == InteractionHand.MAIN_HAND);
        buf.writeUtf(msg.animName);
        buf.writeDouble(msg.speed);
        buf.writeDouble(msg.offsetSeconds);
        buf.writeDouble(msg.skipAtSeconds);
        buf.writeDouble(msg.skipToSeconds);
    }

    public static ClientboundGunAnimationPacket decode(FriendlyByteBuf buf) {
        return new ClientboundGunAnimationPacket(buf.readInt(), buf.readLong(), buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, buf.readUtf(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(ClientboundGunAnimationPacket payload, Supplier<NetworkEvent.Context> context) {
        ClientHelper.handleGunAnimationPacket(payload);
    }
}
