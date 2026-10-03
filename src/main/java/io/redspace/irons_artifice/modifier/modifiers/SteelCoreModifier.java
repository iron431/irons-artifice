package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.ValueStackModifier;

import java.util.Map;

public final class SteelCoreModifier extends ValueStackModifier {
    public SteelCoreModifier() {
        super(Map.of(
                ShotComponents.PIERCING, new ValueModifier(2, ValueModifier.Operation.ADD)
        ));
    }
}
