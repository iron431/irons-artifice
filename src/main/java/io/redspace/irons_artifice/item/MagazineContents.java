package io.redspace.irons_artifice.item;

import io.redspace.irons_artifice.registry.DataComponentRegistry;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record MagazineContents(int count) {
    public static final MagazineContents EMPTY = new MagazineContents(0);

    public static final Codec<MagazineContents> CODEC =
            Codec.INT.xmap(MagazineContents::new, MagazineContents::count);

    public static final StreamCodec<ByteBuf, MagazineContents> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(MagazineContents::new, MagazineContents::count);

    public static @Nullable MagazineContents get(ItemStack stack) {
        return stack.get(DataComponentRegistry.MAGAZINE);
    }

    public static void set(ItemStack stack, MagazineContents magazine) {
        stack.set(DataComponentRegistry.MAGAZINE, magazine);
    }

    public static boolean has(ItemStack stack) {
        return stack.has(DataComponentRegistry.MAGAZINE);
    }

    public static void remove(ItemStack stack) {
        stack.remove(DataComponentRegistry.MAGAZINE);
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
