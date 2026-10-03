package io.redspace.irons_artifice.data;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.function.Supplier;

public final class ComponentType<T> {
    final Identifier name;

    final Supplier<T> defaultValue;

    final Sentiment sentiment;

    public ComponentType(Identifier name, Supplier<T> defaultValue) {
        this(name, defaultValue, Sentiment.NEUTRAL);
    }

    public ComponentType(Identifier name, Supplier<T> defaultValue, Sentiment sentiment) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.sentiment = sentiment;
    }

    public T provideDefaultValue() {
        return defaultValue.get();
    }

    public Identifier getName() {
        return name;
    }

    public Sentiment sentiment() {
        return sentiment;
    }

    @Override
    public String toString() {
        return String.format("ComponentType[%s]", name);
    }

    /**
     * Whether a higher value is better for the shooter
     */
    public enum Sentiment {
        POSITIVE,
        NEUTRAL,
        NEGATIVE,
        ;

        public Attribute.Sentiment toAttributeSentiment() {
            return switch (this) {
                case POSITIVE -> Attribute.Sentiment.POSITIVE;
                case NEUTRAL -> Attribute.Sentiment.NEUTRAL;
                case NEGATIVE -> Attribute.Sentiment.NEGATIVE;
            };
        }
    }
}
