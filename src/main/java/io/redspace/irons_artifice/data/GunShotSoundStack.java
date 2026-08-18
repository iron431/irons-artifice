package io.redspace.irons_artifice.data;

import io.redspace.irons_artifice.client.sounds.GunShotSoundSettings;
import io.redspace.irons_artifice.network.packets.ClientboundGunshotSoundPacket;
import io.redspace.irons_artifice.registry.SoundRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GunShotSoundStack implements Copyable<GunShotSoundStack> {
    GunShotSoundSettings baseSound;
    GunShotSoundSettings echoSound;
    PlayableSound dryFireSound;
    private final Set<PlayableSound> accents = new HashSet<>();

    public GunShotSoundStack(GunShotSoundSettings baseSound, GunShotSoundSettings echoSound, PlayableSound dryFireSound) {
        this.baseSound = baseSound;
        this.dryFireSound = dryFireSound;
        this.echoSound = echoSound;
    }

    public GunShotSoundStack(GunShotSoundSettings baseSound, PlayableSound dryFireSound) {
        this(baseSound, new GunShotSoundSettings(SoundRegistry.BULLET_ECHO_GENERIC, 0.7f, 0.9f, 64f, 128f, 192f), dryFireSound);
    }

    public void addAccent(PlayableSound options) {
        this.accents.add(options);
    }

    public void setBaseSound(GunShotSoundSettings sound) {
        this.baseSound = sound;
    }

    public PlayableSound getDryFireSound() {
        return dryFireSound;
    }

    public List<PlayableSound> getAccentSounds() {
        return List.copyOf(accents);
    }

    public void playGunShotSound(Level level, Vec3 pos) {
        for (PlayableSound sound : accents) {
            sound.play(level, pos, SoundSource.NEUTRAL);
        }
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersNear(serverLevel, null, pos.x, pos.y, pos.z, 12 * 16,
                    new ClientboundGunshotSoundPacket(SoundSource.NEUTRAL, pos.x, pos.y, pos.z, List.of(this.baseSound, this.echoSound)));
        }
    }

    public void playDryFireSound(Level level, Vec3 pos) {
        this.dryFireSound.play(level, pos, SoundSource.NEUTRAL);
    }

    @Override
    public GunShotSoundStack copy() {
        GunShotSoundStack copy = new GunShotSoundStack(this.baseSound, this.echoSound, this.dryFireSound);
        copy.accents.addAll(this.accents);
        return copy;
    }
}
