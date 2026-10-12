package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.menu.GunContainer;
import io.redspace.irons_artifice.menu.GunModifierMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerboundOpenModifierMenuPacket() {
    public static final ServerboundOpenModifierMenuPacket INSTANCE = new ServerboundOpenModifierMenuPacket();

    public static void encode(ServerboundOpenModifierMenuPacket msg, FriendlyByteBuf buf) {
    }

    public static ServerboundOpenModifierMenuPacket decode(FriendlyByteBuf buf) {
        return INSTANCE;
    }

    public static void handle(ServerboundOpenModifierMenuPacket payload, Supplier<NetworkEvent.Context> context) {
        ServerPlayer serverPlayer = context.get().getSender();
        if (serverPlayer == null) {
            return;
        }
        ItemStack held = serverPlayer.getMainHandItem();
        if (!(held.getItem() instanceof GunItem)) {
            return;
        }
        serverPlayer.openMenu(new SimpleMenuProvider(
                (id, inventory, player) -> new GunModifierMenu(id, inventory, new GunContainer(held)),
                Component.translatable("container.irons_artifice.gun")
        ));
    }
}
