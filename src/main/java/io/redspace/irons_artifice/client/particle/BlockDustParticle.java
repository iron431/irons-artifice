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
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import javax.annotation.Nullable;

public class BlockDustParticle extends TextureSheetParticle {
    public BlockDustParticle(ClientLevel level, double x, double y, double z,
                             double xa, double ya, double za,
                             float r, float g, float b,
                             SpriteSet sprites) {
        super(level, x, y, z, xa, ya, za, sprites.first());
        this.setParticleSpeed(xa, ya, za);
        this.setColor(r, g, b);
        this.sprites = sprites;
        this.setSpriteFromAge(sprites);
        this.gravity *= 0.5f;
        this.quadSize *= 2f;
        updateAlpha(0);
    }

    protected final SpriteSet sprites;

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(sprites);
        this.xd *= 0.9;
        this.yd *= 0.9;
        this.zd *= 0.9;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTickTime) {
        updateAlpha(partialTickTime);
        super.render(buffer, camera, partialTickTime);
    }

    private void updateAlpha(float partialTickTime) {
        this.alpha = Mth.clamp(Mth.lerp((age + partialTickTime) / lifetime, 0.75f, 0), 0, 1f);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<BlockParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public @Nullable Particle createParticle(BlockParticleOption options, ClientLevel level,
                                                 double x, double y, double z,
                                                 double xa, double ya, double za, RandomSource random) {
            BlockState blockState = options.getState();
            if (!blockState.isAir() && blockState.getRenderShape() == RenderShape.INVISIBLE) {
                return null;
            } else {
                BlockPos pos = BlockPos.containing(x, y, z);
                int tintColor;
                if (blockState.getBlock() instanceof FallingBlock fallingBlock) {
                    tintColor = fallingBlock.getDustColor(blockState, level, pos);
                } else if (blockState.is(Tags.Blocks.GLASS_BLOCKS_COLORLESS) || blockState.is(Tags.Blocks.GLASS_PANES_COLORLESS)) {
                    // for some reason, undyed glass's color is black. hardcode to white-blue
                    tintColor = 0xd0eae9;
                } else {
                    int blockColor = Minecraft.getInstance().getBlockColors().getColor(blockState, level, pos, 0);
                    if (blockColor != -1) {
                        tintColor = blockColor;
                    } else {
                        tintColor = blockState.getMapColor(level, pos).col;
                    }
                }

                float intensity = random.nextIntBetweenInclusive(5, 7) * 0.1f;
                float r = (tintColor >> 16 & 0xFF) / 255.0F * intensity;
                float g = (tintColor >> 8 & 0xFF) / 255.0F * intensity;
                float b = (tintColor & 0xFF) / 255.0F * intensity;
                return new BlockDustParticle(level, x, y, z, xa, ya, za, r, g, b, this.sprite);
            }
        }
    }
}
