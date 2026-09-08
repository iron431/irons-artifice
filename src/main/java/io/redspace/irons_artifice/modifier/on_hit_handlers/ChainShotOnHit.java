package io.redspace.irons_artifice.modifier.on_hit_handlers;

import io.redspace.irons_artifice.data.LastHitTarget;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.entity.ChainEntity;
import io.redspace.irons_artifice.modifier.PostHitEffect;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;

public class ChainShotOnHit implements PostHitEffect {
    @Override
    public void postHit(ServerLevel level, Bullet bullet, HitResult hitResult, Entity entity) {
        if (!(entity instanceof LivingEntity livingVictim)) {
            return;
        }
        if (!(bullet.getOwner() instanceof LivingEntity livingOwner)) {
            return;
        }

        LastHitTarget lastHit = LastHitTarget.get(livingOwner);
        if (lastHit.uuid() != null) {
            Entity previous = level.getEntity(lastHit.uuid());
            if (previous instanceof LivingEntity last
                    && !last.isRemoved()
                    && last != livingVictim
                    && Utils.canHarm(livingOwner, last)
                    && Utils.canHarm(livingOwner, livingVictim)) {
                double distSq = last.getBoundingBox().getCenter().distanceToSqr(livingVictim.getBoundingBox().getCenter());
                if (distSq <= ChainEntity.SPAWN_RANGE * ChainEntity.SPAWN_RANGE) {
                    level.addFreshEntity(new ChainEntity(level, last, livingVictim));
                }
            }
        }

        LastHitTarget.set(livingOwner, livingVictim.getUUID());
    }
}
