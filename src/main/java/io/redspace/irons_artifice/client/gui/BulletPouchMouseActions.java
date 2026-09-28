package io.redspace.irons_artifice.client.gui;

import io.redspace.irons_artifice.item.BulletPouchContents;
import io.redspace.irons_artifice.item.BulletPouchItem;
import io.redspace.irons_artifice.network.packets.ServerboundSelectPouchItemPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.joml.Vector2i;

public class BulletPouchMouseActions implements ItemSlotMouseAction {
    private final Minecraft minecraft;
    private final ScrollWheelHandler scrollWheelHandler = new ScrollWheelHandler();

    public BulletPouchMouseActions(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public boolean matches(Slot slot) {
        return slot.getItem().getItem() instanceof BulletPouchItem;
    }

    @Override
    public boolean onMouseScrolled(double scrollX, double scrollY, int slotIndex, ItemStack pouch) {
        int shown = BulletPouchItem.numberOfStacksToShow(pouch);
        if (shown == 0) {
            return false;
        }
        Vector2i wheel = scrollWheelHandler.onMouseScroll(scrollX, scrollY);
        int steps = wheel.y == 0 ? -wheel.x : wheel.y;
        if (steps != 0) {
            int current = BulletPouchItem.selectedIndex(pouch);
            int next = ScrollWheelHandler.getNextScrollWheelSelection(steps, current, shown);
            if (current != next) {
                select(pouch, slotIndex, next);
            }
        }
        return true;
    }

    @Override
    public void onStopHovering(Slot slot) {
        select(slot.getItem(), slot.index, BulletPouchContents.NO_SELECTION);
    }

    @Override
    public void onSlotClicked(Slot slot, ContainerInput input) {
        if (input == ContainerInput.QUICK_MOVE || input == ContainerInput.SWAP) {
            select(slot.getItem(), slot.index, BulletPouchContents.NO_SELECTION);
        }
    }

    private void select(ItemStack pouch, int slotIndex, int index) {
        if (minecraft.getConnection() == null || index >= BulletPouchItem.numberOfStacksToShow(pouch)) {
            return;
        }
        BulletPouchItem.toggleSelected(pouch, index);
        ClientPacketDistributor.sendToServer(new ServerboundSelectPouchItemPacket(slotIndex, index));
    }
}
