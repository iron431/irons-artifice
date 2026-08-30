package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class EntityLootProvider extends EntityLootSubProvider {
    public static final ResourceKey<LootTable> DROWNED_PIRATE = ResourceKey.create(
            Registries.LOOT_TABLE, IronsArtifice.id("entities/drowned_pirate"));

    public EntityLootProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return Stream.of(EntityRegistry.ILLIFICER.get());
    }

    @Override
    public void generate() {
        this.add(EntityRegistry.ILLIFICER.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.EMERALD)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 3.0F)))
                                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(
                                        this.registries, UniformGenerator.between(0.0F, 1.0F))))
                )
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ItemRegistry.BLACKPOWDER.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(
                                        this.registries, UniformGenerator.between(0.0F, 1.0F))))
                )
        );
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        super.generate(output);
        output.accept(DROWNED_PIRATE, drownedPirateTable());
    }

    private LootTable.Builder drownedPirateTable() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(NestedLootTable.lootTableReference(EntityType.DROWNED.getDefaultLootTable().orElseThrow())))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ItemRegistry.BLACKPOWDER.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 6.0F)))
                                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(
                                        this.registries, UniformGenerator.between(0.0F, 1.0F)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.EMERALD).setWeight(6)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F)))
                                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(
                                        this.registries, UniformGenerator.between(0.0F, 1.0F))))
                        .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(3)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(
                                        this.registries, UniformGenerator.between(0.0F, 1.0F))))
                        .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(1))
                );
    }
}
