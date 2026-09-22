package io.redspace.irons_artifice.item.animation_adjuster;

import io.redspace.irons_artifice.item.MagazineContents;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

import java.util.Optional;

public interface AnimationAdjuster {
    AnimationAdjuster LOWER_HAMMER = new LowerHammerAdjuster();
    AnimationAdjuster DOUBLE_BARREL_HAMMER = new DoubleBarrelHammerAdjuster();
    AnimationAdjuster HARMONICA_MAGAZINE = new HarmonicaMagazineAdjuster();
    AnimationAdjuster MUZZLE_LOAD_OFFSET = new MuzzleLoadOffsetAdjuster();

    void adjust(AdjustContext context);

    static void restoreInitial(GeoBone bone) {
        var initial = bone.getInitialSnapshot();
        if (initial == null) {
            return;
        }
        bone.updateRotation(initial.getRotX(), initial.getRotY(), initial.getRotZ());
        bone.updatePosition(initial.getOffsetX(), initial.getOffsetY(), initial.getOffsetZ());
        bone.updateScale(initial.getScaleX(), initial.getScaleY(), initial.getScaleZ());
    }

    static void restoreInitialRotation(GeoBone bone) {
        var initial = bone.getInitialSnapshot();
        if (initial == null) {
            return;
        }
        bone.updateRotation(initial.getRotX(), initial.getRotY(), initial.getRotZ());
    }

    static void restoreInitialTransform(GeoBone bone) {
        var initial = bone.getInitialSnapshot();
        if (initial == null) {
            return;
        }
        bone.updateRotation(initial.getRotX(), initial.getRotY(), initial.getRotZ());
        bone.updatePosition(initial.getOffsetX(), initial.getOffsetY(), initial.getOffsetZ());
    }

    record AdjustContext(@Nullable MagazineContents magazine, double reloadProgressSeconds, float reloadPercent,
                         float muzzleOffset, GeoModel<?> model) {
        public Optional<GeoBone> bone(String name) {
            return model.getBone(name);
        }
    }
}
