package io.redspace.irons_artifice.modifier.on_hit_handlers;

import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.entity.ChainEntity;
import io.redspace.irons_artifice.gun.HitEntityAccumulator;
import io.redspace.irons_artifice.data.LastHitTarget;
import io.redspace.irons_artifice.modifier.OnHitEffect;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ChainShotOnHit implements OnHitEffect {
    @Override
    public void onHit(ServerLevel level, Bullet bullet, HitResult hitResult, HitEntityAccumulator accumulator) {
        if (!(hitResult instanceof EntityHitResult entityHit)) {
            return;
        }
        if (!(entityHit.getEntity() instanceof LivingEntity current)) {
            return;
        }
        if (!(bullet.getOwner() instanceof ServerPlayer player)) {
            return;
        }

        LastHitTarget lastHit = LastHitTarget.get(player);
        if (lastHit.uuid() != null) {
            Entity previous = level.getEntity(lastHit.uuid());
            if (previous instanceof LivingEntity last
                    && !last.isRemoved()
                    && last != current
                    && Utils.canHarm(player, last)
                    && Utils.canHarm(player, current)) {
                double distSq = last.getBoundingBox().getCenter().distanceToSqr(current.getBoundingBox().getCenter());
                if (distSq <= ChainEntity.SPAWN_RANGE * ChainEntity.SPAWN_RANGE) {
                    level.addFreshEntity(new ChainEntity(level, last, current));
                }
            }
        }

        LastHitTarget.set(player, current.getUUID());
    }
}
