package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.gametest.TestCatalog.ModifierTest;
import io.redspace.irons_artifice.modifier.ModifierItem;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Every registered modifier has a catalog entry in {@link ModifierTests#ENTRIES}. */
public final class ModifierCoverageTests {

    static void everyModifierHasATest(GameTestHelper helper) {
        Set<Item> covered = ModifierTests.ENTRIES.stream()
                .map(ModifierTest::modifier)
                .map(modifier -> (Item) modifier.get())
                .collect(Collectors.toSet());

        List<String> registered = new ArrayList<>();
        List<String> uncovered = new ArrayList<>();
        for (var entry : ItemRegistry.ITEMS.getEntries()) {
            Item item = entry.get();
            if (!(item instanceof ModifierItem)) {
                continue;
            }
            String id = BuiltInRegistries.ITEM.getKey(item).getPath();
            registered.add(id);
            if (!covered.contains(item)) {
                uncovered.add(id);
            }
        }

        helper.assertTrue(!registered.isEmpty(), "found at least one registered modifier");
        helper.assertTrue(uncovered.isEmpty(), "every registered modifier has a catalog entry (missing: " + uncovered + ")");
        helper.succeed();
    }
}
