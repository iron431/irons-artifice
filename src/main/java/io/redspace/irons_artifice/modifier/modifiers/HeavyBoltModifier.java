package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.ValueStackModifier;

import java.util.Map;

public final class HeavyBoltModifier extends ValueStackModifier {
    public HeavyBoltModifier() {
        super(Map.of(
                ShotComponents.FIRE_RATE, new ValueModifier(-0.25, ValueModifier.Operation.MULTIPLY_TOTAL),
                ShotComponents.RELOAD_SPEED_MULTIPLIER, new ValueModifier(-0.25, ValueModifier.Operation.MULTIPLY_TOTAL),
                ShotComponents.DAMAGE, new ValueModifier(0.25, ValueModifier.Operation.MULTIPLY_TOTAL)
        ));
    }
}
