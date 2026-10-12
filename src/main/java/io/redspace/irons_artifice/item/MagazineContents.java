package io.redspace.irons_artifice.item;

import io.redspace.irons_artifice.IronsArtifice;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record MagazineContents(int count) {
    public static final MagazineContents EMPTY = new MagazineContents(0);

    public static final String TAG_KEY = IronsArtifice.id("magazine").toString();

    /** A gun with no magazine tag is full, so a fresh stack needs no tag written to it. */
    public static @Nullable MagazineContents get(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_KEY)) {
            return new MagazineContents(tag.getInt(TAG_KEY));
        }
        return stack.getItem() instanceof GunItem gun ? new MagazineContents(gun.magazineCapacity()) : null;
    }

    public static void set(ItemStack stack, MagazineContents magazine) {
        stack.getOrCreateTag().putInt(TAG_KEY, magazine.count);
    }

    public static boolean has(ItemStack stack) {
        return get(stack) != null;
    }

    public boolean isEmpty() {
        return this.count <= 0;
    }

    public int missing(int capacity) {
        return Math.max(0, capacity - this.count);
    }

    public boolean isFull(int capacity) {
        return this.count >= capacity;
    }

    public MagazineContents with(int newCount) {
        return new MagazineContents(Math.max(0, newCount));
    }

    /** Returns a copy with one round removed (floored at zero). */
    public MagazineContents deplete() {
        return with(this.count - 1);
    }

    /** Returns a copy with rounds removed (floored at zero). */
    public MagazineContents deplete(int deplete) {
        return with(this.count - deplete);
    }
}
