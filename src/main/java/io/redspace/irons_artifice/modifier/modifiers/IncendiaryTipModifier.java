package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.client.particle.ColorTransitionParticleOption;
import io.redspace.irons_artifice.data.ParticleStack;
import io.redspace.irons_artifice.data.PlayableSound;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.data.MuzzleFlashSettings;
import io.redspace.irons_artifice.modifier.ValueStackModifier;
import io.redspace.irons_artifice.modifier.on_hit_handlers.IgnitePostHit;
import io.redspace.irons_artifice.registry.ParticleRegistry;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.Map;
import java.util.function.Consumer;

public final class IncendiaryTipModifier extends ValueStackModifier {
    public static final int BURN_TICKS_PER = 5 * 20;

    public IncendiaryTipModifier() {
        super(Map.of(
                ShotComponents.DAMAGE, new ValueModifier(0.15, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL)
        ));
    }

    @Override
    public void apply(ShotComponentMap components) {
        super.apply(components);
        components.getOrCreate(ShotComponents.POST_HIT_EFFECTS)
                .getOrCreate(IgnitePostHit.class, () -> new IgnitePostHit(0))
                .addDuration(BURN_TICKS_PER);
        components.getOrCreate(ShotComponents.PARTICLE_TRAIL).add(
                new ColorTransitionParticleOption(ParticleRegistry.BULLET_TRAIL.get(), 0xfffa87, 0xfa0a00,
                        1f, 0.5f,
                        1f, 1f,
                        0.5f, 0f,
                        0f)
        );
        components.getOrCreate(ShotComponents.PARTICLE_TRAIL).addAccent(
                new ParticleStack.ParticleAccent(ParticleTypes.SMOKE, 0.250)
        );
        components.getOrCreate(ShotComponents.GUNSHOT_SOUND).addAccent(PlayableSound.of(PlayableSound.holder(SoundEvents.BLAZE_SHOOT), 4f, 1.2f, 1.4f));
        components.getOrCreate(ShotComponents.MUZZLE_FLASH).addTint(MuzzleFlashSettings.UNTINTED);
        components.getOrCreate(ShotComponents.ON_HIT).add(
                (level, bullet, hitResult, accumulator) -> {
                    // fixme: this is dumb
                    var vec = hitResult.getLocation();
                    Utils.spawnParticles(level, ParticleTypes.LAVA, vec.x, vec.y, vec.z, 3, 0, 0, 0, 0.2, true);
                }
        );
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        super.getDescriptionText(builder);
        builder.accept(Component.translatable("irons_artifice.component_type.burn_duration_on_hit", BURN_TICKS_PER / 20).withStyle(ChatFormatting.GREEN));
    }
}