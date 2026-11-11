package io.redspace.irons_artifice.data;

public record ValueModifier(double amount, Operation operation) {
    /**
     * @deprecated the type is ignored; sentiment belongs to the modified {@link ComponentType}
     */
    @Deprecated
    public ValueModifier(double amount, Operation operation, Type type) {
        this(amount, operation);
    }

    /**
     * @deprecated always {@link Type#NEUTRAL}; use {@link ComponentType#sentiment()}
     */
    @Deprecated
    public Type type() {
        return Type.NEUTRAL;
    }

    /**
     * Applied in declaration order; matches vanilla attribute operations
     */
    public enum Operation {
        ADD,
        MULTIPLY_BASE,
        MULTIPLY_TOTAL,
        ;
    }

    /**
     * @deprecated use {@link ComponentType.Sentiment}
     */
    @Deprecated
    public enum Type {
        BENEFICIAL,
        HARMFUL,
        NEUTRAL,
        ;
    }
}
