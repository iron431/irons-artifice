package io.redspace.irons_artifice.client.particle;

import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FairyDustParticle extends TextureSheetParticle {
    private final float angularSpeed;
    private final SpriteSet spriteSet;
    private final float phase;
    private final float radius;
    private final Vec3 u;
    private final Vec3 v;
    private double centerX;
    private double centerY;
    private double centerZ;

    public FairyDustParticle(ClientLevel level, double x, double y, double z,
                             double xa, double ya, double za, SpriteSet spriteSet, FairyDustParticleOption options) {
        super(level, x, y, z);
        this.setSprite(spriteSet.get(0, 1));
        this.spriteSet = spriteSet;
        this.phase = options.getPhase();
        this.radius = options.getRadius();
        this.centerX = x;
        this.centerY = y;
        this.centerZ = z;
        Vec3 random = Utils.randomVec3(0.01);
        this.setParticleSpeed(xa + random.x, ya + random.y, za + random.z);
        this.angularSpeed = 0.35f + (float) Math.random() * 0.05f;

        Vec3 axis = options.getAxis();
        double lenSq = axis.lengthSqr();
        Vec3 n = lenSq < 1.0E-8 ? new Vec3(0, 1, 0) : axis.normalize();
        Vec3 arbitrary = Math.abs(n.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        this.u = n.cross(arbitrary).normalize();
        this.v = n.cross(this.u);

        this.hasPhysics = false;
        this.gravity = 0f;
        this.friction = 1f;
        this.lifetime = 30;
        this.quadSize = 0.12f;
        this.rCol = 1f;
        this.gCol = 0.85f;
        this.bCol = 1f;

        applyOrbit(0);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    @Override
    public void tick() {
        this.gravity += 0.0005f;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.centerX += this.xd;
        this.centerY += this.yd;
        this.centerZ += this.zd;
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        this.yd -= this.gravity;
        applyOrbit(0);
        setSpriteFromAge(spriteSet);
        float life = (float) this.age / this.lifetime;
        this.alpha = 1f - life * life;
        if (age < 5) {
            float f = Mth.lerp(age / 5f, 0.5f, 1f);
            this.rCol = 1f * f;
            this.gCol = 0.85f * f;
            this.bCol = 1f * f;
        } else {
            this.rCol = 1f;
            this.gCol = 0.85f;
            this.bCol = 1f;
        }
    }

    private void applyOrbit(float partialTick) {
        float f = this.age + partialTick;
        float p = f / lifetime;
        float radius = p * this.radius + 0.05f;
        float angle = this.phase + (this.age + partialTick) * angularSpeed / radius;
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        this.x = this.centerX + this.u.x * cos * radius + this.v.x * sin * radius;
        this.y = this.centerY + this.u.y * cos * radius + this.v.y * sin * radius;
        this.z = this.centerZ + this.u.z * cos * radius + this.v.z * sin * radius;
    }

    @Override
    protected int getLightColor(float a) {
        float lightIntensity = (this.age + a) / lifetime;
//        lightIntensity = 1 - (1 - lightIntensity) * (1 - lightIntensity);
        int packed = super.getLightColor(a);
        int block = Mth.clamp((int) Mth.lerp(lightIntensity, LightTexture.block(packed), 240), 0, 15);
        int sky = Mth.clamp((int) Mth.lerp(lightIntensity, LightTexture.sky(packed), 240), 0, 15);
        return LightTexture.pack(block, sky);
    }


    @Override
    public float getQuadSize(float partialTick) {
        float life = Mth.clamp((this.age + partialTick) / this.lifetime, 0, 1);
        return this.quadSize * (1f - life * 0.5f);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<FairyDustParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public @Nullable Particle createParticle(FairyDustParticleOption options, ClientLevel level,
                                                 double x, double y, double z,
                                                 double xa, double ya, double za) {
            return new FairyDustParticle(level, x, y, z, xa, ya, za, this.sprite, options);
        }
    }
}
