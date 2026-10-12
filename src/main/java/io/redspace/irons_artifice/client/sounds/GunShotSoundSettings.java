package io.redspace.irons_artifice.client.sounds;

import io.redspace.irons_artifice.data.PlayableSound;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public record GunShotSoundSettings(Holder<SoundEvent> soundEvent, float minPitch, float maxPitch, float start,
                                   float peak, float end) {

    public static GunShotSoundSettings standardShot(Holder<SoundEvent> soundEvent, float basePitch) {
        return new GunShotSoundSettings(soundEvent, basePitch - 0.1f, basePitch + 0.1f,
                -1, 0, 128f);
    }

    public static GunShotSoundSettings standardEcho(Holder<SoundEvent> soundEvent, float basePitch) {
        return new GunShotSoundSettings(soundEvent, basePitch - 0.1f, basePitch + 0.1f,
                32f, 80f, 192f);
    }

    public static GunShotSoundSettings standardShot(Supplier<SoundEvent> soundEvent, float basePitch) {
        return standardShot(PlayableSound.holder(soundEvent), basePitch);
    }

    public static GunShotSoundSettings standardEcho(Supplier<SoundEvent> soundEvent, float basePitch) {
        return standardEcho(PlayableSound.holder(soundEvent), basePitch);
    }


    public static void write(FriendlyByteBuf buf, GunShotSoundSettings settings) {
        buf.writeId(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), settings.soundEvent, (b, sound) -> sound.writeToNetwork(b));
        buf.writeFloat(settings.minPitch);
        buf.writeFloat(settings.maxPitch);
        buf.writeFloat(settings.start);
        buf.writeFloat(settings.peak);
        buf.writeFloat(settings.end);
    }

    public static GunShotSoundSettings read(FriendlyByteBuf buf) {
        return new GunShotSoundSettings(
                buf.readById(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), SoundEvent::readFromNetwork),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat());
    }

    public static GunShotSoundSettings of(SoundEvent soundEvent, float minPitch, float maxPitch, float start, float peak, float end) {
        return new GunShotSoundSettings(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent), minPitch, maxPitch, start, peak, end);
    }

    public static GunShotSoundSettings of(Supplier<SoundEvent> soundEvent, float minPitch, float maxPitch, float start, float peak, float end) {
        return new GunShotSoundSettings(PlayableSound.holder(soundEvent), minPitch, maxPitch, start, peak, end);
    }
}
