package io.redspace.irons_artifice.attribute;

import io.redspace.irons_artifice.data.ComponentType;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.neoforge.common.BooleanAttribute;

/**
 * An attribute linked to a {@link ComponentType} of a {@link Boolean}. Follows {@link BooleanAttribute}'s convention: +1 {@code ADD_VALUE}
 * enables, -1 {@code ADD_MULTIPLIED_TOTAL} disables, and disabling wins.
 * <br>
 * An untouched attribute (default base, no modifiers) leaves the component as the gun set it.
 */
public class BooleanGunAttribute extends BooleanAttribute implements IGunAttribute {
    private final ComponentType<Boolean> component;

    public BooleanGunAttribute(String descriptionId, ComponentType<Boolean> component) {
        super(descriptionId, false);
        this.component = component;
        setSentiment(component.sentiment().toAttributeSentiment());
        setSyncable(true);
    }

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
