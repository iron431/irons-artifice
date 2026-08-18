package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.ClientHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundBulletImpactPacket(Vec3 position, Vec3 deltaMovement, Vec3 normal, double damage)
        implements CustomPacketPayload {

    public static final Type<ClientboundBulletImpactPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(IronsArtifice.MODID, "bullet_impact"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBulletImpactPacket> STREAM_CODEC =
            StreamCodec.of(ClientboundBulletImpactPacket::encode, ClientboundBulletImpactPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, ClientboundBulletImpactPacket msg) {
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

    private static ClientboundBulletImpactPacket decode(RegistryFriendlyByteBuf buf) {
        Vec3 pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        Vec3 deltaMovement = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        Vec3 normal = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        return new ClientboundBulletImpactPacket(pos, deltaMovement, normal, buf.readDouble());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundBulletImpactPacket payload, IPayloadContext context) {
        ClientHelper.handleBulletImpact(payload);
    }
}
