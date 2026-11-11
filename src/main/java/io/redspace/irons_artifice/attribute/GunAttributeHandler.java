package io.redspace.irons_artifice.attribute;

import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.ironslib.util.MemoizedSupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

import java.util.List;

public final class GunAttributeHandler {

    private static final MemoizedSupplier<List<Attribute>> GUN_ATTRIBUTES =
            new MemoizedSupplier<>(() -> BuiltInRegistries.ATTRIBUTE.stream()
                    .filter(attribute -> attribute instanceof IGunAttribute)
                    .toList());

    public static void applyShooterAttributes(LivingEntity shooter, ShotProfile profile) {
        AttributeMap attributes = shooter.getAttributes();
        for (Attribute attribute : GUN_ATTRIBUTES.get()) {
            AttributeInstance instance = attributes.getInstance(attribute);
            if (instance != null) {
                ((IGunAttribute) attribute).applyTo(instance, profile);
            }
        }
    }
}
