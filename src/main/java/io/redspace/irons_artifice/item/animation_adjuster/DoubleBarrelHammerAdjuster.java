package io.redspace.irons_artifice.item.animation_adjuster;

import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;

import java.util.Optional;

public final class DoubleBarrelHammerAdjuster implements AnimationAdjuster {
    @Override
    public void adjust(AnimationState<GunItem> animationState, GeoModel<GunItem> model) {
        MagazineContents magazineContents = animationState.getData(GunItem.MAGAZINE_ANIMATION_TICKET);
        double reloadProgress = AnimationAdjuster.reloadProgressSeconds(animationState);
        Optional<GeoBone> leftOpt = model.getBone(GunBones.HAMMER_LEFT);
        Optional<GeoBone> rightOpt = model.getBone(GunBones.HAMMER_RIGHT);
        if (leftOpt.isEmpty() || rightOpt.isEmpty() || magazineContents == null) {
            return;
        } else if (reloadProgress <= 1.17) {
            if (magazineContents.count() <= 1) {
                AnimationAdjuster.restoreRestRotation(leftOpt.get());
            }
            if (magazineContents.isEmpty()) {
                AnimationAdjuster.restoreRestRotation(rightOpt.get());
            }
        }
    }
}
