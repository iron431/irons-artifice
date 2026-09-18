package io.redspace.irons_artifice.item.animation_adjuster;

import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;

import java.util.Optional;

public final class HarmonicaMagazineAdjuster implements AnimationAdjuster {
    @Override
    public void adjust(AnimationState<GunItem> animationState, GeoModel<GunItem> model) {
        double reloadProgress = AnimationAdjuster.reloadProgressSeconds(animationState);
        MagazineContents magazineContents = animationState.getData(GunItem.MAGAZINE_ANIMATION_TICKET);
        Optional<GeoBone> magazineOpt = model.getBone(GunBones.MAGAZINE);
        if (magazineOpt.isEmpty() || magazineContents == null) {
            return;
        }
        boolean ignoreForReload = reloadProgress > 0.42;
        if (!ignoreForReload) {
            float percent = 1 - magazineContents.count() / 10f;
            GeoBone magazine = magazineOpt.get();
            magazine.updatePosition(4 * percent, 0, 0);
            magazine.resetStateChanges();
        }
    }
}
