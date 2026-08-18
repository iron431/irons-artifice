package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.IronsArtifice;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = IronsArtifice.MODID)
public final class DataGenerators {
    private DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        event.createProvider(ItemModelDataGenerator::new);
        event.createProvider(RecipeDataGenerator.Runner::new);
        event.createProvider(ItemTagDataGenerator::new);
        event.createProvider(EntityTypeTagDataGenerator::new);
        event.createProvider(LootTableDataGenerator::new);
    }

    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        event.createProvider(EntityTypeTagDataGenerator::new);
        event.createProvider(LootTableDataGenerator::new);
    }
}
