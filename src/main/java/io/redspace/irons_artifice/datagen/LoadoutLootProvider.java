package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.registry.IronsArtificeRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
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
    public static final ResourceKey<LootTable> ILLIFICER_LOADOUT = key("loadouts/illificer/loadout");
    public static final ResourceKey<LootTable> ILLIFICER_MAIN_MODIFIER = key("loadouts/illificer/main_modifier");
    public static final ResourceKey<LootTable> ILLIFICER_AUX_MODIFIER = key("loadouts/illificer/aux_modifier");
    public static final ResourceKey<LootTable> DROWNED_PIRATE_GUN = key("loadouts/drowned_pirate/gun");
    public static final ResourceKey<LootTable> DROWNED_PIRATE_LOADOUT = key("loadouts/drowned_pirate/loadout");

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, IronsArtifice.id(path));
    }

    public LoadoutLootProvider(HolderLookup.Provider registries) {
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(ILLIFICER_MAIN_MODIFIER, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(IronsArtificeRegistries.Items.SEEKING_POWDER.get()))
                        .add(LootItem.lootTableItem(IronsArtificeRegistries.Items.INCENDIARY_TIP_MODIFIER.get()))
                        .add(LootItem.lootTableItem(IronsArtificeRegistries.Items.FROZEN_JACKET.get()))
                        .add(LootItem.lootTableItem(IronsArtificeRegistries.Items.CHAIN_LIGHTNING.get()))
                ));

        output.accept(ILLIFICER_AUX_MODIFIER, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(IronsArtificeRegistries.Items.SCATTERSHOT.get()))
                        .add(LootItem.lootTableItem(IronsArtificeRegistries.Items.WIND_CHAMBER.get()))
                        .add(LootItem.lootTableItem(IronsArtificeRegistries.Items.SINGULARITY_CHARGE_MODIFIER.get()))
                        .add(LootItem.lootTableItem(IronsArtificeRegistries.Items.LEAD_CORE.get()))
                        .add(LootItem.lootTableItem(IronsArtificeRegistries.Items.TRICK_BULLET_MODIFIER.get()))
                        .add(LootItem.lootTableItem(IronsArtificeRegistries.Items.CHAIN_LIGHTNING.get()))
                ));

        output.accept(ILLIFICER_LOADOUT, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(NestedLootTable.lootTableReference(ILLIFICER_MAIN_MODIFIER))
                )
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0F, 3.0F))
                        .add(NestedLootTable.lootTableReference(ILLIFICER_AUX_MODIFIER))
                ));

        output.accept(DROWNED_PIRATE_LOADOUT, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(IronsArtificeRegistries.Items.SPIRAL_TIP_MODIFIER.get()))
                ).withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(IronsArtificeRegistries.Items.BAYONET_ATTACHMENT_MODIFIER.get()))
                ));
        output.accept(DROWNED_PIRATE_GUN, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(IronsArtificeRegistries.Items.FLINTLOCK_PISTOL.get()).setWeight(3))
                        .add(LootItem.lootTableItem(IronsArtificeRegistries.Items.BLUNDERBUSS.get()))
                ));
    }
}
