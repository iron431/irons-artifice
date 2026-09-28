package io.redspace.irons_artifice.item;

import com.mojang.serialization.Codec;
import io.redspace.irons_artifice.utils.IronsArtificeTags;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public final class BulletPouchContents {
    public static final int CAPACITY = 256;
    public static final int NO_SELECTION = -1;
    private static final int GRID_CELLS = 12;
    private static final int GRID_COLUMNS = 4;
    public static final BulletPouchContents EMPTY = new BulletPouchContents(List.of());
    public static final Codec<BulletPouchContents> CODEC = ItemStackTemplate.CODEC.listOf()
            .xmap(BulletPouchContents::new, BulletPouchContents::stacks);
    public static final StreamCodec<RegistryFriendlyByteBuf, BulletPouchContents> STREAM_CODEC = ItemStackTemplate.STREAM_CODEC
            .apply(ByteBufCodecs.list())
            .map(BulletPouchContents::new, BulletPouchContents::stacks);

    private final List<ItemStackTemplate> stacks;
    private final int selectedIndex;

    public BulletPouchContents(List<ItemStackTemplate> stacks) {
        this(stacks, NO_SELECTION);
    }

    private BulletPouchContents(List<ItemStackTemplate> stacks, int selectedIndex) {
        this.stacks = List.copyOf(stacks);
        this.selectedIndex = selectedIndex;
    }

    public static boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && stack.is(IronsArtificeTags.AMMO) && stack.canFitInsideContainerItems();
    }

    public List<ItemStackTemplate> stacks() {
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
        for (ItemStackTemplate stack : stacks) {
            total += stack.count();
        }
        return total;
    }

    public Stream<ItemStack> copies() {
        return stacks.stream().map(ItemStackTemplate::create);
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public @Nullable ItemStackTemplate selectedStack() {
        return selectedIndex >= 0 && selectedIndex < stacks.size() ? stacks.get(selectedIndex) : null;
    }

    public int numberOfStacksToShow() {
        int visibleCells = size() > GRID_CELLS ? GRID_CELLS - 1 : GRID_CELLS;
        int onPartialRow = size() % GRID_COLUMNS;
        int emptyOnPartialRow = onPartialRow == 0 ? 0 : GRID_COLUMNS - onPartialRow;
        return Math.min(size(), visibleCells - emptyOnPartialRow);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof BulletPouchContents contents && stacks.equals(contents.stacks);
    }

    @Override
    public int hashCode() {
        return stacks.hashCode();
    }

    @Override
    public String toString() {
        return "BulletPouchContents" + stacks;
    }

    public static final class Mutable {
        private final List<ItemStack> stacks = new ArrayList<>();
        private int selectedIndex;

        public Mutable(BulletPouchContents contents) {
            for (ItemStackTemplate template : contents.stacks) {
                ItemStack stack = template.create();
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
            selectedIndex = stacks.size() == contents.stacks.size() ? contents.selectedIndex : NO_SELECTION;
        }

        public int count() {
            int total = 0;
            for (ItemStack stack : stacks) {
                total += stack.getCount();
            }
            return total;
        }

        public int room() {
            return CAPACITY - count();
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
                if (space <= 0 || !ItemStack.isSameItemSameComponents(stored, source)) {
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
            selectedIndex = NO_SELECTION;
            return toMove;
        }

        public ItemStack removeOne() {
            if (stacks.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int index = inRange(selectedIndex) ? selectedIndex : 0;
            selectedIndex = NO_SELECTION;
            return stacks.remove(index);
        }

        public void putBack(ItemStack stack) {
            if (!stack.isEmpty()) {
                stacks.addFirst(stack);
            }
        }

        public int drain(int amount) {
            int removed = 0;
            while (removed < amount && !stacks.isEmpty()) {
                ItemStack first = stacks.getFirst();
                int take = Math.min(amount - removed, first.getCount());
                first.shrink(take);
                removed += take;
                if (first.isEmpty()) {
                    stacks.removeFirst();
                }
            }
            if (removed > 0) {
                selectedIndex = NO_SELECTION;
            }
            return removed;
        }

        public void toggleSelected(int index) {
            selectedIndex = index != selectedIndex && inRange(index) ? index : NO_SELECTION;
        }

        private boolean inRange(int index) {
            return index >= 0 && index < stacks.size();
        }

        public BulletPouchContents toImmutable() {
            List<ItemStackTemplate> templates = new ArrayList<>(stacks.size());
            for (ItemStack stack : stacks) {
                templates.add(ItemStackTemplate.fromNonEmptyStack(stack));
            }
            return new BulletPouchContents(templates, selectedIndex);
        }
    }
}
