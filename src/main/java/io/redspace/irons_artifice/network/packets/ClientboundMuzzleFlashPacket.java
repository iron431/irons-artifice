package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.client.ClientHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundMuzzleFlashPacket(
        MuzzleFlashVisuals visuals,
        int entityId,
        Vec3 entityMotion,
        float extraForwardOffset,
        Vec3 backupPos
) {

    public static void encode(ClientboundMuzzleFlashPacket msg, FriendlyByteBuf buf) {
        MuzzleFlashVisuals.write(buf, msg.visuals);
        buf.writeVarInt(msg.entityId);
        writeVec3(buf, msg.entityMotion);
        buf.writeFloat(msg.extraForwardOffset);
        writeVec3(buf, msg.backupPos);
    }

    public static ClientboundMuzzleFlashPacket decode(FriendlyByteBuf buf) {
        return new ClientboundMuzzleFlashPacket(
                MuzzleFlashVisuals.read(buf),
                buf.readVarInt(),
                readVec3(buf),
                buf.readFloat(),
                readVec3(buf)
        );
    }

    private static void writeVec3(FriendlyByteBuf buf, Vec3 value) {
        buf.writeDouble(value.x);
        buf.writeDouble(value.y);
        buf.writeDouble(value.z);
    }

    private static Vec3 readVec3(FriendlyByteBuf buf) {
        return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(ClientboundMuzzleFlashPacket payload, Supplier<NetworkEvent.Context> context) {
        ClientHelper.handleMuzzleFlash(payload);
    }
}
