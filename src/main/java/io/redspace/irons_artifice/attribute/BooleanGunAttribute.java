package io.redspace.irons_artifice.attribute;

import io.redspace.irons_artifice.data.ComponentType;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

/**
 * An attribute linked to a {@link ComponentType} of a {@link Boolean}. Its value is sanitized to 0 or 1: +1 {@code ADDITION}
 * enables, -1 {@code MULTIPLY_TOTAL} disables, and disabling wins.
 * <br>
 * An untouched attribute (default base, no modifiers) leaves the component as the gun set it.
 */
public class BooleanGunAttribute extends Attribute implements IGunAttribute {
    private final ComponentType<Boolean> component;

    public BooleanGunAttribute(String descriptionId, ComponentType<Boolean> component) {
        super(descriptionId, 0);
        this.component = component;
        setSyncable(true);
    }

    @Override
    public double sanitizeValue(double value) {
        if (Double.isNaN(value)) {
            return 0;
        }
        return value > 0 ? 1 : 0;
    }

    @Override
    public ComponentType<Boolean> component() {
        return component;
    }

    @Override
    public void applyTo(AttributeInstance instance, ShotProfile profile) {
        if (instance.getModifiers().isEmpty() && instance.getBaseValue() == getDefaultValue()) {
            return;
        }
        profile.components().set(component, instance.getValue() > 0);
    }
}
