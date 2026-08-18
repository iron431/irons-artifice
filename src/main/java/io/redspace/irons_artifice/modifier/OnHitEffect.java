package io.redspace.irons_artifice.modifier;

import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.HitEntityAccumulator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.HitResult;

public interface OnHitEffect {
    /**
     * Called on bullet hit. HitEntityAccumulator tracks damaged entities by previous OnHitEffects. Add damaged entities to the accumulator.
     */
    void onHit(ServerLevel level, Bullet bullet, HitResult hitResult, HitEntityAccumulator accumulator);
}
