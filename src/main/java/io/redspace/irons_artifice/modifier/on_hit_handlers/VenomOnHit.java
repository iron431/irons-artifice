package io.redspace.irons_artifice.modifier.on_hit_handlers;

import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.HitEntityAccumulator;
import io.redspace.irons_artifice.modifier.OnHitEffect;
import io.redspace.irons_artifice.registry.ParticleRegistry;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class VenomOnHit implements OnHitEffect {

    private int durationTicks;
    private int amplifier;

    public VenomOnHit(int durationTicks, int amplifier) {
        this.durationTicks = durationTicks;
        this.amplifier = amplifier;
    }

    public void addDuration(int ticks) {
        this.durationTicks += ticks;
    }

    public void addAmplifier(int amount) {
        this.amplifier += amount;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public int getAmplifier() {
        return amplifier;
    }

    @Override
    public void onHit(ServerLevel level, Bullet bullet, HitResult hitResult, HitEntityAccumulator accumulator) {
        Vec3 center = hitResult.getLocation();
        Vec3 spawn = level.clip(new ClipContext(center, center.add(0, -5, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, CollisionContext.empty())).getLocation().add(0, 0.05, 0);
        int duration = Math.max(1, durationTicks);
        int amp = Math.max(0, amplifier);
        int poisonColor = MobEffects.POISON.value().getColor();

        AreaEffectCloud cloud = new AreaEffectCloud(level, spawn.x, spawn.y, spawn.z);
        if (bullet.getOwner() instanceof LivingEntity owner) {
            cloud.setOwner(owner);
        }
        cloud.getPersistentData().putBoolean("irons_artifice:venom_cloud", true);
        cloud.setRadius(3.5f);
        cloud.setRadiusOnUse(0);
        cloud.setWaitTime(10);
        cloud.setDuration(duration);
        cloud.setRadiusPerTick(-cloud.getRadius() / duration);
        cloud.addEffect(new MobEffectInstance(MobEffects.POISON, duration, amp));
        level.addFreshEntity(cloud);
        splashEffects(level, center, poisonColor, bullet.getDeltaMovement());
    }

    private void splashEffects(ServerLevel level, Vec3 center, int color, Vec3 impactMotion) {
        RandomSource random = level.getRandom();
        ColorParticleOption splash = ColorParticleOption.create(ParticleRegistry.SPLASH.get(), ARGB.opaque(color));
        Vec3 bias = impactMotion.lengthSqr() > 1.0E-6 ? impactMotion.normalize().scale(0.15) : Vec3.ZERO;

        for (int i = 0; i < 7; i++) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            double speed = 0.15 + random.nextDouble() * 0.35;
            double vx = Math.cos(angle) * speed + bias.x;
            double vy = 0.2 + random.nextDouble() * 0.35;
            double vz = Math.sin(angle) * speed + bias.z;
            // offsets are treated as velocity when count is zero
            Utils.spawnParticles(level, splash, center.x, center.y + 0.1, center.z, 0, vx, vy, vz, 1.5, true);
        }
    }
}
