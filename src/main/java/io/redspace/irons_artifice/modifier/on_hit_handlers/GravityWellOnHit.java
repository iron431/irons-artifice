package io.redspace.irons_artifice.modifier.on_hit_handlers;

import io.redspace.irons_artifice.client.particle.MuzzleFlashParticleOption;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.HitEntityAccumulator;
import io.redspace.irons_artifice.modifier.OnHitEffect;
import io.redspace.irons_artifice.registry.ParticleRegistry;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class GravityWellOnHit implements OnHitEffect {
    public static final float RADIUS = 3f;
    public static final float MAX_STRENGTH = 0.5f;

    @Override
    public void onHit(ServerLevel level, Bullet bullet, HitResult hitResult, HitEntityAccumulator accumulator) {
        Vec3 center = hitResult.getLocation().subtract(bullet.getDeltaMovement().normalize().scale(0.25));
        AABB area = AABB.ofSize(center, RADIUS * 2, RADIUS * 2, RADIUS * 2).inflate(1);
        Entity owner = bullet.getOwner();
        bullet.level().getEntities(bullet, area, entity ->
                entity.canBeHitByProjectile() && Utils.canHarm(owner, entity)
        ).forEach(
                entity -> {
                    if (entity.position().distanceToSqr(center) < RADIUS * RADIUS) {
                        Vec3 delta = center.subtract(entity.position());
                        entity.setDeltaMovement(entity.getDeltaMovement().add(delta.normalize().scale(Math.min(MAX_STRENGTH, delta.length()))));
                        entity.hurtMarked = true;
                    }
                }
        );
        // todo: vfx/sound
        Utils.spawnParticles(level, new MuzzleFlashParticleOption(ParticleRegistry.EXPLOSION_96.get(), 0.9F, 0.25f, 1f), center.x, center.y + 0.25, center.z, 1, 0, 0, 0, 0, true);
    }
}
