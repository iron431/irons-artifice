package io.redspace.irons_artifice.item.animation_adjuster;

import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;

public interface AnimationAdjuster {
    AnimationAdjuster LOWER_HAMMER = new LowerHammerAdjuster();
    AnimationAdjuster DOUBLE_BARREL_HAMMER = new DoubleBarrelHammerAdjuster();
    AnimationAdjuster HARMONICA_MAGAZINE = new HarmonicaMagazineAdjuster();
    AnimationAdjuster MUZZLE_LOAD_OFFSET = new MuzzleLoadOffsetAdjuster();

    void adjust(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots);
}
