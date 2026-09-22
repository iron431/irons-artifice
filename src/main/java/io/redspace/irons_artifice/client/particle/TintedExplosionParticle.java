package io.redspace.irons_artifice.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;
import javax.annotation.Nullable;

public class TintedExplosionParticle extends MuzzleFlashParticle {


    public TintedExplosionParticle(ClientLevel level, double x, double y, double z, double xa, double ya, double za, SpriteSet sprites, float tintR, float tintG, float tintB) {
        super(level, x, y, z, xa, ya, za, sprites, tintR, tintG, tintB);
        this.quadSize = 3;
        this.lifetime = 4;
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
            return new TintedExplosionParticle(level, x, y, z, xa, ya, za, this.sprite, options.r(), options.g(), options.b());
        }
    }
}
