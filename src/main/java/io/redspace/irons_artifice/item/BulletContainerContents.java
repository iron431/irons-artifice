package io.redspace.irons_artifice.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.utils.IronsArtificeTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class BulletContainerContents {
    public static final int NO_SELECTION = -1;
    private static final int GRID_CELLS = 12;
    private static final int GRID_COLUMNS = 4;
    public static final Codec<BulletContainerContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("capacity").forGetter(BulletContainerContents::capacity),
            ItemStack.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(BulletContainerContents::stacks)
    ).apply(instance, BulletContainerContents::new));

    public static final String TAG_KEY = IronsArtifice.id("bullet_pouch").toString();

    private final int capacity;
    private final List<ItemStack> stacks;

    public BulletContainerContents(int capacity, List<ItemStack> stacks) {
        this.capacity = capacity;
        this.stacks = List.copyOf(stacks);
    }

    public static BulletContainerContents empty(int capacity) {
        return new BulletContainerContents(capacity, List.of());
    }

    public static @Nullable BulletContainerContents get(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_KEY)) {
            return null;
        }
        return CODEC.parse(NbtOps.INSTANCE, tag.get(TAG_KEY)).resultOrPartial(IronsArtifice.LOGGER::error).orElse(null);
    }

    public static void set(ItemStack stack, BulletContainerContents contents) {
        CODEC.encodeStart(NbtOps.INSTANCE, contents).resultOrPartial(IronsArtifice.LOGGER::error)
                .ifPresent(encoded -> stack.getOrCreateTag().put(TAG_KEY, encoded));
    }

    public static boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && stack.is(IronsArtificeTags.AMMO) && stack.getItem().canFitInsideContainerItems();
    }

    public int capacity() {
        return capacity;
    }

    public List<ItemStack> stacks() {
        return stacks;
    }

    public int size() {
        return stacks.size();
    }

    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    public int count() {
        int total = 0;
        for (ItemStack stack : stacks) {
            total += stack.getCount();
        }
        return total;
    }

    public Stream<ItemStack> copies() {
        return stacks.stream().map(ItemStack::copy);
    }

    public int numberOfStacksToShow() {
        int visibleCells = size() > GRID_CELLS ? GRID_CELLS - 1 : GRID_CELLS;
        int onPartialRow = size() % GRID_COLUMNS;
        int emptyOnPartialRow = onPartialRow == 0 ? 0 : GRID_COLUMNS - onPartialRow;
        return Math.min(size(), visibleCells - emptyOnPartialRow);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof BulletContainerContents contents && capacity == contents.capacity && stacksMatch(stacks, contents.stacks);
    }

    @Override
    public int hashCode() {
        int hash = capacity;
        for (ItemStack stack : stacks) {
            hash = 31 * hash + stack.getItem().hashCode();
            hash = 31 * hash + stack.getCount();
            hash = 31 * hash + Objects.hashCode(stack.getTag());
        }
        return hash;
    }

    private static boolean stacksMatch(List<ItemStack> left, List<ItemStack> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int i = 0; i < left.size(); i++) {
            if (!ItemStack.matches(left.get(i), right.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "BulletPouchContents[capacity=" + capacity + ", stacks=" + stacks + "]";
    }

    public static final class Mutable {
        private final int capacity;
        private final List<ItemStack> stacks = new ArrayList<>();

        public Mutable(BulletContainerContents contents) {
            capacity = contents.capacity;
            for (ItemStack stored : contents.stacks) {
                if (!stored.isEmpty()) {
                    stacks.add(stored.copy());
                }
            }
        }

        public int count() {
            int total = 0;
            for (ItemStack stack : stacks) {
                total += stack.getCount();
            }
            return total;
        }

        public int room() {
            return capacity - count();
        }

        public int insert(ItemStack source) {
            if (!accepts(source)) {
                return 0;
            }
            int toMove = Math.min(source.getCount(), room());
            if (toMove <= 0) {
                return 0;
            }
            int remaining = toMove;
            List<ItemStack> promoted = new ArrayList<>();
            Iterator<ItemStack> iterator = stacks.iterator();
            while (iterator.hasNext() && remaining > 0) {
                ItemStack stored = iterator.next();
                int space = stored.getMaxStackSize() - stored.getCount();
                if (space <= 0 || !ItemStack.isSameItemSameTags(stored, source)) {
                    continue;
                }
                int added = Math.min(space, remaining);
                stored.grow(added);
                remaining -= added;
                iterator.remove();
                promoted.add(stored);
            }
            int maxStackSize = source.getMaxStackSize();
            int firstChunk = remaining % maxStackSize == 0 ? maxStackSize : remaining % maxStackSize;
            for (int chunk = firstChunk; remaining > 0; chunk = maxStackSize) {
                promoted.add(0, source.copyWithCount(chunk));
                remaining -= chunk;
            }
            stacks.addAll(0, promoted);
            source.shrink(toMove);
            return toMove;
        }

        /** Takes the selected stack, or the newest one when nothing is selected. */
        public ItemStack removeOne(int selectedIndex) {
            if (stacks.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return stacks.remove(inRange(selectedIndex) ? selectedIndex : 0);
        }

        public void putBack(ItemStack stack) {
            if (!stack.isEmpty()) {
                stacks.add(0, stack);
            }
        }

        public int drain(int amount) {
            int removed = 0;
            while (removed < amount && !stacks.isEmpty()) {
                ItemStack first = stacks.get(0);
                int take = Math.min(amount - removed, first.getCount());
                first.shrink(take);
                removed += take;
                if (first.isEmpty()) {
                    stacks.remove(0);
                }
            }
            return removed;
        }

        private boolean inRange(int index) {
            return index >= 0 && index < stacks.size();
        }

        public BulletContainerContents toImmutable() {
            List<ItemStack> copies = new ArrayList<>(stacks.size());
            for (ItemStack stack : stacks) {
                copies.add(stack.copy());
            }
            return new BulletContainerContents(capacity, copies);
        }
    }
}
