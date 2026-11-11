package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.ValueStackModifier;

import java.util.Map;

public final class OverchargedPowderModifier extends ValueStackModifier {
    public OverchargedPowderModifier() {
        super(Map.of(
                ShotComponents.BULLET_SPEED, new ValueModifier(0.25, ValueModifier.Operation.MULTIPLY_TOTAL),
                ShotComponents.CAMERA_RECOIL_MULTIPLIER, new ValueModifier(0.20, ValueModifier.Operation.MULTIPLY_TOTAL),
                ShotComponents.DAMAGE, new ValueModifier(0.15, ValueModifier.Operation.MULTIPLY_TOTAL)
        ));
    }
}
