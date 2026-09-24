package io.redspace.irons_artifice.menu;

import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.modifier.ModifierItem;
import io.redspace.irons_artifice.utils.ModifierPatchHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.function.Consumer;

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

    /**
     * Visits what is installed in the gun, in slot order, without opening a container. Anything sitting beyond the gun's modifier slots is
     * not installed, however it got there.
     */
    public static void forEachInstalled(ItemStack gunStack, Consumer<ItemStackTemplate> consumer) {
        ItemContainerContents contents = gunStack.get(DataComponents.CONTAINER);
        if (contents == null) {
            return;
        }
        int slots = sizeFromStack(gunStack);
        if (contents.getSlots() <= slots) {
            // resolving a shot happens every tick and every frame, and templates can be read without creating a stack per slot
            contents.nonEmptyItems().forEach(consumer);
            return;
        }
        for (int slot = 0; slot < slots; slot++) {
            ItemStack installed = contents.getStackInSlot(slot);
            if (!installed.isEmpty()) {
                consumer.accept(ItemStackTemplate.fromNonEmptyStack(installed));
            }
        }
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
