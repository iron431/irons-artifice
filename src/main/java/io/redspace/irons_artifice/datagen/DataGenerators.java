package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.IronsArtifice;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = IronsArtifice.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
        generator.addProvider(event.includeServer(), new RecipeDataGenerator(output));
        generator.addProvider(event.includeServer(), new ItemTagDataGenerator(output, registries, existingFileHelper));
        generator.addProvider(event.includeServer(), new BlockTagDataGenerator(output, registries, existingFileHelper));
//        generator.addProvider(event.includeServer(), new LootTableTagGenerator(output, registries, existingFileHelper));
        generator.addProvider(event.includeServer(), new EntityTypeTagDataGenerator(output, registries, existingFileHelper));
        generator.addProvider(event.includeServer(), new LootTableDataGenerator(output));
        generator.addProvider(event.includeServer(), new AdvancementDataGenerator(output, registries));
    }
}
