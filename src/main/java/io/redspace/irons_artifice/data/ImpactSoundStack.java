package io.redspace.irons_artifice.data;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ImpactSoundStack implements Copyable<ImpactSoundStack> {
    Optional<PlayableSound> baseBlockImpact;
    Optional<PlayableSound> baseEntityImpact;
    private final Set<PlayableSound> blockAccents = new HashSet<>();
    private final Set<PlayableSound> entityAccents = new HashSet<>();

    public ImpactSoundStack(Optional<PlayableSound> baseBlockImpact, Optional<PlayableSound> baseEntityImpact) {
        this.baseBlockImpact = baseBlockImpact;
        this.baseEntityImpact = baseEntityImpact;
    }


    public void addEntityAccent(PlayableSound options) {
        this.entityAccents.add(options);
    }

    public void addBlockAccent(PlayableSound options) {
        this.blockAccents.add(options);
    }

    public void addGenericAccent(PlayableSound options) {
        addEntityAccent(options);
        addBlockAccent(options);
    }

    public void setBaseBlockSound(PlayableSound sound) {
        this.baseBlockImpact = Optional.of(sound);
    }

    public void setBaseEntitySound(PlayableSound sound) {
        this.baseEntityImpact = Optional.of(sound);
    }

    public void playImpactSound(Level level, Vec3 pos, boolean entity) {
        var base = entity ? this.baseEntityImpact : this.baseBlockImpact;
        var accents = entity ? this.entityAccents : this.blockAccents;
        base.ifPresent(sound -> sound.play(level, pos, SoundSource.NEUTRAL));
        for (PlayableSound sound : accents) {
            sound.play(level, pos, SoundSource.NEUTRAL);
        }
    }

    @Override
    public ImpactSoundStack copy() {
        ImpactSoundStack copy = new ImpactSoundStack(this.baseBlockImpact, this.baseEntityImpact);
        copy.blockAccents.addAll(this.blockAccents);
        copy.entityAccents.addAll(this.entityAccents);
        return copy;
    }
}
