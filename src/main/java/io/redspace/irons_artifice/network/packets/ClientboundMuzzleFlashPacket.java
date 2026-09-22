package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.ClientHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundMuzzleFlashPacket(
        MuzzleFlashVisuals visuals,
        int entityId,
        Vec3 entityMotion,
        float extraForwardOffset,
        Vec3 backupPos
) implements CustomPacketPayload {

    public static final Type<ClientboundMuzzleFlashPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(IronsArtifice.MODID, "muzzle_flash"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundMuzzleFlashPacket> STREAM_CODEC =
            StreamCodec.composite(
                    MuzzleFlashVisuals.STREAM_CODEC,
                    ClientboundMuzzleFlashPacket::visuals,
                    ByteBufCodecs.VAR_INT,
                    ClientboundMuzzleFlashPacket::entityId,
                    ByteBufCodecs.fromCodecWithRegistries(Vec3.CODEC),
                    ClientboundMuzzleFlashPacket::entityMotion,
                    ByteBufCodecs.FLOAT,
                    ClientboundMuzzleFlashPacket::extraForwardOffset,
                    ByteBufCodecs.fromCodecWithRegistries(Vec3.CODEC),
                    ClientboundMuzzleFlashPacket::backupPos,
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
