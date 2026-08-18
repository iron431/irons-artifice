package io.redspace.irons_artifice.modifier.on_hit_handlers;

import io.redspace.irons_artifice.client.particle.ColorTransitionParticleOption;
import io.redspace.irons_artifice.data.ParticleStack;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.Value;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.HitEntityAccumulator;
import io.redspace.irons_artifice.modifier.OnHitEffect;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ParticleRegistry;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FrozenShrapnelOnHit implements OnHitEffect {
    public static final int SHRAPNEL_COUNT = 3;
    public static final float DAMAGE_FRACTION = 0.5f;
    public static final float SPEED = 3f;
    public static final float DRAG = 0.8f;
    public static final float CONE_HALF_ANGLE_DEG = 30f;
    public static final float SPAWN_OFFSET = 0.1f;
    public static final int TRAIL_COLOR_FROM = 0xE8F7FF;
    public static final int TRAIL_COLOR_TO = 0x7EC8FF;

    @Override
    public void onHit(ServerLevel level, Bullet bullet, HitResult hitResult, HitEntityAccumulator accumulator) {
        ShotProfile parentProfile = bullet.getProfile();
        if (parentProfile == null) {
            return;
        }

        Vec3 incoming = bullet.getDeltaMovement();
        if (incoming.lengthSqr() < 1.0E-8) {
            return;
        }
        Vec3 axis = incoming.normalize();
        if (hitResult instanceof BlockHitResult blockHit) {
            axis = Utils.reflect(incoming, blockHit.getDirection().getUnitVec3()).normalize();
            if (axis.lengthSqr() < 1.0E-8) {
                return;
            }
        }

        Vec3 origin = hitResult.getLocation().add(axis.scale(SPAWN_OFFSET));
        Entity hitEntity = hitResult instanceof EntityHitResult entityHit ? entityHit.getEntity() : null;
        float childDamage = bullet.resolveDamage() * DAMAGE_FRACTION;
        RandomSource random = level.getRandom();
        for (int i = 0; i < SHRAPNEL_COUNT; i++) {
            Vec3 direction = Utils.directionWithinCone(axis, CONE_HALF_ANGLE_DEG, random);
            ShotProfile childProfile = createChildProfile(parentProfile, childDamage);
            Bullet child = new Bullet(EntityRegistry.BULLET.get(), level);
            child.setOwner(bullet.getOwner());
            child.applyProfile(childProfile);
            child.setPos(origin);
            if (hitEntity != null) {
                child.markPierced(hitEntity);
            }
            child.shoot(direction.x, direction.y, direction.z, SPEED, 0f);
            level.addFreshEntity(child);
        }
    }

    private static ShotProfile createChildProfile(ShotProfile parent, float damage) {
        ShotProfile child = parent.deepCopy();
        ShotComponentMap components = child.components();
        components.set(ShotComponents.DAMAGE, Value.of(damage));
        components.set(ShotComponents.PROJECTILE_COUNT, Value.of(1));
        components.set(ShotComponents.BULLET_SPEED, Value.of(SPEED));
        components.set(ShotComponents.BULLET_DRAG, Value.of(DRAG));
        ParticleStack trail = new ParticleStack();
        trail.add(new ColorTransitionParticleOption(
                ParticleRegistry.BULLET_TRAIL.get(), TRAIL_COLOR_FROM, TRAIL_COLOR_TO, 1f, 0f, 1f, 1f, 0.45f, 0f, 0
        ));
        components.set(ShotComponents.PARTICLE_TRAIL, trail);
        components.getOrCreate(ShotComponents.ON_HIT).remove(FrozenShrapnelOnHit.class);
        return child;
    }
}
