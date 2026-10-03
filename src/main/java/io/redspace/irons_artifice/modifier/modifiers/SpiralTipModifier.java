package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.GunModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public final class SpiralTipModifier implements GunModifier {
    @Override
    public void apply(ShotComponentMap components) {
        components.modifyValue(ShotComponents.UNDERWATER_DRAG, new ValueModifier(1, ValueModifier.Operation.ADD));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.modifier.spiral_tip").withStyle(ChatFormatting.AQUA));
    }
}
