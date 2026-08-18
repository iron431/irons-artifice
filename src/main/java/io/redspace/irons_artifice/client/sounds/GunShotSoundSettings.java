package io.redspace.irons_artifice.client.sounds;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;

public record GunShotSoundSettings(Holder<SoundEvent> soundEvent, float minPitch, float maxPitch, float start,
                                   float peak, float end) {

    public static GunShotSoundSettings standardShot(Holder<SoundEvent> soundEvent, float basePitch) {
        return new GunShotSoundSettings(soundEvent, basePitch - 0.1f, basePitch + 0.1f,
                -1, 0, 160f);
    }

    public static GunShotSoundSettings standardEcho(Holder<SoundEvent> soundEvent, float basePitch) {
        return new GunShotSoundSettings(soundEvent, basePitch - 0.1f, basePitch + 0.1f,
                32f, 80f, 192f);
    }


    public static final StreamCodec<RegistryFriendlyByteBuf, GunShotSoundSettings> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.SOUND_EVENT), GunShotSoundSettings::soundEvent,
            ByteBufCodecs.FLOAT, GunShotSoundSettings::minPitch,
            ByteBufCodecs.FLOAT, GunShotSoundSettings::maxPitch,
            ByteBufCodecs.FLOAT, GunShotSoundSettings::start,
            ByteBufCodecs.FLOAT, GunShotSoundSettings::peak,
            ByteBufCodecs.FLOAT, GunShotSoundSettings::end,
            GunShotSoundSettings::new);

    public static GunShotSoundSettings of(SoundEvent soundEvent, float minPitch, float maxPitch, float start, float peak, float end) {
        return new GunShotSoundSettings(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent), minPitch, maxPitch, start, peak, end);
    }
}
