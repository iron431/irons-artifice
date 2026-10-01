package io.redspace.irons_artifice.attribute;

import io.redspace.irons_artifice.data.ComponentType;
import io.redspace.irons_artifice.data.Value;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * An attribute linked to a {@link ComponentType} of a {@link Value}. The <b>modifiers</b> of this attribute are converted to {@link io.redspace.irons_artifice.data.ValueModifier}'s, and applied
 * automatically to the {@link io.redspace.irons_artifice.data.ShotComponents} of a gunshot.
 * <br>
 * Base Value modifications are treated the same as Addition.
 */
public class GunAttribute extends RangedAttribute {
    private static final double RANGE = 1_000_000;

    private final ComponentType<Value> component;

    public GunAttribute(String descriptionId, ComponentType<Value> component, Sentiment sentiment) {
        super(descriptionId, 0, -RANGE, RANGE);
        this.component = component;
        setSentiment(sentiment);
        setSyncable(true);
    }

    public ComponentType<Value> component() {
        return component;
    }
}
