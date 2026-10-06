package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.modifier.ModifierItem;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class LoadSmokeTests {

    static void everyGunComposesWithEveryModifier(GameTestHelper helper) {
        List<GunItem> guns = new ArrayList<>();
        List<Item> modifiers = new ArrayList<>();
        for (var entry : ItemRegistry.ITEMS.getEntries()) {
            Item item = entry.get();
            if (item instanceof GunItem gun) {
                guns.add(gun);
            } else if (item instanceof ModifierItem) {
                modifiers.add(item);
            }
        }
        helper.assertTrue(!guns.isEmpty(), "found at least one registered gun");
        helper.assertTrue(!modifiers.isEmpty(), "found at least one registered modifier");

        for (GunItem gun : guns) {
            helper.assertTrue(gun.magazineCapacity() > 0, "magazine capacity above zero for " + gun);
            assertComposes(helper, gun, new ItemStack(gun), "bare " + gun);
            for (Item modifier : modifiers) {
                assertComposes(helper, gun, TestFixtures.gunWith(gun, 1, modifier), modifier + " on " + gun);
            }
        }
        helper.succeed();
    }

    private static void assertComposes(GameTestHelper helper, GunItem gun, ItemStack stack, String what) {
        ShotProfile profile = GunplayManager.compose(null, gun.getGun(), stack);
        helper.assertTrue(profile.fireDelayTicks() > 0.0, "fire delay above zero for " + what);
    }
}
