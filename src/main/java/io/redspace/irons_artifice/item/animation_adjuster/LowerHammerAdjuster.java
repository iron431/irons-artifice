package io.redspace.irons_artifice.item.animation_adjuster;

import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;

import java.util.Optional;

public final class LowerHammerAdjuster implements AnimationAdjuster {
    @Override
    public void adjust(AnimationState<GunItem> animationState, GeoModel<GunItem> model) {
        MagazineContents magazineContents = animationState.getData(GunItem.MAGAZINE_ANIMATION_TICKET);
        double reloadProgress = AnimationAdjuster.reloadProgressSeconds(animationState);
        Optional<GeoBone> boneOpt = model.getBone(GunBones.HAMMER);
        if (boneOpt.isEmpty() || magazineContents == null) {
            return;
        } else if (magazineContents.isEmpty() && reloadProgress <= 0) {
            AnimationAdjuster.restoreRestRotation(boneOpt.get());
        }
    }
}
