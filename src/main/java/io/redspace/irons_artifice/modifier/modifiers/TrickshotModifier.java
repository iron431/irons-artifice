package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.PlayableSound;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.ValueStackModifier;
import io.redspace.irons_artifice.registry.IronsArtificeSounds;

import java.util.Map;

public final class TrickshotModifier extends ValueStackModifier {
    public TrickshotModifier() {
        super(Map.of(
                ShotComponents.RICOCHET, new ValueModifier(3, ValueModifier.Operation.ADD)
        ));
    }

    @Override
    public void apply(ShotComponentMap components) {
        super.apply(components);
        components.getOrCreate(ShotComponents.IMPACT_SOUND).addBlockAccent(PlayableSound.of(IronsArtificeSounds.BULLET_IMPACT_RICOCHET, 2f, .7f, 1.3f));
    }
}
