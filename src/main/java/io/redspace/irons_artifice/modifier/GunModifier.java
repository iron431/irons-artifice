package io.redspace.irons_artifice.modifier;

import io.redspace.irons_artifice.data.ShotComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.Consumer;

public interface GunModifier {
    void apply(ShotComponentMap components);

    default void getDescriptionText(Consumer<Component> builder) {
    }

    /** Stack tag entries, keyed as they are stored on the gun, that this modifier adds while slotted. */
    default Optional<CompoundTag> getPatch() {
        return Optional.empty();
    }
}
