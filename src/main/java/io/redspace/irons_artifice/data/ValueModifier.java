package io.redspace.irons_artifice.data;

public record ValueModifier(double amount, Operation operation, Type type) {
    /**
     * Applied in declaration order; matches vanilla attribute operations
     */
    public enum Operation {
        ADD,
        MULTIPLY_BASE,
        MULTIPLY_TOTAL,
        ;
    }

    public enum Type {
        BENEFICIAL,
        HARMFUL,
        NEUTRAL,
        ;
    }
}
