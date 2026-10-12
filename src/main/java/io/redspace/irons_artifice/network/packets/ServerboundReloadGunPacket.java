package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerboundReloadGunPacket() {
    public static final ServerboundReloadGunPacket INSTANCE = new ServerboundReloadGunPacket();

    public static void encode(ServerboundReloadGunPacket msg, FriendlyByteBuf buf) {
    }

    public static ServerboundReloadGunPacket decode(FriendlyByteBuf buf) {
        return INSTANCE;
    }

    public static void handle(ServerboundReloadGunPacket payload, Supplier<NetworkEvent.Context> context) {
        ServerPlayer serverPlayer = context.get().getSender();
        if (serverPlayer == null) {
            return;
        }
        ItemStack held = serverPlayer.getMainHandItem();
        if (!(held.getItem() instanceof GunItem) || GunItem.isReloading(held)) {
            return;
        }
        GunItem.playReloadFeedback(serverPlayer.level(), serverPlayer, GunplayManager.attemptStartReload(serverPlayer, held));
    }
}
