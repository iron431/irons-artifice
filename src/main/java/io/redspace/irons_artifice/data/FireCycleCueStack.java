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

public class FireCycleCueStack {
    public static final FireCycleCueStack EMPTY = new FireCycleCueStack(List.of());

    private final List<FireCycleCue> cues;

    private FireCycleCueStack(List<FireCycleCue> cues) {
        this.cues = List.copyOf(cues);
    }

    public static FireCycleCueStack of(FireCycleCue... cues) {
        List<FireCycleCue> sorted = Arrays.stream(cues)
                .sorted(Comparator.comparingDouble(FireCycleCue::percent))
                .toList();
        return new FireCycleCueStack(sorted);
    }

    public int size() {
        return cues.size();
    }

    public boolean isEmpty() {
        return cues.isEmpty();
    }

    public FireCycleCue get(int index) {
        return cues.get(index);
    }

    /**
     * Plays any cues crossed while advancing to {@code percent}, starting at {@code cueIndex}.
     *
     * @return the next cue index to fire
     */
    public int playDueCues(Entity owner, Vec3 pos, SoundSource source, float percent, int cueIndex, float pitchMultiplier) {
        while (cueIndex < cues.size() && percent >= cues.get(cueIndex).percent()) {
            var sound = cues.get(cueIndex).sound();
            var adjustedSound = new PlayableSound(sound.soundEventHolder(), sound.volume(), sound.minPitch() * pitchMultiplier, sound.maxPitch() * pitchMultiplier);
            if (!owner.level().isClientSide()) {
                owner.level().playSound(owner, pos.x, pos.y, pos.z, adjustedSound.soundEventHolder().value(), source, adjustedSound.volume(), adjustedSound.samplePitch(owner.getRandom()));
                if (owner instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer, new ClientboundLocalSoundPacket(source, adjustedSound));
                }
            }
            cueIndex++;
        }
        return cueIndex;
    }
}
