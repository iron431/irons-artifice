package io.redspace.irons_artifice.item.animation_adjuster;

import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.util.Mth;

import java.util.Optional;

public final class MuzzleLoadOffsetAdjuster implements AnimationAdjuster {
    private static final float IN_END = 0.20f;
    private static final float OUT_START = 0.80f;

    @Override
    public void adjust(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots) {
        float muzzleOffset = renderPassInfo.getOrDefaultGeckolibData(GunItem.MUZZLE_OFFSET_TICKET, 0f);
        float reloadPercent = renderPassInfo.getOrDefaultGeckolibData(GunItem.RELOAD_PERCENT_TICKET, 0f);
        if (muzzleOffset == 0f || reloadPercent == 0f) {
            return;
        }
        Optional<BoneSnapshot> gunOpt = snapshots.get("gun");
        Optional<BoneSnapshot> ramrodOpt = snapshots.get("ramrod");
        float weight = envelope(reloadPercent);
        if (weight == 0f) {
            return;
        }
        float offset = muzzleOffset * 16 * weight;
        gunOpt.ifPresent(
                bone->bone.setTranslateZ(bone.getTranslateZ() + offset)
        );
        ramrodOpt.ifPresent(
                // assuming all ramrods are attached to gun, we need to pull it back out the offset so it too lines up with the muzzle
                bone->bone.setTranslateZ(bone.getTranslateZ() - offset)
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
