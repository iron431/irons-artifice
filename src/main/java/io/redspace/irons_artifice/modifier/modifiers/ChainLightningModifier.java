package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.client.particle.ColorTransitionParticleOption;
import io.redspace.irons_artifice.data.PlayableSound;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.modifier.on_hit_handlers.ChainLightningOnHit;
import io.redspace.irons_artifice.registry.ParticleRegistry;
import io.redspace.irons_artifice.registry.SoundRegistry;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public final class ChainLightningModifier implements GunModifier {
    public static final int LIGHTNING_COLOR = 0xcef8ff;
    public static final int LIGHTNING_FADE_COLOR = 0x00f8ff;
    public static final int MUZZLE_FLASH_COLOR = 0x00f8ff;
    private static final int coolColor = LIGHTNING_COLOR << 4;

    /**
     * Lazily created to avoid accessing DeferredHolder during class initialization.
     * Particle types are not bound until after RegisterEvent, so static field
     * initializers that call ParticleRegistry.X.get() trigger
     * NullPointerException: Trying to access unbound value at ItemRegistry clinit.
     */
    public static ParticleOptions getLightningEmitter() {
        return new ColorTransitionParticleOption(ParticleRegistry.LIGHTNING_TRAIL.get(), LIGHTNING_COLOR, LIGHTNING_FADE_COLOR, 1f, 0f, 1f, 1f, 0.5f, 0f, 0);
    }

    public static ParticleOptions getLightningTrail() {
        return new ColorTransitionParticleOption(ParticleRegistry.BULLET_TRAIL.get(), LIGHTNING_COLOR, LIGHTNING_FADE_COLOR, 1f, 0f, 1f, 1f, 0.5f, 0f, 0);
    }

    @Override
    public void apply(ShotComponentMap components) {
        components.getOrCreate(ShotComponents.ON_HIT).add(new ChainLightningOnHit());
        components.getOrCreate(ShotComponents.PARTICLE_TRAIL).add(getLightningEmitter());
        components.getOrCreate(ShotComponents.IMPACT_SOUND).addGenericAccent(PlayableSound.of(SoundRegistry.LIGHTNING_ACCENT_IMPACT, 2f, .9f, 1.1f));
        components.getOrCreate(ShotComponents.GUNSHOT_SOUND).addAccent(PlayableSound.of(SoundRegistry.LIGHTNING_ACCENT_SHOOT, 3f, 1.6f, 1.8f));
        components.getOrCreate(ShotComponents.MUZZLE_FLASH).addTint(Utils.vector3fFromRGB24(MUZZLE_FLASH_COLOR));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.modifier.chain_lightning", ChainLightningOnHit.CHAIN_COUNT, (int) (ChainLightningOnHit.DAMAGE_MULTIPLIER * 100)).withStyle(ChatFormatting.AQUA));
    }
}
