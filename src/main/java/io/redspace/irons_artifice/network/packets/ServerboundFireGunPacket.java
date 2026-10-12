package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.item.FireOutcome;
import io.redspace.irons_artifice.item.GunplayManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerboundFireGunPacket(Vec3 direction) {

    public static void encode(ServerboundFireGunPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.direction.x);
        buf.writeDouble(msg.direction.y);
        buf.writeDouble(msg.direction.z);
    }

    public static ServerboundFireGunPacket decode(FriendlyByteBuf buf) {
        return new ServerboundFireGunPacket(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
    }

    public static void handle(ServerboundFireGunPacket payload, Supplier<NetworkEvent.Context> context) {
        ServerPlayer serverPlayer = context.get().getSender();
        if (serverPlayer != null) {
            FireOutcome outcome = GunplayManager.tryFire(serverPlayer, payload.direction());
            if (outcome == FireOutcome.FIRE_DELAY_ACTIVE) {
                // a shot that missed the gate by a tick or less is held, not thrown away
                GunplayManager.queueEarlyShot(serverPlayer, payload.direction());
            }
        }
    }
}
