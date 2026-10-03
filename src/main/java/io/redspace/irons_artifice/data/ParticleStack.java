package io.redspace.irons_artifice.data;

import io.redspace.irons_artifice.client.particle.ColorTransitionParticleOption;
import io.redspace.irons_artifice.registry.IronsArtificeRegistries;
import net.minecraft.core.particles.ParticleOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParticleStack implements Copyable<ParticleStack> {
    public record ParticleAccent(ParticleOptions options, double chance) {
    }

    private static final List<ParticleOptions> EMPTY = List.of(new ColorTransitionParticleOption(
            IronsArtificeRegistries.Particles.BULLET_TRAIL.get(), 0xffc600, 0x04f0b00, 1f, 0f, 1f, 1f, 0.5f, 0f, 0
    ));
    private final List<ParticleOptions> particles = new ArrayList<>();
    private final Set<ParticleAccent> accents = new HashSet<>();

    public void add(ParticleOptions options) {
        this.particles.add(options);
    }

    public void addAccent(ParticleAccent accent) {
        this.accents.add(accent);
    }

    public List<ParticleOptions> getParticles() {
        return List.copyOf(particles.isEmpty() ? EMPTY : particles);
    }

    public List<ParticleAccent> getAccents() {
        return List.copyOf(accents);
    }

    @Override
    public ParticleStack copy() {
        ParticleStack copy = new ParticleStack();
        copy.particles.addAll(this.particles);
        copy.accents.addAll(this.accents);
        return copy;
    }
}
