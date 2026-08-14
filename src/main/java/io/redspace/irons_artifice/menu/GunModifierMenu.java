package io.redspace.irons_artifice.menu;

import io.redspace.irons_artifice.registry.MenuRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GunModifierMenu extends AbstractContainerMenu {
    public static final int SLOT_SIZE = 24;
    private final Container gunInventory;
    private final int size;
    public final ItemStack gunstack;

    public List<Slot> getModifierSlots() {
        return modifierSlots;
    }

    private final List<Slot> modifierSlots;

    public GunModifierMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new GunContainer(playerInventory.getSelectedItem()));
    }

    public GunModifierMenu(int containerId, Inventory playerInventory, Container gunInventory) {
        super(MenuRegistry.GUN_MENU.get(), containerId);
        this.gunInventory = gunInventory;
        this.modifierSlots = new ArrayList<>();
        gunInventory.startOpen(playerInventory.player);
        this.size = gunInventory.getContainerSize();
        // fixme: mainhand only
        this.gunstack = playerInventory.getSelectedItem();

        int maxPerRow = 8;
        if (size % maxPerRow == 1) {
            // prevent awkward single slot row
            maxPerRow--;
        }
        int rows = (size - 1) / maxPerRow + 1;
        int centerX = 176 / 2;
        int top = 84 - SLOT_SIZE * rows;
        int margin = (SLOT_SIZE - 16) / 2;
        for (int i = 0; i < size; i++) {
            int row = i / maxPerRow;
            int col = i % maxPerRow;
            int slotsInRow = Math.min(maxPerRow, size - row * maxPerRow);
            int rowLeft = centerX - (slotsInRow * SLOT_SIZE) / 2 + margin;
            modifierSlots.add(this.addSlot(new Slot(gunInventory, i,
                    rowLeft + col * SLOT_SIZE,
                    top + row * SLOT_SIZE) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return gunInventory.canPlaceItem(this.index, stack);
                }
            }));
        }

        this.addStandardInventorySlots(playerInventory, 8, 101);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.gunInventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (slotIndex < size) {
                if (!this.moveItemStackTo(stack, size, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, size, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.gunInventory.stopOpen(player);
    }
}
