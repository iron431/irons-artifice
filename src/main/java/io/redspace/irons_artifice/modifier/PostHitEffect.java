package io.redspace.irons_artifice.modifier;

import io.redspace.irons_artifice.entity.Bullet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;

public interface PostHitEffect {
    /**
     * Called once per damaged entity on bullet hit
     */
    void postHit(ServerLevel level, Bullet bullet, HitResult hitResult, Entity entity);
}
