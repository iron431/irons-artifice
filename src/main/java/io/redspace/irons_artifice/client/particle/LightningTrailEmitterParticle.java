package io.redspace.irons_artifice.client.particle;

import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class LightningTrailEmitterParticle extends BulletTrailParticle {
    private static final float EMIT_CHANCE = 0.04f;

    public LightningTrailEmitterParticle(ClientLevel level, double x, double y, double z, double xa, double ya, double za, SpriteSet spriteSet, ColorTransitionParticleOption particleOptions) {
        super(level, x, y, z, xa, ya, za, spriteSet, particleOptions);
        if (level.getRandom().nextFloat() < EMIT_CHANCE) {
            emitLightningTrail(level, x, y, z, xa, ya, za);
        }
    }

    private void emitLightningTrail(ClientLevel level, double x, double y, double z, double xa, double ya, double za) {
        Vec3 direction = new Vec3(xa, ya, za);
        double speed = direction.length();
        direction = direction.scale(1 / speed);
        Vec3 arbitrary = Math.abs(direction.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 u = direction.cross(arbitrary).normalize();
        Vec3 v = direction.cross(u);
        float r = 0.5f;
        Vec3 pos = new Vec3(x, y, z);
        u = u.scale((level.getRandom().nextFloat() - 0.5f) * 2 * r);
        v = v.scale((level.getRandom().nextFloat() - 0.5f) * 2 * r);
        Vec3 destination = pos.add(direction.scale(level.getRandom().nextIntBetweenInclusive(20, 40) * 0.1f));
        int count = (int) (destination.distanceTo(pos) * Bullet.TRAIL_DENSITY);
        Vec3 motion = destination.subtract(pos).normalize().scale(speed);
        motion = new Vec3(xa, ya, za);
        for (int i = 0; i < count; i++) {
            float f = (i) / (count - 1f);
            float uvf = Utils.triangleInterpolate(f, 0, 0.5f, 1);
            Vec3 spawn = pos.lerp(destination, f)
                    .add(u.scale(uvf))
                    .add(v.scale(uvf));
            level.addAlwaysVisibleParticle(this.particleOptions, true, spawn.x, spawn.y, spawn.z, motion.x, motion.y, motion.z);
        }
    }

    public static class Provider implements ParticleProvider<ColorTransitionParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public @Nullable Particle createParticle(ColorTransitionParticleOption options, ClientLevel level,
                                                 double x, double y, double z,
                                                 double xa, double ya, double za) {
            return new LightningTrailEmitterParticle(level, x, y, z, xa, ya, za, this.sprite, options);
        }
    }
}
