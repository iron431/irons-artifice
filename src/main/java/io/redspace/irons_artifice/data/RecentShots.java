package io.redspace.irons_artifice.data;

import io.redspace.irons_artifice.entity.IExtendedLivingEntity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public record RecentShots(List<Long> ticks) {
    public static final RecentShots NONE = new RecentShots(List.of());
    public static final int WINDOW_TICKS = 20;

    public boolean isEmpty() {
        return ticks.isEmpty();
    }

    public int size() {
        return ticks.size();
    }

    public RecentShots pruned(long now) {
        List<Long> next = new ArrayList<>(ticks);
        next.removeIf(tick -> now - tick >= WINDOW_TICKS);
        if (next.size() == ticks.size()) {
            return this;
        }
        if (next.isEmpty()) {
            return NONE;
        }
        return new RecentShots(next);
    }

    public RecentShots withShot(long now) {
        List<Long> next = new ArrayList<>(pruned(now).ticks);
        next.add(now);
        return new RecentShots(next);
    }

    public static int count(LivingEntity entity) {
        IExtendedLivingEntity extended = (IExtendedLivingEntity) entity;
        RecentShots current = extended.irons_artifice$getRecentShots();
        if (current.isEmpty()) {
            return 0;
        }
        RecentShots pruned = current.pruned(entity.level().getGameTime());
        if (pruned != current) {
            extended.irons_artifice$setRecentShots(pruned);
        }
        return pruned.size();
    }

    public static void trackShot(LivingEntity entity) {
        IExtendedLivingEntity extended = (IExtendedLivingEntity) entity;
        extended.irons_artifice$setRecentShots(extended.irons_artifice$getRecentShots().withShot(entity.level().getGameTime()));
    }
}
