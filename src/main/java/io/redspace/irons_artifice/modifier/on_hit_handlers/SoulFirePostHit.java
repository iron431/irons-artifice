package io.redspace.irons_artifice.modifier.on_hit_handlers;

import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.modifier.PostHitEffect;
import io.redspace.irons_artifice.utils.Utils;
import io.redspace.ironslib.soulfire.SoulFireHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;

public class SoulFirePostHit implements PostHitEffect {
    private final int durationTicks;

    public SoulFirePostHit(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    @Override
    public void postHit(ServerLevel level, Bullet bullet, HitResult hitResult, Entity entity) {
        if (!Utils.canHarm(bullet.getOwner(), entity)) {
            return;
        }
        SoulFireHelper.igniteSoul(entity, durationTicks);
    }
}
