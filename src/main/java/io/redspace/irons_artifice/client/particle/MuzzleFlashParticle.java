package io.redspace.irons_artifice.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.RandomSource;
import javax.annotation.Nullable;

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
        super(level, x, y, z, xa, ya, za, sprites.get(0));
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
        this.roll = level.getRandom().nextInt(4) * ((float) (Math.PI / 2));
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

    private void updateSprites() {
        TextureAtlasSprite fireSprite = sprites.get(age, lifetime);
        if (!tinted) {
            setSprite(fireSprite);
            whiteMaskSprite = null;
            return;
        }

        ResourceLocation fireName = fireSprite.contents().name();
        String path = fireName.getPath();
        if (!path.endsWith(FIRE_SUFFIX)) {
            setSprite(fireSprite);
            whiteMaskSprite = null;
            return;
        }

        String basePath = path.substring(0, path.length() - FIRE_SUFFIX.length());
        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_PARTICLES);
        setSprite(atlas.getSprite(fireName.withPath(basePath + TINTED_MASKED_SUFFIX)));
        whiteMaskSprite = atlas.getSprite(fireName.withPath(basePath + WHITE_MASK_SUFFIX));
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTickTime) {
        // Tinted masked pass (uses particle tint color), or single untinted fire pass.
        super.render(buffer, camera, partialTickTime);

        if (!tinted || whiteMaskSprite == null) {
            return;
        }

        // White-mask highlight pass: same transform, white color, white_mask UVs.
        TextureAtlasSprite originalSprite = this.sprite;
        float r = this.rCol;
        float g = this.gCol;
        float b = this.bCol;
        this.setSprite(whiteMaskSprite);
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        super.render(buffer, camera, partialTickTime);
        this.setSprite(originalSprite);
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
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
