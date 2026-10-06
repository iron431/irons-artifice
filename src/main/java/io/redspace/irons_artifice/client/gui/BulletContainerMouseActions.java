package io.redspace.irons_artifice.client.gui;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.BulletContainerContents;
import io.redspace.irons_artifice.item.BulletContainerItem;
import io.redspace.irons_artifice.network.packets.ServerboundSelectBulletContainerItemPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.function.Predicate;

@EventBusSubscriber(modid = IronsArtifice.MODID, value = Dist.CLIENT)
public final class BulletContainerMouseActions {
    private static double accumulatedScrollX;
    private static double accumulatedScrollY;
    @Nullable
    private static Slot hoveredSlot;

    @SubscribeEvent
    static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        Slot slot = screen.getSlotUnderMouse();
        if (matches(slot) && onMouseScrolled(event.getScrollDeltaX(), event.getScrollDeltaY(), slot.index, slot.getItem())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onScreenRendered(ScreenEvent.Render.Post event) {
        Slot previous = hoveredSlot;
        hoveredSlot = event.getScreen() instanceof AbstractContainerScreen<?> screen ? screen.getSlotUnderMouse() : null;
        if (previous != null && previous != hoveredSlot) {
            onStopHovering(previous);
        }
    }

    @SubscribeEvent
    static void onScreenClosing(ScreenEvent.Closing event) {
        if (hoveredSlot != null) {
            onStopHovering(hoveredSlot);
            hoveredSlot = null;
        }
    }

    @SubscribeEvent
    static void onMouseButtonPressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        Slot slot = screen.getSlotUnderMouse();
        if (!matches(slot) || !screen.getMenu().getCarried().isEmpty()) {
            return;
        }
        int button = event.getButton();
        if (button == 0 || button == 1) {
            if (Screen.hasShiftDown()) {
                onSlotClicked(slot, ClickType.QUICK_MOVE);
            }
        } else if (isHotbarKey(key -> key.matchesMouse(button))) {
            onSlotClicked(slot, ClickType.SWAP);
        }
    }

    @SubscribeEvent
    static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        Slot slot = screen.getSlotUnderMouse();
        if (matches(slot) && screen.getMenu().getCarried().isEmpty()
                && isHotbarKey(key -> key.matches(event.getKeyCode(), event.getScanCode()))) {
            onSlotClicked(slot, ClickType.SWAP);
        }
    }

    private static boolean matches(@Nullable Slot slot) {
        return slot != null && slot.getItem().getItem() instanceof BulletContainerItem;
    }

    private static boolean isHotbarKey(Predicate<KeyMapping> pressed) {
        Options options = Minecraft.getInstance().options;
        if (pressed.test(options.keySwapOffhand)) {
            return true;
        }
        for (KeyMapping hotbarKey : options.keyHotbarSlots) {
            if (pressed.test(hotbarKey)) {
                return true;
            }
        }
        return false;
    }

    private static boolean onMouseScrolled(double scrollX, double scrollY, int slotIndex, ItemStack pouch) {
        int shown = BulletContainerItem.numberOfStacksToShow(pouch);
        if (shown == 0) {
            return false;
        }
        int steps = accumulateScroll(scrollX, scrollY);
        if (steps != 0) {
            int current = BulletContainerItem.selectedIndex(pouch);
            int next = nextScrollWheelSelection(steps, current, shown);
            if (current != next) {
                select(pouch, slotIndex, next);
            }
        }
        return true;
    }

    private static int accumulateScroll(double scrollX, double scrollY) {
        if (accumulatedScrollX != 0 && Math.signum(scrollX) != Math.signum(accumulatedScrollX)) {
            accumulatedScrollX = 0;
        }
        if (accumulatedScrollY != 0 && Math.signum(scrollY) != Math.signum(accumulatedScrollY)) {
            accumulatedScrollY = 0;
        }
        accumulatedScrollX += scrollX;
        accumulatedScrollY += scrollY;
        int wholeX = (int) accumulatedScrollX;
        int wholeY = (int) accumulatedScrollY;
        accumulatedScrollX -= wholeX;
        accumulatedScrollY -= wholeY;
        return wholeY == 0 ? -wholeX : wholeY;
    }

    private static int nextScrollWheelSelection(int steps, int current, int shown) {
        int next = Math.max(-1, current - (int) Math.signum(steps));
        return Math.floorMod(next, shown);
    }

    private static void onStopHovering(Slot slot) {
        if (matches(slot)) {
            select(slot.getItem(), slot.index, BulletContainerContents.NO_SELECTION);
        }
    }

    private static void onSlotClicked(Slot slot, ClickType clickType) {
        if (clickType == ClickType.QUICK_MOVE || clickType == ClickType.SWAP) {
            select(slot.getItem(), slot.index, BulletContainerContents.NO_SELECTION);
        }
    }

    private static void select(ItemStack pouch, int slotIndex, int index) {
        if (Minecraft.getInstance().getConnection() == null || index >= BulletContainerItem.numberOfStacksToShow(pouch)) {
            return;
        }
        BulletContainerItem.toggleSelected(pouch, index);
        PacketDistributor.sendToServer(new ServerboundSelectBulletContainerItemPacket(slotIndex, index));
    }
}
