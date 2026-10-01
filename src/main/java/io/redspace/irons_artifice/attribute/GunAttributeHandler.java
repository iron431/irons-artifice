package io.redspace.irons_artifice.attribute;

import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.mixin.AttributeAccessor;
import io.redspace.ironslib.util.MemoizedSupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;
import java.util.Objects;

@EventBusSubscriber
public final class GunAttributeHandler {

    private static final MemoizedSupplier<List<GunAttribute>> GUN_ATTRIBUTES =
            new MemoizedSupplier<>(() -> BuiltInRegistries.ATTRIBUTE.stream().filter(a -> a instanceof GunAttribute).map(GunAttribute.class::cast).toList());

    @SubscribeEvent
    public static void applyShooterAttributes(ComposeShotEvent event) {
        ShotProfile profile = event.getShotProfile();
        LivingEntity entity = event.getEntity();
        var attributes = entity.getAttributes();
        for (GunAttribute gunAttribute : GUN_ATTRIBUTES.get()) {
            if (attributes.hasAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(gunAttribute))) {
                var instance = attributes.getInstance(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(gunAttribute));
                applyAttributeModifiersToValue(gunAttribute, Objects.requireNonNull(instance), profile);
            }
        }
    }

    private static void applyAttributeModifiersToValue(GunAttribute attribute, AttributeInstance instance, ShotProfile profile) {
        double baseBonus = instance.getBaseValue() - attribute.getDefaultValue();
        if (baseBonus != 0) {
            profile.modifyValue(attribute.component(), toValueModifier(attribute, baseBonus, ValueModifier.Operation.ADD));
        }
        for (AttributeModifier modifier : instance.getModifiers()) {
            ValueModifier.Operation operation = switch (modifier.operation()) {
                case ADD_VALUE -> ValueModifier.Operation.ADD;
                case ADD_MULTIPLIED_BASE -> ValueModifier.Operation.MULTIPLY_BASE;
                case ADD_MULTIPLIED_TOTAL -> ValueModifier.Operation.MULTIPLY_TOTAL;
            };
            profile.modifyValue(attribute.component(), toValueModifier(attribute, modifier.amount(), operation));
        }
    }

    private static ValueModifier toValueModifier(GunAttribute attribute, double amount, ValueModifier.Operation operation) {
        boolean increase = amount > 0;
        ValueModifier.Type type = switch (((AttributeAccessor) attribute).getSentiment()) {
            case POSITIVE -> increase ? ValueModifier.Type.BENEFICIAL : ValueModifier.Type.HARMFUL;
            case NEGATIVE -> increase ? ValueModifier.Type.HARMFUL : ValueModifier.Type.BENEFICIAL;
            case NEUTRAL -> ValueModifier.Type.NEUTRAL;
        };
        return new ValueModifier(amount, operation, type);
    }
}
