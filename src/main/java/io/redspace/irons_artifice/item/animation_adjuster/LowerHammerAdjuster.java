package io.redspace.irons_artifice.item.animation_adjuster;

import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.item.MagazineContents;
import software.bernie.geckolib.cache.object.GeoBone;

import java.util.Optional;

public final class LowerHammerAdjuster implements AnimationAdjuster {
    @Override
    public void adjust(AdjustContext context) {
        MagazineContents magazineContents = context.magazine();
        Optional<GeoBone> boneOpt = context.bone(GunBones.HAMMER);
        if (boneOpt.isEmpty() || magazineContents == null) {
            return;
        } else if (magazineContents.isEmpty() && context.reloadProgressSeconds() <= 0) {
            AnimationAdjuster.restoreInitialRotation(boneOpt.get());
        }
    }
}
