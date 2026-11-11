package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public final class BufferSpringModifier implements GunModifier {
    @Override
    public void apply(ShotComponentMap components) {
        components.modifyValue(ShotComponents.CHARACTER_BLOWBACK, new ValueModifier(-1, ValueModifier.Operation.MULTIPLY_TOTAL));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.modifier.negate", Utils.getComponentTranslate(ShotComponents.CHARACTER_BLOWBACK)).withStyle(ChatFormatting.AQUA));
    }
}
