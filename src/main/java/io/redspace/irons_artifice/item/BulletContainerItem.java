package io.redspace.irons_artifice.item;

import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.Consumer;

public class BulletContainerItem extends Item {
    private static final BulletContainerContents MISSING_CONTENTS = BulletContainerContents.empty(0);

    public BulletContainerItem(Properties properties, int capacity) {
        super(properties
                .stacksTo(1)
                .component(DataComponentRegistry.BULLET_POUCH, BulletContainerContents.empty(capacity))
        );
    }

    public static BulletContainerContents contents(ItemStack pouch) {
        return pouch.getOrDefault(DataComponentRegistry.BULLET_POUCH.get(), MISSING_CONTENTS);
    }

    public static int count(ItemStack pouch) {
        return contents(pouch).count();
    }

    public static int numberOfStacksToShow(ItemStack pouch) {
        return contents(pouch).numberOfStacksToShow();
    }

    public static int selectedIndex(ItemStack pouch) {
        return contents(pouch).selectedIndex();
    }

    private static void write(ItemStack pouch, BulletContainerContents.Mutable mutable) {
        pouch.set(DataComponentRegistry.BULLET_POUCH.get(), mutable.toImmutable());
    }

    public static int storeInPouches(Inventory inventory, ItemStack source) {
        int moved = 0;
        for (int i = 0; i < inventory.getContainerSize() && !source.isEmpty(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof BulletContainerItem && stack.getCount() == 1) {
                BulletContainerContents.Mutable contents = new BulletContainerContents.Mutable(contents(stack));
                moved += contents.insert(source);
                write(stack, contents);
            }
        }
        return moved;
    }

    public static int drain(ItemStack pouch, int amount) {
        if (pouch.getCount() != 1) {
            return 0;
        }
        BulletContainerContents.Mutable contents = new BulletContainerContents.Mutable(contents(pouch));
        int removed = contents.drain(amount);
        write(pouch, contents);
        return removed;
    }

    public static void toggleSelected(ItemStack pouch, int index) {
        BulletContainerContents.Mutable contents = new BulletContainerContents.Mutable(contents(pouch));
        contents.toggleSelected(index);
        write(pouch, contents);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack pouch, Slot slot, ClickAction action, Player player) {
        if (pouch.getCount() != 1) {
            return false;
        }
        ItemStack other = slot.getItem();
        BulletContainerContents.Mutable contents = new BulletContainerContents.Mutable(contents(pouch));
        if (action == ClickAction.PRIMARY && BulletContainerContents.accepts(other)) {
            int room = contents.room();
            ItemStack taken = room > 0 ? slot.safeTake(other.getCount(), room, player) : ItemStack.EMPTY;
            boolean inserted = contents.insert(taken) > 0;
            write(pouch, contents);
            playSound(player, inserted ? SoundEvents.BUNDLE_INSERT : SoundEvents.BUNDLE_INSERT_FAIL);
            return true;
        }
        if (action == ClickAction.SECONDARY && other.isEmpty()) {
            ItemStack removed = contents.removeOne();
            if (removed.isEmpty()) {
                return false;
            }
            ItemStack remainder = slot.safeInsert(removed);
            if (remainder.isEmpty()) {
                playSound(player, SoundEvents.BUNDLE_REMOVE_ONE);
            } else {
                contents.putBack(remainder);
            }
            write(pouch, contents);
            return true;
        }
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack pouch, ItemStack carried, Slot slot, ClickAction action, Player player, SlotAccess carriedAccess) {
        if (pouch.getCount() != 1) {
            return false;
        }
        BulletContainerContents.Mutable contents = new BulletContainerContents.Mutable(contents(pouch));
        if (action == ClickAction.PRIMARY && BulletContainerContents.accepts(carried)) {
            boolean inserted = slot.allowModification(player) && contents.insert(carried) > 0;
            write(pouch, contents);
            playSound(player, inserted ? SoundEvents.BUNDLE_INSERT : SoundEvents.BUNDLE_INSERT_FAIL);
            return true;
        }
        if (action == ClickAction.SECONDARY && carried.isEmpty() && slot.allowModification(player)) {
            ItemStack removed = contents.removeOne();
            if (removed.isEmpty()) {
                return false;
            }
            write(pouch, contents);
            carriedAccess.set(removed);
            playSound(player, SoundEvents.BUNDLE_REMOVE_ONE);
            return true;
        }
        toggleSelected(pouch, BulletContainerContents.NO_SELECTION);
        return false;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack pouch = player.getItemInHand(hand);
        if (pouch.getCount() != 1) {
            return InteractionResult.PASS;
        }
        BulletContainerContents.Mutable contents = new BulletContainerContents.Mutable(contents(pouch));
        ItemStack removed = contents.removeOne();
        if (removed.isEmpty()) {
            return InteractionResult.PASS;
        }
        write(pouch, contents);
        if (!level.isClientSide()) {
            player.getInventory().placeItemBackInInventory(removed);
        }
        playSound(player, SoundEvents.BUNDLE_REMOVE_ONE);
        return InteractionResult.SUCCESS;
    }

    private static void playSound(Player player, SoundEvent sound) {
        player.playSound(sound, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }

    @Override
    public boolean isBarVisible(ItemStack pouch) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack pouch) {
        BulletContainerContents contents = contents(pouch);
        if (contents.capacity() <= 0) {
            return 0;
        }
        return Mth.clamp(Math.round(13f * contents.count() / contents.capacity()), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack pouch) {
        return GunItem.AMMO_BAR_COLOR;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public void appendHoverText(ItemStack pouch, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(pouch, context, display, builder, flag);
        BulletContainerContents contents = contents(pouch);
        builder.accept(Component.translatable("irons_artifice.tooltip.bullet_pouch", contents.count(), contents.capacity()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pouch) {
        TooltipDisplay display = pouch.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        if (!display.shows(DataComponentRegistry.BULLET_POUCH.get())) {
            return Optional.empty();
        }
        return Optional.of(new BulletContainerTooltip(contents(pouch)));
    }

    @Override
    public void onDestroyed(ItemEntity entity) {
        BulletContainerContents contents = contents(entity.getItem());
        entity.getItem().set(DataComponentRegistry.BULLET_POUCH.get(), BulletContainerContents.empty(contents.capacity()));
        ItemUtils.onContainerDestroyed(entity, contents.copies());
    }
}
