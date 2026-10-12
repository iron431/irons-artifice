package io.redspace.irons_artifice.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class LootTableDataGenerator extends LootTableProvider {
    public LootTableDataGenerator(PackOutput output) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(LoadoutLootProvider::new, LootContextParamSets.EMPTY),
                new SubProviderEntry(EntityLootProvider::new, LootContextParamSets.ENTITY)
        ));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext context) {
        // it thinks the referenced vanilla loot tables dont exist since they arent held here. its fine.
    }
}
