package io.redspace.irons_artifice.client;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

/** The entity whose hand the item being rendered is in. Pushed and popped by {@code ItemInHandRendererMixin}. */
public final class HeldItemRenderContext {
    private static final int MAX_DEPTH = 8;

    private static final Deque<LivingEntity> HOLDERS = new ArrayDeque<>();

    private HeldItemRenderContext() {
    }

    public static void push(LivingEntity holder) {
        // Item renders never nest this deep, so a stack this size means an exception skipped pop() and leaked.
        if (HOLDERS.size() > MAX_DEPTH) {
            HOLDERS.clear();
        }
        HOLDERS.push(holder);
    }

    public static void pop() {
        HOLDERS.poll();
    }

    /** Null outside a hand render, which is the right answer for gui, ground and item frames. */
    public static @Nullable LivingEntity holder() {
        return HOLDERS.peek();
    }
}
