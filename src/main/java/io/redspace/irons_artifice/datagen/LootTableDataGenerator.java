package io.redspace.irons_artifice.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LootTableDataGenerator extends LootTableProvider {
    public LootTableDataGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(LoadoutLootProvider::new, LootContextParamSets.EMPTY),
                new SubProviderEntry(EntityLootProvider::new, LootContextParamSets.ENTITY)
        ), registries);
    }

    @Override
    protected void validate(WritableRegistry<LootTable> registry, ValidationContextSource context, ProblemReporter.Collector collector) {
        // it thinks the referenced vanilla loot tables dont exist since they arent held here. its fine.
    }
}
