package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

public class LoadoutLootProvider implements LootTableSubProvider {
    public static final ResourceLocation ILLIFICER_LOADOUT = IronsArtifice.id("loadouts/illificer/loadout");
    public static final ResourceLocation ILLIFICER_MAIN_MODIFIER = IronsArtifice.id("loadouts/illificer/main_modifier");
    public static final ResourceLocation ILLIFICER_AUX_MODIFIER = IronsArtifice.id("loadouts/illificer/aux_modifier");
    public static final ResourceLocation DROWNED_PIRATE_GUN = IronsArtifice.id("loadouts/drowned_pirate/gun");
    public static final ResourceLocation DROWNED_PIRATE_LOADOUT = IronsArtifice.id("loadouts/drowned_pirate/loadout");

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> output) {
        output.accept(ILLIFICER_MAIN_MODIFIER, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ItemRegistry.SEEKING_POWDER.get()))
                        .add(LootItem.lootTableItem(ItemRegistry.INCENDIARY_TIP_MODIFIER.get()))
                        .add(LootItem.lootTableItem(ItemRegistry.FROZEN_JACKET.get()))
                        .add(LootItem.lootTableItem(ItemRegistry.CHAIN_LIGHTNING.get()))
                ));

        output.accept(ILLIFICER_AUX_MODIFIER, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ItemRegistry.SCATTERSHOT.get()))
                        .add(LootItem.lootTableItem(ItemRegistry.WIND_CHAMBER.get()))
                        .add(LootItem.lootTableItem(ItemRegistry.SINGULARITY_CHARGE_MODIFIER.get()))
                        .add(LootItem.lootTableItem(ItemRegistry.LEAD_CORE.get()))
                        .add(LootItem.lootTableItem(ItemRegistry.TRICK_BULLET_MODIFIER.get()))
                        .add(LootItem.lootTableItem(ItemRegistry.CHAIN_LIGHTNING.get()))
                ));

        output.accept(ILLIFICER_LOADOUT, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootTableReference.lootTableReference(ILLIFICER_MAIN_MODIFIER))
                )
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0F, 3.0F))
                        .add(LootTableReference.lootTableReference(ILLIFICER_AUX_MODIFIER))
                ));

        output.accept(DROWNED_PIRATE_LOADOUT, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ItemRegistry.SPIRAL_TIP_MODIFIER.get()))
                ).withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ItemRegistry.BAYONET_ATTACHMENT_MODIFIER.get()))
                ));
        output.accept(DROWNED_PIRATE_GUN, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ItemRegistry.FLINTLOCK_PISTOL.get()).setWeight(3))
                        .add(LootItem.lootTableItem(ItemRegistry.BLUNDERBUSS.get()))
                ));
    }
}
