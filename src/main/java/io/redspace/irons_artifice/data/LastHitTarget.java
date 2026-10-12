package io.redspace.irons_artifice.data;

import io.redspace.irons_artifice.entity.IExtendedLivingEntity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Transient, record of last living entity hit with a bullet, for consecutive-hit tracking.
 */
public record LastHitTarget(@Nullable UUID uuid) {
    public static final LastHitTarget NONE = new LastHitTarget(null);

    public static LastHitTarget get(LivingEntity entity) {
        return ((IExtendedLivingEntity) entity).irons_artifice$getLastHitTarget();
    }

    public static void set(LivingEntity entity, @Nullable UUID uuid) {
        ((IExtendedLivingEntity) entity).irons_artifice$setLastHitTarget(uuid == null ? NONE : new LastHitTarget(uuid));
    }
}
