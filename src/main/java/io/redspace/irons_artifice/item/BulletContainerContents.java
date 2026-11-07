package io.redspace.irons_artifice.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.irons_artifice.utils.IronsArtificeTags;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public final class BulletContainerContents {
    public static final int NO_SELECTION = -1;
    private static final int GRID_CELLS = 12;
    private static final int GRID_COLUMNS = 4;
    public static final Codec<BulletContainerContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("capacity").forGetter(BulletContainerContents::capacity),
            ItemStackTemplate.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(BulletContainerContents::stacks)
    ).apply(instance, BulletContainerContents::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BulletContainerContents> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BulletContainerContents::capacity,
            ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()), BulletContainerContents::stacks,
            BulletContainerContents::new
    );

    private final int capacity;
    private final List<ItemStackTemplate> stacks;
    private final int selectedIndex;

    public BulletContainerContents(int capacity, List<ItemStackTemplate> stacks) {
        this(capacity, stacks, NO_SELECTION);
    }

    private BulletContainerContents(int capacity, List<ItemStackTemplate> stacks, int selectedIndex) {
        this.capacity = capacity;
        this.stacks = List.copyOf(stacks);
        this.selectedIndex = selectedIndex;
    }

    public static BulletContainerContents empty(int capacity) {
        return new BulletContainerContents(capacity, List.of());
    }

    public static boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && stack.is(IronsArtificeTags.AMMO) && stack.canFitInsideContainerItems();
    }

    public int capacity() {
        return capacity;
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
        return this == other || other instanceof BulletContainerContents contents && capacity == contents.capacity && stacks.equals(contents.stacks);
    }

    @Override
    public int hashCode() {
        return 31 * capacity + stacks.hashCode();
    }

    @Override
    public String toString() {
        return "BulletPouchContents[capacity=" + capacity + ", stacks=" + stacks + "]";
    }

    public static final class Mutable {
        private final int capacity;
        private final List<ItemStack> stacks = new ArrayList<>();
        private int selectedIndex;

        public Mutable(BulletContainerContents contents) {
            capacity = contents.capacity;
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

        public BulletContainerContents toImmutable() {
            List<ItemStackTemplate> templates = new ArrayList<>(stacks.size());
            for (ItemStack stack : stacks) {
                templates.add(ItemStackTemplate.fromNonEmptyStack(stack));
            }
            return new BulletContainerContents(capacity, templates, selectedIndex);
        }
    }
}
