package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.sounds.GunShotSoundSettings;
import io.redspace.irons_artifice.data.GunShotSoundStack;
import io.redspace.irons_artifice.data.MuzzleFlashSettings;
import io.redspace.irons_artifice.data.ParticleBurst;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class SuppressorAttachmentModifier implements GunModifier {

    @Override
    public void apply(ShotComponentMap components) {
        GunShotSoundStack gunShotSoundStack = components.getOrDefault(ShotComponents.GUNSHOT_SOUND);
        GunShotSoundSettings baseSettings = gunShotSoundStack.getBaseSound();
        GunShotSoundSettings echoSettings = gunShotSoundStack.getEchoSound();
        gunShotSoundStack.setBaseSound(new GunShotSoundSettings(
                baseSettings.soundEvent(),
                baseSettings.minPitch(), baseSettings.maxPitch(), -64, -32, 32
        ));
        gunShotSoundStack.setEchoSound(new GunShotSoundSettings(
                gunShotSoundStack.getEchoSound().soundEvent(),
                echoSettings.minPitch(), echoSettings.maxPitch(),
                -32, -16, baseSettings.end() - 32
        ));
        MuzzleFlashSettings muzzleFlashSettings = components.getOrCreate(ShotComponents.MUZZLE_FLASH);
        muzzleFlashSettings = new MuzzleFlashSettings(
                muzzleFlashSettings.types(),
                0.75f,
                muzzleFlashSettings.tints(),
                muzzleFlashSettings.airBursts(),
                muzzleFlashSettings.underwaterBursts()
        );
        muzzleFlashSettings.airBursts().add(ParticleBurst.SMOKE);
        muzzleFlashSettings.types().clear();
        components.set(ShotComponents.MUZZLE_FLASH, muzzleFlashSettings);
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.modifier.bayonet").withStyle(ChatFormatting.AQUA));
    }

    @Override
    public Optional<DataComponentPatch> getPatch() {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        builder.set(DataComponentRegistry.ATTACHMENT.get(), new AttachmentMap(Map.of(
                "attachment_muzzle", IronsArtifice.id("suppressor")
        )));
        return Optional.of(builder.build());
    }
}
