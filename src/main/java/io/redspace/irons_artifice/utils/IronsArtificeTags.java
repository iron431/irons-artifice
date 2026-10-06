package io.redspace.irons_artifice.utils;

import io.redspace.irons_artifice.IronsArtifice;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

public final class IronsArtificeTags {
    public static final TagKey<Block> ALWAYS_BREAK = block("always_break");
    public static final TagKey<Block> NEVER_BREAK = block("never_break");
    public static final TagKey<LootTable> CURSED_BY_PIRATES = lootTable("cursed_by_pirates");

    public static final TagKey<Item> AMMO = item("ammo");
    public static final TagKey<Item> GUNS = item("guns");

    public static final TagKey<EntityType<?>> BULLET_TARGETS = entityType("bullet_targets");

    private static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, IronsArtifice.id(path));
    }

    private static TagKey<Item> item(String path) {
        return TagKey.create(Registries.ITEM, IronsArtifice.id(path));
    }

    private static TagKey<EntityType<?>> entityType(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, IronsArtifice.id(path));
    }

    private static TagKey<LootTable> lootTable(String path) {
        return TagKey.create(Registries.LOOT_TABLE, IronsArtifice.id(path));
    }
}
