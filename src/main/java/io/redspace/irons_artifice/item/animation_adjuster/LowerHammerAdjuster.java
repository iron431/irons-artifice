package io.redspace.irons_artifice.item.animation_adjuster;

import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;

import java.util.Optional;

public final class LowerHammerAdjuster implements AnimationAdjuster {
    @Override
    public void adjust(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots) {
        MagazineContents magazineContents = renderPassInfo.getGeckolibData(GunItem.MAGAZINE_ANIMATION_TICKET);
        double reloadProgress = renderPassInfo.getOrDefaultGeckolibData(GunItem.RELOAD_PROGRESS_SECONDS_TICKET, 0.0);
        Optional<BoneSnapshot> boneOpt = snapshots.get("hammer");
        if (boneOpt.isEmpty() || magazineContents == null) {
            return;
        } else if (magazineContents.isEmpty() && reloadProgress <= 0) {
            boneOpt.get().setRotation(0, 0, 0);
        }
    }
}
