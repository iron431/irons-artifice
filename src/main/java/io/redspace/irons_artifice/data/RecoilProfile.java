package io.redspace.irons_artifice.data;

/**
 * @param magnitude Magnitude in degrees of the recoil impulse per shot
 * @param horizontalRatio Ratio of the magnitude also applied in the horizontal (yaw) direction
 * @param horizontalPatternFrequency The frequency in radian which the horizontal recoil shifts per shot. !! Be careful of using whole angles !!
 * @param seed The seed for the recoil pattern
 */
public record RecoilProfile(float magnitude, float horizontalRatio, float horizontalPatternFrequency, int seed) {
    public static RecoilProfile simple(float magnitude, int seed) {
        return new RecoilProfile(magnitude, .33f, 1.7f, seed);
    }

    public static RecoilProfile of(float magnitude, float horizontalRatio, float horizontalPatternFrequency, int seed) {
        return new RecoilProfile(magnitude, horizontalRatio, horizontalPatternFrequency, seed);
    }
}
