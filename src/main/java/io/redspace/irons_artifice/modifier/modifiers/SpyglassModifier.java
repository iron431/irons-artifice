package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.modifier.GunModifier;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;

import java.util.Optional;

public class SpyglassModifier implements GunModifier {
    final DataComponentPatch eat;

    public SpyglassModifier() {
        this.eat = DataComponentPatch.builder()
                .set(DataComponents.FOOD, new FoodProperties(1, 1, true))
                .set(DataComponents.CONSUMABLE, Consumable.builder().build())
                .build();
    }

    @Override
    public void apply(ShotComponentMap components) {

    }

    @Override
    public Optional<DataComponentPatch> getPatch() {
        return Optional.of(eat);
    }
}
