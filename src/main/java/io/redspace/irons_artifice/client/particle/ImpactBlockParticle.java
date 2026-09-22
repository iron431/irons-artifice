package io.redspace.irons_artifice.client.particle;

import io.redspace.irons_artifice.registry.ParticleRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

public class ImpactBlockParticle extends TerrainParticle {
    public ImpactBlockParticle(ClientLevel level, double x, double y, double z,
                               double xa, double ya, double za, BlockState state) {
        super(level, x, y, z, xa, ya, za, state);
        this.setParticleSpeed(xa, ya, za);
        this.emitter = level.getRandom().nextFloat() < 0.25;
        if (emitter) {
            this.blockState = state;
        }
    }

    @Nullable BlockState blockState;
    final boolean emitter;

    @Override
    public void tick() {
        super.tick();
        if (this.emitter && blockState != null) {
            if (age < 10) {
                level.addParticle(new BlockParticleOption(ParticleRegistry.BLOCK_DUST.get(), blockState), x, y, z, xd * 0.5, yd * 0.5, zd * 0.5);
            } else {
                blockState = null;
            }
        }
    }

    public static class Provider implements ParticleProvider<BlockParticleOption> {
        @Override
        public @Nullable Particle createParticle(BlockParticleOption options, ClientLevel level,
                                                 double x, double y, double z,
                                                 double xa, double ya, double za) {
            BlockState state = options.getState();
            if (state.isAir() || state.is(Blocks.MOVING_PISTON) || !state.shouldSpawnTerrainParticles()) {
                return null;
            }
            return new ImpactBlockParticle(level, x, y, z, xa, ya, za, state);
        }
    }
}
