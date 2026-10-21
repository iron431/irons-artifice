package io.redspace.irons_artifice.network.packets;

import io.redspace.irons_artifice.item.BulletContainerContents;
import io.redspace.irons_artifice.item.BulletContainerItem;
import io.redspace.irons_artifice.item.BulletContainerSelection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerboundSelectBulletContainerItemPacket(int slotIndex, int selectedIndex) {

    public static void encode(ServerboundSelectBulletContainerItemPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slotIndex);
        buf.writeVarInt(msg.selectedIndex);
    }

    public static ServerboundSelectBulletContainerItemPacket decode(FriendlyByteBuf buf) {
        return new ServerboundSelectBulletContainerItemPacket(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(ServerboundSelectBulletContainerItemPacket payload, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null) {
            return;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (payload.slotIndex < 0 || payload.slotIndex >= menu.slots.size()) {
            return;
        }
        ItemStack stack = menu.getSlot(payload.slotIndex).getItem();
        if (!(stack.getItem() instanceof BulletContainerItem)) {
            return;
        }
        int shown = BulletContainerItem.numberOfStacksToShow(stack);
        boolean clearing = payload.selectedIndex == BulletContainerContents.NO_SELECTION;
        boolean inRange = payload.selectedIndex >= 0 && payload.selectedIndex < shown;
        if (!clearing && !inRange) {
            return;
        }
        BulletContainerSelection.set(player, payload.selectedIndex);
    }
}
