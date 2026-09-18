package io.redspace.irons_artifice.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MuzzleFlashParticle extends TextureSheetParticle {
    private static final String FIRE_SUFFIX = "_fire";
    private static final String TINTED_MASKED_SUFFIX = "_tinted_masked";
    private static final String WHITE_MASK_SUFFIX = "_white_mask";

    private final SpriteSet sprites;
    private final boolean tinted;
    private final boolean mirrorHorizontal;
    private final boolean mirrorVertical;

    @Nullable
    private TextureAtlasSprite whiteMaskSprite;

    public MuzzleFlashParticle(ClientLevel level, double x, double y, double z,
                               double xa, double ya, double za, SpriteSet sprites,
                               float tintR, float tintG, float tintB) {
        super(level, x, y, z, xa, ya, za);
        this.setSprite(sprites.get(0, 1));
        this.sprites = sprites;
        this.tinted = !(tintR < 0f || tintG < 0 || tintB < 0);
        this.lifetime = 3;
        this.xd = xa;
        this.yd = ya;
        this.zd = za;
        this.quadSize = 1;
        this.rCol = tinted ? tintR : 1f;
        this.gCol = tinted ? tintG : 1f;
        this.bCol = tinted ? tintB : 1f;
        this.mirrorHorizontal = level.getRandom().nextBoolean();
        this.mirrorVertical = level.getRandom().nextBoolean();
        this.roll = level.getRandom().nextInt(4) * Mth.HALF_PI;
        this.oRoll = roll;
        updateSprites();
    }

    @Override
    protected float getU0() {
        return mirrorHorizontal ? super.getU1() : super.getU0();
    }

    @Override
    protected float getU1() {
        return mirrorHorizontal ? super.getU0() : super.getU1();
    }

    @Override
    protected float getV0() {
        return mirrorVertical ? super.getV1() : super.getV0();
    }

    @Override
    protected float getV1() {
        return mirrorVertical ? super.getV0() : super.getV1();
    }

    @Override
    public void tick() {
        super.tick();
        updateSprites();
    }

    @Nullable
    private static TextureAtlas particleAtlas() {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_PARTICLES);
        return texture instanceof TextureAtlas atlas ? atlas : null;
    }

    private void updateSprites() {
        TextureAtlasSprite fireSprite = sprites.get(age, lifetime);
        if (!tinted) {
            setSprite(fireSprite);
            whiteMaskSprite = null;
            return;
        }

        ResourceLocation fireName = fireSprite.contents().name();
        String path = fireName.getPath();
        TextureAtlas atlas = particleAtlas();
        if (atlas == null || !path.endsWith(FIRE_SUFFIX)) {
            setSprite(fireSprite);
            whiteMaskSprite = null;
            return;
        }

        String basePath = path.substring(0, path.length() - FIRE_SUFFIX.length());
        setSprite(atlas.getSprite(fireName.withPath(basePath + TINTED_MASKED_SUFFIX)));
        whiteMaskSprite = atlas.getSprite(fireName.withPath(basePath + WHITE_MASK_SUFFIX));
    }

    @Override
    protected void renderRotatedQuad(
            VertexConsumer buffer,
            Quaternionf rotation,
            float x,
            float y,
            float z,
            float partialTickTime
    ) {
        // Tinted masked pass (uses particle tint color), or single untinted fire pass.
        super.renderRotatedQuad(buffer, rotation, x, y, z, partialTickTime);

        if (!tinted || whiteMaskSprite == null) {
            return;
        }

        // White-mask highlight pass: same transform, white color, white_mask UVs.
        float u0 = mirrorHorizontal ? whiteMaskSprite.getU1() : whiteMaskSprite.getU0();
        float u1 = mirrorHorizontal ? whiteMaskSprite.getU0() : whiteMaskSprite.getU1();
        float v0 = mirrorVertical ? whiteMaskSprite.getV1() : whiteMaskSprite.getV0();
        float v1 = mirrorVertical ? whiteMaskSprite.getV0() : whiteMaskSprite.getV1();
        float size = getQuadSize(partialTickTime);
        int light = getLightColor(partialTickTime);
        renderMaskVertex(buffer, rotation, x, y, z, 1.0F, -1.0F, size, u1, v1, light);
        renderMaskVertex(buffer, rotation, x, y, z, 1.0F, 1.0F, size, u1, v0, light);
        renderMaskVertex(buffer, rotation, x, y, z, -1.0F, 1.0F, size, u0, v0, light);
        renderMaskVertex(buffer, rotation, x, y, z, -1.0F, -1.0F, size, u0, v1, light);
    }

    private void renderMaskVertex(VertexConsumer buffer, Quaternionf rotation,
                                  float x, float y, float z,
                                  float offsetX, float offsetY, float size,
                                  float u, float v, int light) {
        Vector3f corner = new Vector3f(offsetX, offsetY, 0.0F).rotate(rotation).mul(size).add(x, y, z);
        buffer.addVertex(corner.x(), corner.y(), corner.z()).setUv(u, v).setColor(1.0F, 1.0F, 1.0F, alpha).setLight(light);
    }

    @Override
    protected int getLightColor(float a) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<MuzzleFlashParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public @Nullable Particle createParticle(MuzzleFlashParticleOption options, ClientLevel level,
                                                 double x, double y, double z,
                                                 double xa, double ya, double za) {
            return new MuzzleFlashParticle(level, x, y, z, xa, ya, za, this.sprite, options.r(), options.g(), options.b());
        }
    }
}
