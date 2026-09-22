package io.redspace.irons_artifice.item.animation_adjuster;

import io.redspace.irons_artifice.api.GunBones;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.GeoBone;

import java.util.Optional;

public final class MuzzleLoadOffsetAdjuster implements AnimationAdjuster {
    private static final float IN_END = 0.20f;
    private static final float OUT_START = 0.80f;

    @Override
    public void adjust(AdjustContext context) {
        float muzzleOffset = context.muzzleOffset();
        float reloadPercent = context.reloadPercent();
        if (muzzleOffset == 0f || reloadPercent == 0f) {
            return;
        }
        Optional<GeoBone> gunOpt = context.bone(GunBones.GUN);
        Optional<GeoBone> ramrodOpt = context.bone(GunBones.RAMROD);
        float weight = envelope(reloadPercent);
        if (weight == 0f) {
            return;
        }
        float offset = muzzleOffset * 16 * weight;
        gunOpt.ifPresent(
                bone -> bone.setPosZ(bone.getPosZ() + offset)
        );
        ramrodOpt.ifPresent(
                // assuming all ramrods are attached to gun, we need to pull it back out the offset so it too lines up with the muzzle
                bone -> bone.setPosZ(bone.getPosZ() - offset)
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
