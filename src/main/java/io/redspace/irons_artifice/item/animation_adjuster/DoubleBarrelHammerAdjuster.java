package io.redspace.irons_artifice.item.animation_adjuster;

import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.item.MagazineContents;
import software.bernie.geckolib.cache.object.GeoBone;

import java.util.Optional;

public final class DoubleBarrelHammerAdjuster implements AnimationAdjuster {
    @Override
    public void adjust(AdjustContext context) {
        MagazineContents magazineContents = context.magazine();
        Optional<GeoBone> leftOpt = context.bone(GunBones.HAMMER_LEFT);
        Optional<GeoBone> rightOpt = context.bone(GunBones.HAMMER_RIGHT);
        if (leftOpt.isEmpty() || rightOpt.isEmpty() || magazineContents == null) {
            return;
        } else if (context.reloadProgressSeconds() <= 1.17) {
            if (magazineContents.count() <= 1) {
                AnimationAdjuster.restoreInitialRotation(leftOpt.get());
            }
            if (magazineContents.isEmpty()) {
                AnimationAdjuster.restoreInitialRotation(rightOpt.get());
            }
        }
    }
}
