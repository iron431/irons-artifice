package io.redspace.irons_artifice.modifier.on_hit_handlers;

import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.modifier.PostHitEffect;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;

public class IgnitePostHit implements PostHitEffect {
    private int durationTicks;

    public IgnitePostHit(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    public void addDuration(int ticks) {
        this.durationTicks += ticks;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public void postHit(ServerLevel level, Bullet bullet, HitResult hitResult, Entity entity) {
        if (!Utils.canHarm(bullet.getOwner(), entity)) {
            return;
        }
        entity.igniteForTicks(durationTicks);
    }
}
