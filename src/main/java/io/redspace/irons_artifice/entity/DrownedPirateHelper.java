package io.redspace.irons_artifice.entity;

import io.redspace.irons_artifice.datagen.EntityLootProvider;
import io.redspace.irons_artifice.datagen.LoadoutLootProvider;
import io.redspace.irons_artifice.menu.GunContainer;
import io.redspace.irons_artifice.mixin.MobAccessor;
import io.redspace.irons_artifice.modifier.ModifierItem;
import io.redspace.irons_artifice.registry.ItemRegistry;
import io.redspace.irons_artifice.registry.SoundRegistry;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilus;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.Optional;

public class DrownedPirateHelper {

    public static void trySpawnPirates(ServerLevel level, LivingEntity target, Vec3 center) {
        float distance = 18;
        for (int i = 0; i < 18; i++) {
            Vec3 pos = center.add(new Vec3(0, 0, distance).yRot(i * 60 * Mth.DEG_TO_RAD)).add(Utils.randomVec3(3));
            BlockPos heightSamplePos = BlockPos.containing(pos);
            int waterHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE, heightSamplePos);
            if (pos.y > waterHeight - 3) {
                int y = (int) Math.min((pos.y + pos.y + level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, heightSamplePos)) / 3, waterHeight - 5);
                pos = new Vec3(pos.x, y, pos.z);
            }
            AABB box = AABB.ofSize(pos, 5, 3, 5);
            if (level.isFluidAtPosition(BlockPos.containing(pos), f -> f.is(Tags.Fluids.WATER)) && level.noCollision(box)) {
                for (int j = 0; j < 3; j++) {
                    Drowned pirate = DrownedPirateHelper.createDrownedPirate(level);
                    pirate.setPos(pos.add(Utils.randomVec3(3)));
                    pirate.setTarget(target);
                    if (level.getRandom().nextFloat() < 0.50) {
                        ZombieNautilus zombieNautilus = new ZombieNautilus(EntityType.ZOMBIE_NAUTILUS, level);
                        zombieNautilus.setPos(pirate.position());
                        pirate.startRiding(zombieNautilus);
                        level.addFreshEntityWithPassengers(zombieNautilus);
                    } else {
                        level.addFreshEntity(pirate);
                    }
                }
                level.playSound(null, BlockPos.containing(pos), SoundRegistry.PIRATE_AMBUSH.get(), SoundSource.NEUTRAL, 2.5f, 1);
                break;
            }
        }
    }

    public static Drowned createDrownedPirate(ServerLevel level) {
        Drowned drowned = new Drowned(EntityType.DROWNED, level);
        // drop chances intentionally left unchanged
        drowned.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemRegistry.TRICORNE_HAT.get()));
        drowned.setItemSlot(EquipmentSlot.MAINHAND, createLoadout(level));
        ((MobAccessor) drowned).setLootTable(Optional.of(EntityLootProvider.DROWNED_PIRATE));
        return drowned;
    }

    public static ItemStack createLoadout(ServerLevel level) {
        List<ItemStack> guns = rollLootTable(level, LoadoutLootProvider.DROWNED_PIRATE_GUN);
        if (guns.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack gun = guns.get(0);
        List<ItemStack> modifiers = rollLootTable(level, LoadoutLootProvider.DROWNED_PIRATE_LOADOUT);
        GunContainer container = new GunContainer(gun);
        int slot = 0;
        for (ItemStack stack : modifiers) {
            if (slot >= container.getContainerSize()) {
                break;
            }
            if (!(stack.getItem() instanceof ModifierItem)) {
                continue;
            }
            container.setItem(slot++, stack.copyWithCount(1));
        }
        container.setChanged();
        return gun;
    }

    public static List<ItemStack> rollLootTable(ServerLevel level, ResourceKey<LootTable> lootTableResourceKey) {
        LootTable table = level.getServer().reloadableRegistries().getLootTable(lootTableResourceKey);
        LootParams params = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
        return table.getRandomItems(params);
    }
}
