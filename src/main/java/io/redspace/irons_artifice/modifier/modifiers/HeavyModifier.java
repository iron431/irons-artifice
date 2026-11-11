package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.ValueStackModifier;

import java.util.Map;

public final class HeavyModifier extends ValueStackModifier {
    public HeavyModifier() {
        super(Map.of(
                ShotComponents.BULLET_SPEED, new ValueModifier(-0.10, ValueModifier.Operation.MULTIPLY_TOTAL),
                ShotComponents.KNOCKBACK, new ValueModifier(0.5, ValueModifier.Operation.ADD)
        ));
    }
}
