package io.redspace.irons_artifice.menu;

import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.modifier.ModifierItem;
import io.redspace.irons_artifice.utils.ModifierPatchHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GunContainer extends SimpleContainer {
    private static final String ITEMS_TAG = "Items";
    private final ItemStack stack;

    public GunContainer(ItemStack stack) {
        super(loadItems(stack));
        this.stack = stack;
    }

    private static ItemStack[] loadItems(ItemStack stack) {
        NonNullList<ItemStack> items = NonNullList.withSize(sizeFromStack(stack), ItemStack.EMPTY);
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            ContainerHelper.loadAllItems(tag, items);
        }
        return items.toArray(ItemStack[]::new);
    }

    private static int sizeFromStack(ItemStack stack) {
        if (!(stack.getItem() instanceof GunItem gunItem)) {
            return 0;
        }
        return gunItem.getGun().modifierSlots();
    }

    public ItemStack getGunStack() {
        return this.stack;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.stack.removeTagKey(ITEMS_TAG);
        if (!this.isEmpty()) {
            ContainerHelper.saveAllItems(this.stack.getOrCreateTag(), items());
        }
        ModifierPatchHandler.rebuild(this.stack, this);
    }

    private NonNullList<ItemStack> items() {
        NonNullList<ItemStack> items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < items.size(); slot++) {
            items.set(slot, this.getItem(slot));
        }
        return items;
    }

    @Override
    public boolean stillValid(Player player) {
        return !player.isSpectator()
                // fixme: must become handed
                && (player.getMainHandItem() == this.stack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof ModifierItem;
    }
}
