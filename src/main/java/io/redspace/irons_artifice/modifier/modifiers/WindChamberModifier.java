package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.ValueStackModifier;

import java.util.Map;

public final class WindChamberModifier extends ValueStackModifier {
    public WindChamberModifier() {
        super(Map.of(
                ShotComponents.CHARACTER_BLOWBACK, new ValueModifier(0.5, ValueModifier.Operation.MULTIPLY_TOTAL),
                ShotComponents.IN_AIR_PENALTY, new ValueModifier(-0.5, ValueModifier.Operation.MULTIPLY_TOTAL)
        ));
    }
}
