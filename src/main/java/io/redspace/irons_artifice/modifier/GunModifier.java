package io.redspace.irons_artifice.modifier;

import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Behaviour of a modifier item beyond plain numbers. Stat changes are data on the item, see {@link DataComponentRegistry#GUN_MODIFIER_STATS}.
 */
public interface GunModifier {
    /**
     * For modifier items that only change stats
     */
    GunModifier NONE = new GunModifier() {
    };

    /**
     * Changes the non-numeric parts of a shot. Runs for every shot composed with this modifier installed.
     */
    default void apply(ShotComponentMap components) {
    }

    default void getDescriptionText(Consumer<Component> builder) {
    }

    /**
     * Builds the whole modifier tooltip. Override to place lines ahead of the stat lines.
     *
     * @param statLines appends the lines generated from the item's stat changes
     */
    default void appendTooltip(Consumer<Component> builder, Runnable statLines) {
        statLines.run();
        getDescriptionText(builder);
    }

    /**
     * Item components to put on the gun while this modifier is installed. Should not contain attribute modifiers: the gun's own stats are
     * put back regardless, but the patch would replace whatever else that particular stack carried, and removing the modifier would not
     * restore it. Use {@link DataComponentRegistry#GUN_MODIFIER_STATS} for stat changes.
     */
    default Optional<DataComponentPatch> getPatch() {
        return Optional.empty();
    }
}
