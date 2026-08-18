package io.redspace.irons_artifice.data;

import io.redspace.irons_artifice.registry.DataAttachmentRegistry;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Transient, per-player record of last living entity hit with a bullet, for consecutive-hit tracking.
 */
public record LastHitTarget(@Nullable UUID uuid) {
    public static final LastHitTarget NONE = new LastHitTarget(null);

    public static LastHitTarget get(ServerPlayer player) {
        return player.getData(DataAttachmentRegistry.LAST_HIT_TARGET);
    }

    public static void set(ServerPlayer player, @Nullable UUID uuid) {
        player.setData(DataAttachmentRegistry.LAST_HIT_TARGET, uuid == null ? NONE : new LastHitTarget(uuid));
    }
}
