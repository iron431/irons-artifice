package io.redspace.irons_artifice.modifier;

import io.redspace.irons_artifice.data.ShotComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.Consumer;

public interface GunModifier {
    void apply(ShotComponentMap components);

    default void getDescriptionText(Consumer<Component> builder) {
    }

    default Optional<DataComponentPatch> getPatch() {
        return Optional.empty();
    }
}
