package io.redspace.irons_artifice.client;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.OptionalInt;

/**
 * Tracks which entity the client is currently rendering an item for.
 * <p>
 * 1.21.1's {@code ItemRenderer#renderStatic(LivingEntity, ...)} is the choke point for every held-item
 * render (first person hands, third person hands, mob gear). GeckoLib's item renderer itself never
 * learns the owner, so the client mixin for that method exposes the entity here for the duration of the
 * render. This mirrors the upstream {@code GunInHandRenderer}'s {@code ITEM_OWNER_ID_TICKET}, which
 * newer GeckoLib can supply natively.
 * <p>
 * Client render thread only; entries are push/pop matched per render call.
 */
public final class RenderingEntityTracker {
    private static final int NO_ENTITY = -1;
    private static final Deque<Integer> STACK = new ArrayDeque<>();

    private RenderingEntityTracker() {
    }

    public static void push(@Nullable Entity entity) {
        STACK.push(entity == null ? NO_ENTITY : entity.getId());
    }

    public static void pop() {
        STACK.pop();
    }

    public static OptionalInt currentEntityId() {
        Integer id = STACK.peek();
        return id == null || id == NO_ENTITY ? OptionalInt.empty() : OptionalInt.of(id);
    }

    /** Defensive cleanup on logout; the deque should already be empty between frames. */
    public static void clear() {
        STACK.clear();
    }
}
