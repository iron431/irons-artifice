package io.redspace.irons_artifice.data;

/**
 * Shared, mutable token object. Created on shot, and shared to each pellet spawned.
 */
public final class SoulToken {
    private int amount;

    private SoulToken(int amount) {
        this.amount = amount;
    }

    public static SoulToken available() {
        return new SoulToken(1);
    }

    public static SoulToken available(int amount) {
        return new SoulToken(amount);
    }

    public static SoulToken spent() {
        return new SoulToken(0);
    }

    public boolean tryClaim() {
        if (amount == 0) {
            return false;
        }
        amount--;
        return true;
    }
}
