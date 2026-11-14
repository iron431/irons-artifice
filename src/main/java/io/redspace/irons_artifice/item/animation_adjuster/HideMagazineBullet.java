package io.redspace.irons_artifice.item.animation_adjuster;

import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

import java.util.Optional;

public final class HideMagazineBullet implements AnimationAdjuster {

    private final double reloadProgressReset;

    public HideMagazineBullet(double reloadProgressReset) {
        this.reloadProgressReset = reloadProgressReset;
    }

    @Override
    public void adjust(AnimationState<GunItem> animationState, GeoModel<GunItem> model) {
        MagazineContents magazineContents = animationState.getData(GunItem.MAGAZINE_ANIMATION_TICKET);
        double reloadProgress = AnimationAdjuster.reloadProgressSeconds(animationState);
        if (magazineContents == null) {
            return;
        }
        boolean isMagazineEmpty = reloadProgress < reloadProgressReset && magazineContents.count() <= 1; // include 1, as if the last bullet is in the chamber
        Optional<GeoBone> boneOpt = model.getBone("magazine_bullet");
        boneOpt.ifPresent(bone -> bone.setHidden(isMagazineEmpty));
    }

}
