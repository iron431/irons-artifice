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
        event.createProvider((output) -> new ItemModelDataGenerator(output, event.getExistingFileHelper()));
    }

    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        event.createProvider((output) -> new RecipeDataGenerator(output, event.getLookupProvider()));
        event.createProvider((output) -> new ItemTagDataGenerator(output, event.getLookupProvider()));
        event.createProvider((output) -> new BlockTagDataGenerator(output, event.getLookupProvider()));
        event.createProvider((output) -> new EntityTypeTagDataGenerator(output, event.getLookupProvider()));
        event.createProvider((output) -> new LootTableDataGenerator(output, event.getLookupProvider()));
        event.createProvider((output) -> new AdvancementDataGenerator(output, event.getLookupProvider()));
    }
}
