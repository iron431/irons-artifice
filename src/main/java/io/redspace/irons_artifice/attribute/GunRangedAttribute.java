package io.redspace.irons_artifice.attribute;

import io.redspace.irons_artifice.data.ComponentType;
import io.redspace.irons_artifice.data.Value;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * An attribute linked to a {@link ComponentType} of a {@link Value}. The <b>modifiers</b> of this attribute are converted to {@link ValueModifier}s,
 * and added to the component's value stack. The attribute's own resolved value is never read.
 * <br>
 * Base value changes are treated as addition.
 */
public class GunRangedAttribute extends RangedAttribute implements IGunAttribute {
    private static final double RANGE = 1_000_000;

    private final ComponentType<Value> component;

    public GunRangedAttribute(String descriptionId, ComponentType<Value> component) {
        super(descriptionId, 0, -RANGE, RANGE);
        this.component = component;
        setSentiment(component.sentiment().toAttributeSentiment());
        setSyncable(true);
    }

    public ComponentType<Value> component() {
        return component;
    }

    @Override
    public void applyTo(AttributeInstance instance, ShotProfile profile) {
        double baseBonus = instance.getBaseValue() - getDefaultValue();
        if (baseBonus != 0) {
            profile.modifyValue(component, new ValueModifier(baseBonus, ValueModifier.Operation.ADD));
        }
        for (AttributeModifier modifier : instance.getModifiers()) {
            ValueModifier.Operation operation = switch (modifier.operation()) {
                case ADD_VALUE -> ValueModifier.Operation.ADD;
                case ADD_MULTIPLIED_BASE -> ValueModifier.Operation.MULTIPLY_BASE;
                case ADD_MULTIPLIED_TOTAL -> ValueModifier.Operation.MULTIPLY_TOTAL;
            };
            profile.modifyValue(component, new ValueModifier(modifier.amount(), operation));
        }
    }
}
