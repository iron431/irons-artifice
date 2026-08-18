package io.redspace.irons_artifice.events;

import io.redspace.irons_artifice.entity.Gunslinger;
import io.redspace.irons_artifice.entity.Illificer;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public final class CommonSetup {
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.GUNSLINGER.get(), Gunslinger.createAttributes().build());
        event.put(EntityRegistry.ILLIFICER.get(), Illificer.createAttributes().build());
    }

    public static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ItemRegistry.ILLIFICER_SPAWN_EGG.get());
        }
    }
}
