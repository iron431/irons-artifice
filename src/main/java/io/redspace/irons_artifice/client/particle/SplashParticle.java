package io.redspace.irons_artifice.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SplashParticle extends Particle {
    private final int color;

    public SplashParticle(ClientLevel level, double x, double y, double z,
                          double xa, double ya, double za, ColorParticleOption options) {
        super(level, x, y, z);
        this.setParticleSpeed(xa, ya, za);
        this.color = 0xFF000000
                | (as8BitChannel(options.getRed()) << 16)
                | (as8BitChannel(options.getGreen()) << 8)
                | as8BitChannel(options.getBlue());
        this.gravity = 1.5f;
        this.friction = 0.96f;
        this.hasPhysics = true;
        this.lifetime = 12 + this.random.nextInt(10);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }
        this.level.addAlwaysVisibleParticle(
                ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, this.color),
                true,
                this.x, this.y, this.z,
                (this.random.nextDouble() - 0.5) * 0.04,
                this.random.nextDouble() * 0.04,
                (this.random.nextDouble() - 0.5) * 0.04
        );
    }

    private static int as8BitChannel(float value) {
        return Mth.clamp((int) (value * 255.0F), 0, 255);
    }

    @Override
    public @Nonnull ParticleRenderType getRenderType() {
        return ParticleRenderType.NO_RENDER;
    }

    public static class Provider implements ParticleProvider<ColorParticleOption> {

        public Provider() {
        }

        @Override
        public @Nullable Particle createParticle(ColorParticleOption options, ClientLevel level,
                                                 double x, double y, double z,
                                                 double xa, double ya, double za) {
            return new SplashParticle(level, x, y, z, xa, ya, za, options);
        }
    }
}
