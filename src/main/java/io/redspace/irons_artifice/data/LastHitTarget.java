package io.redspace.irons_artifice.data;

import io.redspace.irons_artifice.registry.DataAttachmentRegistry;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Transient, record of last living entity hit with a bullet, for consecutive-hit tracking.
 */
public record LastHitTarget(@Nullable UUID uuid) {
    public static final LastHitTarget NONE = new LastHitTarget(null);

    public static LastHitTarget get(LivingEntity entity) {
        return entity.getData(DataAttachmentRegistry.LAST_HIT_TARGET);
    }

    public static void set(LivingEntity entity, @Nullable UUID uuid) {
        entity.setData(DataAttachmentRegistry.LAST_HIT_TARGET, uuid == null ? NONE : new LastHitTarget(uuid));
    }
}
