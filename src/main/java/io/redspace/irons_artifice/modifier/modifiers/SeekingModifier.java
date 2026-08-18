package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.ParticleStack;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.ValueStackModifier;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.function.Consumer;

public final class SeekingModifier extends ValueStackModifier {
    private static final double BASE_SPEED = 3;

    public SeekingModifier() {
        super(Map.of(
                ShotComponents.SEEKING, new ValueModifier(0.10, ValueModifier.Operation.ADD, ValueModifier.Type.BENEFICIAL),
                ShotComponents.GRAVITY, new ValueModifier(-0.25, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL)
        ));
    }

    @Override
    public void apply(ShotComponentMap components) {
        super.apply(components);
        components.set(ShotComponents.BULLET_SPEED,
                components.getOrDefault(ShotComponents.BULLET_SPEED).withBase(BASE_SPEED));
//        components.getOrCreate(ShotComponents.PARTICLE_TRAIL).add(
//                ColorTransitionParticleOption.bulletTrail(0xcb9fff, 0x420036)
//        );
        components.getOrCreate(ShotComponents.PARTICLE_TRAIL).addAccent(
                new ParticleStack.ParticleAccent(ParticleTypes.ENCHANT, 0.125)
        );
//        components.getOrCreate(ShotComponents.MUZZLE_FLASH).addTint(0xcb9fff);
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        super.getDescriptionText(builder);
        builder.accept(Component.translatable("irons_artifice.value.set_base", Utils.getComponentTranslate(ShotComponents.BULLET_SPEED), BASE_SPEED).withStyle(ChatFormatting.YELLOW));
    }
}
