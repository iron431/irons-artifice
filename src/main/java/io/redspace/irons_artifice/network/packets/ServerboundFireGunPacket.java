package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.FireOutcome;
import io.redspace.irons_artifice.item.GunplayManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundFireGunPacket(Vec3 direction)
        implements CustomPacketPayload {

    public static final Type<ServerboundFireGunPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(IronsArtifice.MODID, "fire_gun"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundFireGunPacket> STREAM_CODEC =
            StreamCodec.of(ServerboundFireGunPacket::encode, ServerboundFireGunPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, ServerboundFireGunPacket msg) {
        buf.writeDouble(msg.direction.x);
        buf.writeDouble(msg.direction.y);
        buf.writeDouble(msg.direction.z);
    }

    private static ServerboundFireGunPacket decode(RegistryFriendlyByteBuf buf) {
        return new ServerboundFireGunPacket(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundFireGunPacket payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            FireOutcome outcome = GunplayManager.tryFire(serverPlayer, payload.direction());
            if (outcome == FireOutcome.FIRE_DELAY_ACTIVE) {
                // a shot that missed the gate by a tick or less is held, not thrown away
                GunplayManager.queueEarlyShot(serverPlayer, payload.direction());
            }
        }
    }
}
