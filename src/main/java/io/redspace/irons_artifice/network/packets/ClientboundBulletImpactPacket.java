package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.client.ClientHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundBulletImpactPacket(Vec3 position, Vec3 deltaMovement, Vec3 normal, double damage) {

    public static void encode(ClientboundBulletImpactPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.position.x);
        buf.writeDouble(msg.position.y);
        buf.writeDouble(msg.position.z);
        buf.writeDouble(msg.deltaMovement.x);
        buf.writeDouble(msg.deltaMovement.y);
        buf.writeDouble(msg.deltaMovement.z);
        buf.writeDouble(msg.normal.x);
        buf.writeDouble(msg.normal.y);
        buf.writeDouble(msg.normal.z);
        buf.writeDouble(msg.damage);
    }

    public static ClientboundBulletImpactPacket decode(FriendlyByteBuf buf) {
        Vec3 pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        Vec3 deltaMovement = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        Vec3 normal = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        return new ClientboundBulletImpactPacket(pos, deltaMovement, normal, buf.readDouble());
    }

    public static void handle(ClientboundBulletImpactPacket payload, Supplier<NetworkEvent.Context> context) {
        ClientHelper.handleBulletImpact(payload);
    }
}
