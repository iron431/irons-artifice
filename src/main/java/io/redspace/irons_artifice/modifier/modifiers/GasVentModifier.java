package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.ValueStackModifier;

import java.util.Map;

public final class GasVentModifier extends ValueStackModifier {
    public GasVentModifier() {
        super(Map.of(
                ShotComponents.CAMERA_RECOIL_MULTIPLIER, new ValueModifier(-0.5, ValueModifier.Operation.MULTIPLY_TOTAL)
        ));
    }
}
