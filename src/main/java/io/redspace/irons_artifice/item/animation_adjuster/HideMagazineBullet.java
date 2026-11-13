package io.redspace.irons_artifice.item.animation_adjuster;

import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;

import java.util.Optional;

public final class HideMagazineBullet implements AnimationAdjuster {

    private final double reloadProgressReset;

    public HideMagazineBullet(double reloadProgressReset) {
        this.reloadProgressReset = reloadProgressReset;
    }

    @Override
    public void adjust(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots) {
        MagazineContents magazineContents = renderPassInfo.getGeckolibData(GunItem.MAGAZINE_ANIMATION_TICKET);
        double reloadProgress = renderPassInfo.getOrDefaultGeckolibData(GunItem.RELOAD_PROGRESS_SECONDS_TICKET, 0.0);
        if (magazineContents == null) {
            return;
        }
        boolean isMagazineEmpty = reloadProgress < reloadProgressReset && magazineContents.count() <= 1; // include 1, as if the last bullet is in the chamber
        Optional<BoneSnapshot> boneOpt = snapshots.get("magazine_bullet");
        boneOpt.ifPresent(bone -> bone.skipRender(isMagazineEmpty));
    }

}
