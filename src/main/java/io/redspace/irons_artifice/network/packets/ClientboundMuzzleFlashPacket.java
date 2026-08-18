package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.ClientHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundMuzzleFlashPacket(
        ParticleOptions particle,
        int entityId,
        Vec3 entityMotion,
        Vec3 position,
        Vec3 offset
) implements CustomPacketPayload {

    public static final Type<ClientboundMuzzleFlashPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(IronsArtifice.MODID, "muzzle_flash"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundMuzzleFlashPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ParticleTypes.STREAM_CODEC,
                    ClientboundMuzzleFlashPacket::particle,
                    ByteBufCodecs.VAR_INT,
                    ClientboundMuzzleFlashPacket::entityId,
                    Vec3.STREAM_CODEC,
                    ClientboundMuzzleFlashPacket::entityMotion,
                    Vec3.STREAM_CODEC,
                    ClientboundMuzzleFlashPacket::position,
                    Vec3.STREAM_CODEC,
                    ClientboundMuzzleFlashPacket::offset,
                    ClientboundMuzzleFlashPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundMuzzleFlashPacket payload, IPayloadContext context) {
        ClientHelper.handleMuzzleFlash(payload);
    }
}
