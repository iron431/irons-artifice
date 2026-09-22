package io.redspace.irons_artifice.item;

import io.redspace.irons_artifice.registry.DataAttachmentRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * A shot that arrived at the server slightly before the shooter's fire delay had elapsed, held until
 * it has. Useful for smoothing over network jitter
 *
 * @param direction         aim direction the shot was fired with, used unchanged when it runs
 * @param queuedAtGameTime  server game time when it was queued, for expiry
 */
public record PendingShot(Vec3 direction, long queuedAtGameTime) {

    public static final PendingShot NONE = new PendingShot(Vec3.ZERO, Long.MIN_VALUE);

    public static final long MAX_AGE_TICKS = 2;

    public static PendingShot get(LivingEntity living) {
        return living.getData(DataAttachmentRegistry.PENDING_SHOT.get());
    }

    public static void set(LivingEntity living, PendingShot shot) {
        living.setData(DataAttachmentRegistry.PENDING_SHOT.get(), shot);
    }

    public static void clear(LivingEntity living) {
        living.setData(DataAttachmentRegistry.PENDING_SHOT.get(), NONE);
    }

    public boolean isEmpty() {
        return this.queuedAtGameTime == Long.MIN_VALUE;
    }

    public boolean hasExpired(long now) {
        return now - this.queuedAtGameTime > MAX_AGE_TICKS;
    }
}
