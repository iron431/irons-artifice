package io.redspace.irons_artifice.item.animation_adjuster;

import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

import java.util.Optional;

public final class BoltHoldOpenAdjuster implements AnimationAdjuster {
    private final String boneName;
    private final float traversal;
    private final double reloadResetTime;

    public BoltHoldOpenAdjuster(String boneName, float traversal, double reloadResetTime) {
        this.boneName = boneName;
        this.traversal = traversal;
        this.reloadResetTime = reloadResetTime;
    }

    @Override
    public void adjust(AnimationState<GunItem> animationState, GeoModel<GunItem> model) {
        MagazineContents magazineContents = animationState.getData(GunItem.MAGAZINE_ANIMATION_TICKET);
        double reloadProgress = AnimationAdjuster.reloadProgressSeconds(animationState);
        Optional<GeoBone> boneOpt = model.getBone(boneName);
        if (boneOpt.isEmpty() || magazineContents == null) {
            return;
        } else if (magazineContents.isEmpty() && reloadProgress <= reloadResetTime) {
            GeoBone bone = boneOpt.get();
            bone.setPosZ(traversal);
            bone.resetStateChanges();
        }
    }
}
