package io.redspace.irons_artifice.data;

import io.redspace.irons_artifice.network.packets.ClientboundLocalSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ReloadCueStack {
    public static final ReloadCueStack EMPTY = new ReloadCueStack(List.of());

    private final List<ReloadCue> cues;

    private ReloadCueStack(List<ReloadCue> cues) {
        this.cues = List.copyOf(cues);
    }

    public static ReloadCueStack of(ReloadCue... cues) {
        List<ReloadCue> sorted = Arrays.stream(cues)
                .sorted(Comparator.comparingDouble(ReloadCue::seconds))
                .toList();
        return new ReloadCueStack(sorted);
    }

    public static ReloadCueStack of(PlayableSound sound, float... seconds) {
        ReloadCue[] cues = new ReloadCue[seconds.length];
        for (int i = 0; i < seconds.length; i++) {
            cues[i] = new ReloadCue(seconds[i], sound);
        }
        return of(cues);
    }

    public int size() {
        return cues.size();
    }

    public boolean isEmpty() {
        return cues.isEmpty();
    }

    public ReloadCue get(int index) {
        return cues.get(index);
    }

    /**
     * Plays cues whose timestamps fall in {@code [start, end)}.
     */
    public void playCuesBetween(Entity owner, Vec3 pos, SoundSource source, double start, double end, float pitchMultiplier) {
        for (ReloadCue cue : cues) {
            if (cue.seconds() >= start && cue.seconds() < end) {
                play(cue, owner, pos, source, pitchMultiplier);
            }
        }
    }

    private static void play(ReloadCue cue, Entity owner, Vec3 pos, SoundSource source, float pitchMultiplier) {
        var sound = cue.sound();
        var adjustedSound = new PlayableSound(sound.soundEventHolder(), sound.volume(), sound.minPitch() * pitchMultiplier, sound.maxPitch() * pitchMultiplier);
        owner.level().playSound(owner, pos.x, pos.y, pos.z, adjustedSound.soundEventHolder().value(), source, adjustedSound.volume(), adjustedSound.samplePitch(owner.getRandom()));
        if (owner instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new ClientboundLocalSoundPacket(source, adjustedSound));
        }
    }
}
