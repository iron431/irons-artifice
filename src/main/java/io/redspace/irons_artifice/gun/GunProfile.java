package io.redspace.irons_artifice.gun;

import io.redspace.irons_artifice.data.FireCycleCueStack;
import io.redspace.irons_artifice.data.FireMode;
import io.redspace.irons_artifice.data.HandOccupancy;
import io.redspace.irons_artifice.data.PlayableSound;
import io.redspace.irons_artifice.data.ReloadCueStack;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.item.AnimationAdjuster;
import io.redspace.irons_artifice.item.TopLoadConfig;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Definition of a specific gun and its properties
 *
 * @param baseProfileSupplier supplies innate (autoattack) shot component map
 * @param magazineCapacity    rounds the magazine holds
 * @param modifierSlots       number of modifier slots on the gun
 * @param reloadTimeTicks     ticks required to reload
 */
public record GunProfile(
        Supplier<ShotComponentMap> baseProfileSupplier,
        int magazineCapacity,
        int modifierSlots,
        int reloadTimeTicks,
        FireMode fireMode,
        @Nullable TopLoadConfig topLoadConfig,
        ArmPoseKind armPoseKind,
        ReloadCueStack reloadCues,
        @Nullable PlayableSound equipSound,
        FireCycleCueStack fireCycleCues,
        AnimationAdjuster animationAdjuster,
        Map<String, HandOccupancy> animationOccupancy
) {
    public GunProfile {
        animationOccupancy = Map.copyOf(animationOccupancy);
    }

    public ShotComponentMap baseProfile() {
        return baseProfileSupplier.get();
    }

    public HandOccupancy defaultOccupancy() {
        return armPoseKind == ArmPoseKind.RIFLE ? HandOccupancy.BOTH : HandOccupancy.MAINHAND;
    }

    public HandOccupancy occupancyForAnimation(String animation) {
        return animationOccupancy.getOrDefault(animation, defaultOccupancy());
    }
}
