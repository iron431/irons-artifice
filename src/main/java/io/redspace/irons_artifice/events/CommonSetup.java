package io.redspace.irons_artifice.events;

import io.redspace.irons_artifice.entity.Gunslinger;
import io.redspace.irons_artifice.entity.Illificer;
import io.redspace.irons_artifice.registry.AttributeRegistry;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

import java.util.List;

public final class CommonSetup {
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.GUNSLINGER.get(), Gunslinger.createAttributes().build());
        event.put(EntityRegistry.ILLIFICER.get(), Illificer.createAttributes().build());
    }

    /**
     * Any living entity can fire a gun, so all of them carry the gun stats
     */
    public static void modifyEntityAttributes(EntityAttributeModificationEvent event) {
        List<Holder<Attribute>> gunStats = AttributeRegistry.gunStats();
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            for (Holder<Attribute> gunStat : gunStats) {
                if (!event.has(type, gunStat)) {
                    event.add(type, gunStat);
                }
            }
        }
    }

    public static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ItemRegistry.ILLIFICER_SPAWN_EGG.get());
        }
    }
}
