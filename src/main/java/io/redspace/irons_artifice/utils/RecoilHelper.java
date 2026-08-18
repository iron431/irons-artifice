package io.redspace.irons_artifice.utils;

import io.redspace.irons_artifice.data.RecoilProfile;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public final class RecoilHelper {
    /**
     * Fraction of the recoil offsetSeconds retained each tick during recovery (lower = snappier).
     */
    public static final float RETENTION = 0.75F;

    public static final float PERMANENT_FRACTION = 0.15F;

    /**
     * Deterministic step based on the round index being fired out of the magazine <br>
     * Used for client-server agreement, and consistent recoil patterns
     */
    public static int getBulletIndex(ShotProfile shotProfile) {
        return shotProfile.gun().magazineCapacity() - shotProfile.magazineContents().count();
    }

    /**
     * @return pitch, yaw in degrees
     */
    public static Vec2 calculateFullRecoil(ShotProfile shotProfile) {
        int index = getBulletIndex(shotProfile);
        float strengthMultiplier = (float) shotProfile.value(ShotComponents.CAMERA_RECOIL_MULTIPLIER);
        RecoilProfile recoil = shotProfile.get(ShotComponents.CAMERA_RECOIL);
        float pitch = recoil.magnitude();
        float yaw = recoil.magnitude() * recoil.horizontalRatio() *
                Mth.sin((index + recoil.seed()) * recoil.horizontalPatternFrequency());
        return new Vec2(pitch, yaw).scale(strengthMultiplier);
    }

    /**
     * Recoil applied permanently to character's rotation
     *
     * @return pitch, yaw in degrees
     */
    public static Vec2 calculatePermanentRecoil(ShotProfile shotProfile) {
        Vec2 fullRecoil = calculateFullRecoil(shotProfile);
        return fullRecoil.scale(PERMANENT_FRACTION);
    }

    /**
     * Recoil applied temporarily to character's camera and aim
     *
     * @return pitch, yaw in degrees
     */
    public static Vec2 calculateCameraRecoil(ShotProfile shotProfile) {
        Vec2 fullRecoil = calculateFullRecoil(shotProfile);
        return fullRecoil.scale(1 - PERMANENT_FRACTION);
    }

    /**
     * Decays a recoil component over {@code ticks} elapsed ticks.
     */
    public static float decay(float value, int ticks) {
        if (ticks <= 0) {
            return value;
        }
        return value * (float) Math.pow(RETENTION, ticks);
    }
}
