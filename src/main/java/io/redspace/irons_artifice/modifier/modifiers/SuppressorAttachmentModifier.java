package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.api.GunBones;
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
                baseSettings.minPitch(), baseSettings.maxPitch(), -49, -48, 32
        ));
        gunShotSoundStack.setEchoSound(new GunShotSoundSettings(
                gunShotSoundStack.getEchoSound().soundEvent(),
                echoSettings.minPitch(), echoSettings.maxPitch(),
                -33, -32, Math.min(baseSettings.end(), 112)
        ));
        MuzzleFlashSettings muzzleFlashSettings = components.getOrCreate(ShotComponents.MUZZLE_FLASH);
        muzzleFlashSettings.airBursts().add(ParticleBurst.SMOKE);
        muzzleFlashSettings.types().clear();
        components.set(ShotComponents.MUZZLE_OFFSET, components.getOrDefault(ShotComponents.MUZZLE_OFFSET).withBase(11 / 16f));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.modifier.suppressor").withStyle(ChatFormatting.AQUA));
        builder.accept(Component.translatable("irons_artifice.modifier.flash_hider").withStyle(ChatFormatting.AQUA));
    }

    @Override
    public Optional<DataComponentPatch> getPatch() {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        builder.set(DataComponentRegistry.ATTACHMENT.get(), new AttachmentMap(Map.of(
                GunBones.SOCKET_MUZZLE, IronsArtifice.id("suppressor")
        )));
        return Optional.of(builder.build());
    }
}
