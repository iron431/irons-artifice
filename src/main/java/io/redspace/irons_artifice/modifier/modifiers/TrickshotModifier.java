package io.redspace.irons_artifice.modifier.modifiers;

import io.redspace.irons_artifice.data.PlayableSound;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.registry.SoundRegistry;

public final class TrickshotModifier implements GunModifier {
    @Override
    public void apply(ShotComponentMap components) {
        components.getOrCreate(ShotComponents.IMPACT_SOUND).addBlockAccent(PlayableSound.of(SoundRegistry.BULLET_IMPACT_RICOCHET, 2f, .7f, 1.3f));
    }
}
