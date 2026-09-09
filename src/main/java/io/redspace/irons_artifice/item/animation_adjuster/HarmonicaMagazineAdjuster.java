package io.redspace.irons_artifice.item.animation_adjuster;

import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;

import java.util.Optional;

public final class HarmonicaMagazineAdjuster implements AnimationAdjuster {
    @Override
    public void adjust(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots) {
        double reloadProgress = renderPassInfo.getOrDefaultGeckolibData(GunItem.RELOAD_PROGRESS_SECONDS_TICKET, 0.0);
        MagazineContents magazineContents = renderPassInfo.getGeckolibData(GunItem.MAGAZINE_ANIMATION_TICKET);
        Optional<BoneSnapshot> magazineOpt = snapshots.get("magazine");
        if (magazineOpt.isEmpty() || magazineContents == null) {
            return;
        }
        boolean ignoreForReload = reloadProgress > 0.42;
        if (!ignoreForReload) {
            float percent = 1 - magazineContents.count() / 10f;
            BoneSnapshot magazine = magazineOpt.get();
            magazine.setTranslation(4 * percent, 0, 0);
        }
    }
}
