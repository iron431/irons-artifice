package io.redspace.irons_artifice.network;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.network.packets.ClientboundBulletImpactPacket;
import io.redspace.irons_artifice.network.packets.ClientboundBulletTrailPacket;
import io.redspace.irons_artifice.network.packets.ClientboundCancelGunAnimationPacket;
import io.redspace.irons_artifice.network.packets.ClientboundEquipSoundPacket;
import io.redspace.irons_artifice.network.packets.ClientboundGunAnimationPacket;
import io.redspace.irons_artifice.network.packets.ClientboundGunshotSoundPacket;
import io.redspace.irons_artifice.network.packets.ClientboundLocalSoundPacket;
import io.redspace.irons_artifice.network.packets.ClientboundMuzzleFlashPacket;
import io.redspace.irons_artifice.network.packets.ServerboundFireGunPacket;
import io.redspace.irons_artifice.network.packets.ServerboundOpenModifierMenuPacket;
import io.redspace.irons_artifice.network.packets.ServerboundReloadGunPacket;
import io.redspace.irons_artifice.network.packets.ServerboundSelectBulletContainerItemPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.Nullable;

public final class PayloadRegistry {
    private static final String VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            IronsArtifice.id("main"),
            () -> VERSION,
            VERSION::equals,
            VERSION::equals
    );

    public static void register() {
        int id = 0;

        CHANNEL.messageBuilder(ServerboundFireGunPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundFireGunPacket::encode)
                .decoder(ServerboundFireGunPacket::decode)
                .consumerMainThread(ServerboundFireGunPacket::handle)
                .add();
        CHANNEL.messageBuilder(ServerboundOpenModifierMenuPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundOpenModifierMenuPacket::encode)
                .decoder(ServerboundOpenModifierMenuPacket::decode)
                .consumerMainThread(ServerboundOpenModifierMenuPacket::handle)
                .add();
        CHANNEL.messageBuilder(ServerboundReloadGunPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundReloadGunPacket::encode)
                .decoder(ServerboundReloadGunPacket::decode)
                .consumerMainThread(ServerboundReloadGunPacket::handle)
                .add();
        CHANNEL.messageBuilder(ServerboundSelectBulletContainerItemPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundSelectBulletContainerItemPacket::encode)
                .decoder(ServerboundSelectBulletContainerItemPacket::decode)
                .consumerMainThread(ServerboundSelectBulletContainerItemPacket::handle)
                .add();

        CHANNEL.messageBuilder(ClientboundBulletTrailPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundBulletTrailPacket::encode)
                .decoder(ClientboundBulletTrailPacket::decode)
                .consumerMainThread(ClientboundBulletTrailPacket::handle)
                .add();
        CHANNEL.messageBuilder(ClientboundBulletImpactPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundBulletImpactPacket::encode)
                .decoder(ClientboundBulletImpactPacket::decode)
                .consumerMainThread(ClientboundBulletImpactPacket::handle)
                .add();
        CHANNEL.messageBuilder(ClientboundMuzzleFlashPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundMuzzleFlashPacket::encode)
                .decoder(ClientboundMuzzleFlashPacket::decode)
                .consumerMainThread(ClientboundMuzzleFlashPacket::handle)
                .add();
        CHANNEL.messageBuilder(ClientboundGunAnimationPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundGunAnimationPacket::encode)
                .decoder(ClientboundGunAnimationPacket::decode)
                .consumerMainThread(ClientboundGunAnimationPacket::handle)
                .add();
        CHANNEL.messageBuilder(ClientboundCancelGunAnimationPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundCancelGunAnimationPacket::encode)
                .decoder(ClientboundCancelGunAnimationPacket::decode)
                .consumerMainThread(ClientboundCancelGunAnimationPacket::handle)
                .add();
        CHANNEL.messageBuilder(ClientboundGunshotSoundPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundGunshotSoundPacket::encode)
                .decoder(ClientboundGunshotSoundPacket::decode)
                .consumerMainThread(ClientboundGunshotSoundPacket::handle)
                .add();
        CHANNEL.messageBuilder(ClientboundLocalSoundPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundLocalSoundPacket::encode)
                .decoder(ClientboundLocalSoundPacket::decode)
                .consumerMainThread(ClientboundLocalSoundPacket::handle)
                .add();
        CHANNEL.messageBuilder(ClientboundEquipSoundPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundEquipSoundPacket::encode)
                .decoder(ClientboundEquipSoundPacket::decode)
                .consumerMainThread(ClientboundEquipSoundPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(ServerPlayer player, MSG message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToPlayersTrackingEntity(Entity entity, MSG message) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }

    public static <MSG> void sendToPlayersTrackingEntityAndSelf(Entity entity, MSG message) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }

    public static <MSG> void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos, MSG message) {
        CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunk(chunkPos.x, chunkPos.z)), message);
    }

    public static <MSG> void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer excluded, double x, double y, double z, double radius, MSG message) {
        CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(excluded, x, y, z, radius, level.dimension())), message);
    }
}
