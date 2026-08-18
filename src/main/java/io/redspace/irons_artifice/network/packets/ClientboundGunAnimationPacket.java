package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.ClientHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundGunAnimationPacket(int entityId, long instanceId, InteractionHand interactionHand,
                                            String animName,
                                            double speed, double offsetSeconds,
                                            double skipAtSeconds, double skipToSeconds)
        implements CustomPacketPayload {

    public ClientboundGunAnimationPacket(int entityId, long instanceId, InteractionHand interactionHand,
                                         String animName, double speed, double offsetSeconds) {
        this(entityId, instanceId, interactionHand, animName, speed, offsetSeconds, 0, 0);
    }

    public static final Type<ClientboundGunAnimationPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(IronsArtifice.MODID, "gun_animation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundGunAnimationPacket> STREAM_CODEC =
            StreamCodec.of(ClientboundGunAnimationPacket::encode, ClientboundGunAnimationPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, ClientboundGunAnimationPacket msg) {
        buf.writeInt(msg.entityId);
        buf.writeLong(msg.instanceId);
        buf.writeBoolean(msg.interactionHand == InteractionHand.MAIN_HAND);
        buf.writeUtf(msg.animName);
        buf.writeDouble(msg.speed);
        buf.writeDouble(msg.offsetSeconds);
        buf.writeDouble(msg.skipAtSeconds);
        buf.writeDouble(msg.skipToSeconds);
    }

    private static ClientboundGunAnimationPacket decode(RegistryFriendlyByteBuf buf) {
        return new ClientboundGunAnimationPacket(buf.readInt(), buf.readLong(), buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, buf.readUtf(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundGunAnimationPacket payload, IPayloadContext context) {
        ClientHelper.handleGunAnimationPacket(payload);
    }
}
