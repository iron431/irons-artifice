package io.redspace.irons_artifice.menu;

import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.modifier.ModifierItem;
import io.redspace.irons_artifice.utils.ModifierPatchHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class GunContainer extends SimpleContainer {
    private final ItemStack stack;

    public GunContainer(ItemStack stack) {
        super(sizeFromStack(stack));
        this.stack = stack;
        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        contents.copyInto(this.getItems());
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
        this.stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
        ModifierPatchHandler.rebuild(this.stack, this);
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
