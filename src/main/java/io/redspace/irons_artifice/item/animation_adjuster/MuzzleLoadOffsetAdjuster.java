package io.redspace.irons_artifice.item.animation_adjuster;

import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.util.Mth;

import java.util.Optional;

public final class MuzzleLoadOffsetAdjuster implements AnimationAdjuster {
    private static final float IN_END = 0.20f;
    private static final float OUT_START = 0.80f;

    @Override
    public void adjust(AnimationState<GunItem> animationState, GeoModel<GunItem> model) {
        Float muzzleOffsetData = animationState.getData(GunItem.MUZZLE_OFFSET_TICKET);
        Float reloadPercentData = animationState.getData(GunItem.RELOAD_PERCENT_TICKET);
        float muzzleOffset = muzzleOffsetData != null ? muzzleOffsetData : 0f;
        float reloadPercent = reloadPercentData != null ? reloadPercentData : 0f;
        if (muzzleOffset == 0f || reloadPercent == 0f) {
            return;
        }
        Optional<GeoBone> gunOpt = model.getBone(GunBones.GUN);
        Optional<GeoBone> ramrodOpt = model.getBone(GunBones.RAMROD);
        float weight = envelope(reloadPercent);
        if (weight == 0f) {
            return;
        }
        float offset = muzzleOffset * 16 * weight;
        gunOpt.ifPresent(
                bone -> {
                    bone.setPosZ(bone.getPosZ() + offset);
                    bone.resetStateChanges();
                }
        );
        ramrodOpt.ifPresent(
                // assuming all ramrods are attached to gun, we need to pull it back out the offset so it too lines up with the muzzle
                bone -> {
                    bone.setPosZ(bone.getPosZ() - offset);
                    bone.resetStateChanges();
                }
        );
    }

    private static float envelope(float t) {
        if (t < IN_END) {
            return easeInOutSine(t / IN_END);
        }
        if (t > OUT_START) {
            return 1f - easeInOutSine((t - OUT_START) / (1f - OUT_START));
        }
        return 1f;
    }

    private static float easeInOutSine(float x) {
        return 0.5f - 0.5f * Mth.cos(Mth.PI * x);
    }
}
