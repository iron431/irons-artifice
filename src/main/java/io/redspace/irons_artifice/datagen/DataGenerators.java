package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.IronsArtifice;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = IronsArtifice.MODID)
public final class DataGenerators {
    private DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ItemModelDataGenerator(output, existingFileHelper));
        generator.addProvider(event.includeServer(), new RecipeDataGenerator(output, registries));
        generator.addProvider(event.includeServer(), new ItemTagDataGenerator(output, registries, existingFileHelper));
        generator.addProvider(event.includeServer(), new BlockTagDataGenerator(output, registries, existingFileHelper));
//        generator.addProvider(event.includeServer(), new LootTableTagGenerator(output, registries, existingFileHelper));
        generator.addProvider(event.includeServer(), new EntityTypeTagDataGenerator(output, registries, existingFileHelper));
        generator.addProvider(event.includeServer(), new LootTableDataGenerator(output, registries));
        generator.addProvider(event.includeServer(), new AdvancementDataGenerator(output, registries));
    }
}
