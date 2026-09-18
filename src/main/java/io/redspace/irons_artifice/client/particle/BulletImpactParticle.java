package io.redspace.irons_artifice.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BulletImpactParticle extends TextureSheetParticle {
    public BulletImpactParticle(ClientLevel level, double x, double y, double z,
                                double xa, double ya, double za, SpriteSet spriteSet, ColorTransitionParticleOption particleOptions) {
        super(level, x, y, z, xa, ya, za);
        this.setSprite(spriteSet.get(0, 1));
        this.setParticleSpeed(xa, ya, za);
        this.quadSize = 1;
        this.particleOptions = particleOptions;
        this.hasAlpha = particleOptions.getFromAlpha() != 1 || particleOptions.getToAlpha() != 1;
        this.lifetime = level.getRandom().nextIntBetweenInclusive(160, 360);
        setupColorAndAlpha(0);
    }

    protected final boolean hasAlpha;
    protected final ColorTransitionParticleOption particleOptions;

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTickTime) {
        setupColorAndAlpha(partialTickTime);
        super.render(buffer, camera, partialTickTime);
    }

    private void setupColorAndAlpha(float partialTickTime) {
        float f = getLifePercent(partialTickTime);
        if (hasAlpha) {
            this.alpha = Mth.lerp(f, particleOptions.getFromAlpha(), particleOptions.getToAlpha());
        }
        Vector3f fromColor = particleOptions.getFromColor();
        Vector3f toColor = particleOptions.getToColor();
        this.rCol = Mth.lerp(f, fromColor.x(), toColor.x());
        this.gCol = Mth.lerp(f, fromColor.y(), toColor.y());
        this.bCol = Mth.lerp(f, fromColor.z(), toColor.z());
    }

    @Override
    protected int getLightColor(float a) {
        float f = getLifePercent(a);
        float lightIntensity = Mth.lerp(f, particleOptions.getFromIntensity(), particleOptions.getToIntensity());
        int packed = super.getLightColor(a);
        if (lightIntensity == 0) {
            return packed;
        }
        int block = Mth.clamp((int) Mth.lerp(lightIntensity, LightTexture.block(packed), 240), 0, 15);
        int sky = Mth.clamp((int) Mth.lerp(lightIntensity, LightTexture.sky(packed), 240), 0, 15);
        return LightTexture.pack(block, sky);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else if (age % 10 == 0) {
            despawnIfFloating();
        }
    }

    private void despawnIfFloating() {
        BlockPos anchoredBlock = BlockPos.containing(new Vec3(this.x, this.y, this.z).add(new Vec3(xd, yd, zd).normalize().scale(0.25)));
        if (level.getBlockState(anchoredBlock).isAir()) {
            remove();
        }
    }

    @Override
    public float getQuadSize(float a) {
        return Mth.lerp(getLifePercent(a), particleOptions.getFromScale(), particleOptions.getToScale());
    }

    public float getLifePercent(float partialTick) {
        float f = Mth.clamp((age + partialTick + particleOptions.getOffset()) / lifetime, 0, 1);
        return 1 - (1 - f) * (1 - f) * (1 - f);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return hasAlpha ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : ParticleRenderType.PARTICLE_SHEET_OPAQUE;
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
            return new BulletImpactParticle(level, x, y, z, xa, ya, za, this.sprite, options);
        }
    }

}
