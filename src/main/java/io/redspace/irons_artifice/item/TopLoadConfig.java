package io.redspace.irons_artifice.item;

public record TopLoadConfig(double loopStart, double loopEnd, double loopDuration) {
    public double resumeFrom(int roundsToLoad) {
        return loopEnd - loopDuration * (roundsToLoad - 1);
    }
}
