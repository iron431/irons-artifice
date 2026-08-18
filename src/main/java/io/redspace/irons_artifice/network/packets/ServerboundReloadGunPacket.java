package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundReloadGunPacket() implements CustomPacketPayload {
    public static final ServerboundReloadGunPacket INSTANCE = new ServerboundReloadGunPacket();

    public static final Type<ServerboundReloadGunPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(IronsArtifice.MODID, "reload_gun"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundReloadGunPacket> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundReloadGunPacket payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack held = serverPlayer.getMainHandItem();
        if (!(held.getItem() instanceof GunItem) || GunItem.isReloading(held)) {
            return;
        }
        GunItem.playReloadFeedback(serverPlayer.level(), serverPlayer, GunplayManager.attemptStartReload(serverPlayer, held));
    }
}
