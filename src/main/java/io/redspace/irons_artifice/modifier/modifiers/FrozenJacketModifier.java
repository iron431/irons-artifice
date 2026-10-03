package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.client.particle.ColorTransitionParticleOption;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.modifier.on_hit_handlers.FreezePostHit;
import io.redspace.irons_artifice.modifier.on_hit_handlers.FrozenShrapnelOnHit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public final class FrozenJacketModifier implements GunModifier {
    private static final int MUZZLE_TINT = 0xA8E6FF;

    @Override
    public void apply(ShotComponentMap components) {
        components.getOrCreate(ShotComponents.POST_HIT_EFFECTS).add(new FreezePostHit());
        components.getOrCreate(ShotComponents.ON_HIT).add(new FrozenShrapnelOnHit());
        components.getOrCreate(ShotComponents.PARTICLE_TRAIL).add(ColorTransitionParticleOption.bulletTrail(
                FrozenShrapnelOnHit.TRAIL_COLOR_FROM, FrozenShrapnelOnHit.TRAIL_COLOR_TO
        ));
        components.getOrCreate(ShotComponents.MUZZLE_FLASH).addTint(MUZZLE_TINT);
//        components.getOrCreate(ShotComponents.GUNSHOT_SOUND).addAccent(PlayableSound.of(IronsArtificeSounds.FROZEN_JACKET_ACCENT_SHOOT, 3f, 0.9f, 1.1f));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.component_type.freeze_on_hit", FreezePostHit.FREEZE_TICKS / 20).withStyle(ChatFormatting.AQUA));
        builder.accept(Component.translatable("irons_artifice.modifier.frozen_jacket",
                FrozenShrapnelOnHit.SHRAPNEL_COUNT,
                (int) (FrozenShrapnelOnHit.DAMAGE_FRACTION * 100)).withStyle(ChatFormatting.AQUA));
    }
}
