package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.ValueStackModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.function.Consumer;

public final class BreachModifier extends ValueStackModifier {
    public BreachModifier() {
        super(Map.of(
                ShotComponents.BLOCK_DAMAGE_MULTIPLIER, new ValueModifier(0.5, ValueModifier.Operation.MULTIPLY_TOTAL)
        ));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("irons_artifice.component_type.enables_block_damage").withStyle(ChatFormatting.AQUA));
        super.getDescriptionText(builder);
    }

    @Override
    public void apply(ShotComponentMap components) {
        super.apply(components);
        components.set(ShotComponents.BREAKS_BLOCKS, true);
    }
}
