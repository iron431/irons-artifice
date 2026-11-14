package io.redspace.irons_artifice.item.animation_adjuster;

import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;

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
    public void adjust(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots) {
        MagazineContents magazineContents = renderPassInfo.getGeckolibData(GunItem.MAGAZINE_ANIMATION_TICKET);
        double reloadProgress = renderPassInfo.getOrDefaultGeckolibData(GunItem.RELOAD_PROGRESS_SECONDS_TICKET, 0.0);
        Optional<BoneSnapshot> boneOpt = snapshots.get(boneName);
        if (boneOpt.isEmpty() || magazineContents == null) {
            return;
        } else if (magazineContents.isEmpty() && reloadProgress <= reloadResetTime) {
            boneOpt.get().setTranslateZ(traversal);
        }
    }
}