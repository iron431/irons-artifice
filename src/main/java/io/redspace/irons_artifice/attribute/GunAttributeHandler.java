package io.redspace.irons_artifice.attribute;

import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.ironslib.util.MemoizedSupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;

@EventBusSubscriber
public final class GunAttributeHandler {

    private static final MemoizedSupplier<List<Holder<Attribute>>> GUN_ATTRIBUTES =
            new MemoizedSupplier<>(() -> BuiltInRegistries.ATTRIBUTE.listElements()
                    .filter(holder -> holder.value() instanceof IGunAttribute)
                    .<Holder<Attribute>>map(holder -> holder)
                    .toList());

    @SubscribeEvent
    public static void applyShooterAttributes(ComposeShotEvent event) {
        AttributeMap attributes = event.getEntity().getAttributes();
        for (Holder<Attribute> holder : GUN_ATTRIBUTES.get()) {
            AttributeInstance instance = attributes.getInstance(holder);
            if (instance != null) {
                ((IGunAttribute) holder.value()).applyTo(instance, event.getShotProfile());
            }
        }
    }
}
