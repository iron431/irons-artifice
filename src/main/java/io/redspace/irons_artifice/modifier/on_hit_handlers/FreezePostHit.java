package io.redspace.irons_artifice.modifier.on_hit_handlers;

import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.modifier.PostHitEffect;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FreezePostHit implements PostHitEffect {
    public static final int FREEZE_TICKS = 140;

    @Override
    public void postHit(ServerLevel level, Bullet bullet, HitResult hitResult, Entity entity) {
        if (!Utils.canHarm(bullet.getOwner(), entity)) {
            return;
        }
        entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze() * 5,
                entity.getTicksFrozen() + FREEZE_TICKS));

        Vec3 center = entity.getBoundingBox().getCenter();
        Utils.spawnParticles(level, ParticleTypes.SNOWFLAKE, center.x, center.y, center.z, 8, 0.25, 0.35, 0.25, 0.02, false);
    }
}
