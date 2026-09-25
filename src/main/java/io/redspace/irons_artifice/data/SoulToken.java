package io.redspace.irons_artifice.data;

/**
 * Shared, mutable token object. Created on shot, and shared to each pellet spawned. Prevents multiple hits from proccing multiple souls.
 */
public final class SoulToken {
    private boolean claimed;

    private SoulToken(boolean claimed) {
        this.claimed = claimed;
    }

    public static SoulToken available() {
        return new SoulToken(false);
    }

    public static SoulToken spent() {
        return new SoulToken(true);
    }

    public boolean tryClaim() {
        if (claimed) {
            return false;
        }
        claimed = true;
        return true;
    }
}
