package io.redspace.irons_artifice.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
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
        // NeoForge/vanilla 1.21.1 LootTableProvider has no overridable validate() hook
        // (that was a 26.x/Forge-patch API). requiredTables is already Set.of(), so
        // missing-built-in-table checks pass; nested vanilla refs resolve fine here.
    }
}
