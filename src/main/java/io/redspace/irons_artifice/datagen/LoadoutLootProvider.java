package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.registry.ItemRegistry;
import io.redspace.irons_artifice.registry.LootTableRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

public class LoadoutLootProvider implements LootTableSubProvider {
    public LoadoutLootProvider(HolderLookup.Provider registries) {
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(LootTableRegistry.ILLIFICER_MAIN_MODIFIER, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ItemRegistry.SEEKING_POWDER.get()))
                        .add(LootItem.lootTableItem(ItemRegistry.INCENDIARY_TIP_MODIFIER.get()))
                        .add(LootItem.lootTableItem(ItemRegistry.FROZEN_JACKET.get()))
                        .add(LootItem.lootTableItem(ItemRegistry.CHAIN_LIGHTNING.get()))
                ));

        output.accept(LootTableRegistry.ILLIFICER_AUX_MODIFIER, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ItemRegistry.SCATTERSHOT.get()))
                        .add(LootItem.lootTableItem(ItemRegistry.WIND_CHAMBER.get()))
                        .add(LootItem.lootTableItem(ItemRegistry.SINGULARITY_CHARGE_MODIFIER.get()))
                        .add(LootItem.lootTableItem(ItemRegistry.LEAD_CORE.get()))
                        .add(LootItem.lootTableItem(ItemRegistry.TRICK_BULLET_MODIFIER.get()))
                        .add(LootItem.lootTableItem(ItemRegistry.CHAIN_LIGHTNING.get()))
                ));

        output.accept(LootTableRegistry.ILLIFICER_LOADOUT, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(NestedLootTable.lootTableReference(LootTableRegistry.ILLIFICER_MAIN_MODIFIER))
                )
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0F, 3.0F))
                        .add(NestedLootTable.lootTableReference(LootTableRegistry.ILLIFICER_AUX_MODIFIER))
                ));
    }
}
