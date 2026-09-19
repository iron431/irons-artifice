package io.redspace.irons_artifice.item.animation_adjuster;

import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import io.redspace.irons_artifice.item.GunItem;

public interface AnimationAdjuster {
    AnimationAdjuster LOWER_HAMMER = new LowerHammerAdjuster();
    AnimationAdjuster DOUBLE_BARREL_HAMMER = new DoubleBarrelHammerAdjuster();
    AnimationAdjuster HARMONICA_MAGAZINE = new HarmonicaMagazineAdjuster();
    AnimationAdjuster MUZZLE_LOAD_OFFSET = new MuzzleLoadOffsetAdjuster();

    void adjust(AnimationState<GunItem> animationState, GeoModel<GunItem> model);

    /**
     * Puts a bone back on the rotation the geo json baked into it. A bone holds its absolute rotation, so zeroing
     * one throws the pose away instead of silencing the animation, and the flags have to be cleared too or the next
     * render pass skips this bone's reset lerp.
     */
    static void restoreRestRotation(GeoBone bone) {
        BoneSnapshot rest = bone.getInitialSnapshot();
        bone.updateRotation(rest.getRotX(), rest.getRotY(), rest.getRotZ());
        bone.resetStateChanges();
    }

    /** Seconds into the reload animation, or zero when no reload is playing. */
    static double reloadProgressSeconds(AnimationState<GunItem> animationState) {
        Double progress = animationState.getData(GunItem.RELOAD_PROGRESS_SECONDS_TICKET);
        return progress != null ? progress : 0.0;
    }
}
