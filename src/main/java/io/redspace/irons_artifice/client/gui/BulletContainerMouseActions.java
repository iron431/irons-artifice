package io.redspace.irons_artifice.client.gui;

import io.redspace.irons_artifice.item.BulletContainerContents;
import io.redspace.irons_artifice.item.BulletContainerItem;
import io.redspace.irons_artifice.network.packets.ServerboundSelectBulletContainerItemPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.joml.Vector2i;

public class BulletContainerMouseActions implements ItemSlotMouseAction {
    private final Minecraft minecraft;
    private final ScrollWheelHandler scrollWheelHandler = new ScrollWheelHandler();

    public BulletContainerMouseActions(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public boolean matches(Slot slot) {
        return slot.getItem().getItem() instanceof BulletContainerItem;
    }

    @Override
    public boolean onMouseScrolled(double scrollX, double scrollY, int slotIndex, ItemStack pouch) {
        int shown = BulletContainerItem.numberOfStacksToShow(pouch);
        if (shown == 0) {
            return false;
        }
        Vector2i wheel = scrollWheelHandler.onMouseScroll(scrollX, scrollY);
        int steps = wheel.y == 0 ? -wheel.x : wheel.y;
        if (steps != 0) {
            int current = BulletContainerItem.selectedIndex(pouch);
            int next = ScrollWheelHandler.getNextScrollWheelSelection(steps, current, shown);
            if (current != next) {
                select(pouch, slotIndex, next);
            }
        }
        return true;
    }

    @Override
    public void onStopHovering(Slot slot) {
        select(slot.getItem(), slot.index, BulletContainerContents.NO_SELECTION);
    }

    @Override
    public void onSlotClicked(Slot slot, ContainerInput input) {
        if (input == ContainerInput.QUICK_MOVE || input == ContainerInput.SWAP) {
            select(slot.getItem(), slot.index, BulletContainerContents.NO_SELECTION);
        }
    }

    private void select(ItemStack pouch, int slotIndex, int index) {
        if (minecraft.getConnection() == null || index >= BulletContainerItem.numberOfStacksToShow(pouch)) {
            return;
        }
        BulletContainerItem.toggleSelected(pouch, index);
        ClientPacketDistributor.sendToServer(new ServerboundSelectBulletContainerItemPacket(slotIndex, index));
    }
}
