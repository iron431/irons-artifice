package io.redspace.irons_artifice.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BulletTrailParticle extends TextureSheetParticle {
    public BulletTrailParticle(ClientLevel level, double x, double y, double z,
                               double xa, double ya, double za, SpriteSet spriteSet, ColorTransitionParticleOption particleOptions) {
        super(level, x, y, z, xa, ya, za);
        this.setSprite(spriteSet.get(0, 1));
        this.setParticleSpeed(xa, ya, za);
        this.quadSize = 1;
        this.particleOptions = particleOptions;
        this.hasAlpha = particleOptions.getFromAlpha() != 1 || particleOptions.getToAlpha() != 1;
        this.lifetime = 15;
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
        this.rCol = Mth.lerp(f * f, fromColor.x(), toColor.x());
        this.gCol = Mth.lerp(f * f, fromColor.y(), toColor.y());
        this.bCol = Mth.lerp(f * f, fromColor.z(), toColor.z());
    }

    @Override
    protected int getLightColor(float a) {
        float f = getLifePercent(a);
        float lightIntensity = Mth.lerp(f, particleOptions.getFromIntensity(), particleOptions.getToIntensity());
        int packed = super.getLightColor(a);
        if (lightIntensity == 0) {
            return packed;
        }
        return Utils.brightenLightTexture(packed, lightIntensity);
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
            return new BulletTrailParticle(level, x, y, z, xa, ya, za, this.sprite, options);
        }
    }

}
