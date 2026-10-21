package io.redspace.irons_artifice.item;

import io.redspace.irons_artifice.registry.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BulletContainerItem extends Item {
    private static final BulletContainerContents MISSING_CONTENTS = BulletContainerContents.empty(0);

    private final int capacity;

    public BulletContainerItem(Properties properties, int capacity) {
        super(properties.stacksTo(1));
        this.capacity = capacity;
    }

    public static BulletContainerContents contents(ItemStack pouch) {
        BulletContainerContents contents = BulletContainerContents.get(pouch);
        if (contents != null) {
            return contents;
        }
        return pouch.getItem() instanceof BulletContainerItem item ? BulletContainerContents.empty(item.capacity) : MISSING_CONTENTS;
    }

    public static int count(ItemStack pouch) {
        return contents(pouch).count();
    }

    public static int numberOfStacksToShow(ItemStack pouch) {
        return contents(pouch).numberOfStacksToShow();
    }

    private static void write(ItemStack pouch, BulletContainerContents.Mutable mutable) {
        BulletContainerContents.set(pouch, mutable.toImmutable());
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
            if (inserted) {
                playSound(player, SoundRegistry.BULLET_BOX_INSERT.get());
            }
            return true;
        }
        if (action == ClickAction.SECONDARY && other.isEmpty()) {
            ItemStack removed = contents.removeOne(BulletContainerSelection.get(player));
            if (removed.isEmpty()) {
                return false;
            }
            BulletContainerSelection.clear(player);
            ItemStack remainder = slot.safeInsert(removed);
            if (remainder.isEmpty()) {
                playSound(player, SoundRegistry.BULLET_BOX_EXTRACT.get());
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
            if (inserted) {
                playSound(player, SoundRegistry.BULLET_BOX_INSERT.get());
            }
            return true;
        }
        if (action == ClickAction.SECONDARY && carried.isEmpty() && slot.allowModification(player)) {
            ItemStack removed = contents.removeOne(BulletContainerSelection.get(player));
            if (removed.isEmpty()) {
                return false;
            }
            BulletContainerSelection.clear(player);
            write(pouch, contents);
            carriedAccess.set(removed);
            playSound(player, SoundRegistry.BULLET_BOX_EXTRACT.get());
            return true;
        }
        BulletContainerSelection.clear(player);
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pouch = player.getItemInHand(hand);
        if (pouch.getCount() != 1) {
            return InteractionResultHolder.pass(pouch);
        }
        BulletContainerContents.Mutable contents = new BulletContainerContents.Mutable(contents(pouch));
        ItemStack removed = contents.removeOne(BulletContainerSelection.get(player));
        if (removed.isEmpty()) {
            return InteractionResultHolder.pass(pouch);
        }
        BulletContainerSelection.clear(player);
        write(pouch, contents);
        if (!level.isClientSide()) {
            player.getInventory().placeItemBackInInventory(removed);
        }
        playSound(player, SoundRegistry.BULLET_BOX_EXTRACT.get());
        return InteractionResultHolder.sidedSuccess(pouch, level.isClientSide());
    }

    private static void playSound(Player player, SoundEvent sound) {
        player.playSound(sound, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }

    @Override
    public boolean isBarVisible(ItemStack pouch) {
        BulletContainerContents contents = contents(pouch);
        return !contents.isEmpty();
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
        return Mth.color(0.44F, 0.53F, 1.0F);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public void appendHoverText(ItemStack pouch, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag flag) {
        super.appendHoverText(pouch, level, tooltipComponents, flag);
        BulletContainerContents contents = contents(pouch);
        tooltipComponents.add(Component.translatable("irons_artifice.tooltip.bullet_pouch", contents.count(), contents.capacity()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pouch) {
        return Optional.of(new BulletContainerTooltip(contents(pouch)));
    }

    @Override
    public void onDestroyed(ItemEntity entity) {
        BulletContainerContents contents = contents(entity.getItem());
        BulletContainerContents.set(entity.getItem(), BulletContainerContents.empty(contents.capacity()));
        ItemUtils.onContainerDestroyed(entity, contents.copies());
    }
}
