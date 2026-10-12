package io.redspace.irons_artifice.client.sounds;

import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class GunShotSoundInstance extends AbstractSoundInstance {

    public GunShotSoundInstance(GunShotSoundSettings sound, SoundSource source, RandomSource random, Vec3 position) {
        super(sound.soundEvent().value(), source, random);
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        double distance = Minecraft.getInstance().player.position().subtract(position).length();
        this.attenuation = Attenuation.NONE;
        this.volume = Mth.clamp(Utils.triangleInterpolate((float) distance, sound.start(), sound.peak(), sound.end()) * 1.1f, 0, 1);
        this.pitch = Mth.lerp(random.nextFloat(), sound.minPitch(), sound.maxPitch());
        this.relative = false;
//        this.delay = (int) Mth.clamp((distance - 64) / 16, 0, 40);
    }
}
