package io.redspace.irons_artifice.modifier.on_hit_handlers;

import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.HitEntityAccumulator;
import io.redspace.irons_artifice.modifier.OnHitEffect;
import io.redspace.irons_artifice.modifier.modifiers.ChainLightningModifier;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ChainLightningOnHit implements OnHitEffect {
    public static final int CHAIN_COUNT = 2;
    public static final float DAMAGE_MULTIPLIER = 0.25f;
    public static final float RADIUS = 6f;

    @Override
    public void onHit(ServerLevel level, Bullet bullet, HitResult hitResult, HitEntityAccumulator accumulator) {
        Vec3 center = hitResult.getLocation();
        AABB area = AABB.ofSize(center, RADIUS, RADIUS, RADIUS).inflate(1);
        var random = bullet.getRandom();
        Entity owner = bullet.getOwner();
        var targets = bullet.level().getEntities(bullet, area, entity ->
                !accumulator.contains(entity)
                        && entity.canBeHitByProjectile()
                        && Utils.canHarm(owner, entity)
                        && entity.getBoundingBox().getCenter().distanceToSqr(center) < RADIUS * RADIUS
                        && Utils.hasLineOfSight(bullet, entity)
        );
        for (int i = 0; i < CHAIN_COUNT; i++) {
            Vec3 visualAnchor = center.add(new Vec3(random.nextFloat(), random.nextFloat(), random.nextFloat()).scale(RADIUS).subtract(new Vec3(RADIUS, RADIUS, RADIUS).scale(0.5)).scale(1));
            if (!targets.isEmpty()) {
                Entity entity = targets.get(random.nextInt(targets.size()));
                float damage = bullet.resolveDamage() * DAMAGE_MULTIPLIER;
                if (entity.hurtServer(level, bullet.damageSources().indirectMagic(bullet, bullet.getOwner()), damage)) {
                    accumulator.add(entity);
                }
                visualAnchor = entity.getBoundingBox().getCenter();
                targets.remove(entity);
            }
            int particles = (int) visualAnchor.distanceTo(center) * 6;
            Vec3 motion = visualAnchor.subtract(center).normalize().scale(0.05);
            for (int j = 0; j < particles; j++) {
                Vec3 pos = center.lerp(visualAnchor, j / (float) particles);
                Utils.spawnParticles(level, ChainLightningModifier.LIGHTNING_TRAIL, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0.01, true);
            }
        }
    }
}
