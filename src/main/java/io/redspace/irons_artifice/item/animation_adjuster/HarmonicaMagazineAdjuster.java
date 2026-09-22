package io.redspace.irons_artifice.item.animation_adjuster;

import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.item.MagazineContents;
import software.bernie.geckolib.cache.object.GeoBone;

import java.util.Optional;

public final class HarmonicaMagazineAdjuster implements AnimationAdjuster {
    @Override
    public void adjust(AdjustContext context) {
        MagazineContents magazineContents = context.magazine();
        Optional<GeoBone> magazineOpt = context.bone(GunBones.MAGAZINE);
        if (magazineOpt.isEmpty() || magazineContents == null) {
            return;
        }
        boolean ignoreForReload = context.reloadProgressSeconds() > 0.42;
        if (!ignoreForReload) {
            float percent = 1 - magazineContents.count() / 10f;
            GeoBone magazine = magazineOpt.get();
            var initial = magazine.getInitialSnapshot();
            if (initial == null) {
                return;
            }
            magazine.updatePosition(initial.getOffsetX() + 4 * percent, initial.getOffsetY(), initial.getOffsetZ());
        }
    }
}
