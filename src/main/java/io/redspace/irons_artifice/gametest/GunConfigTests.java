package io.redspace.irons_artifice.gametest;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import io.redspace.irons_artifice.config.GunConfig;
import io.redspace.irons_artifice.config.ServerConfig;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.Value;
import io.redspace.irons_artifice.gun.Guns;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class GunConfigTests {

    private static final double TOLERANCE = 1.0E-6;

    static void replacesBaseValue(GameTestHelper helper) {
        withGunConfig(List.of(entry(ItemRegistry.MUSKET.get(), "irons_artifice:damage", 24)), () -> {
            double damage = composedDamage(ItemRegistry.MUSKET.get(), new ItemStack(ItemRegistry.MUSKET.get()));
            assertClose(helper, 24, damage, "configured musket damage");
        });
        helper.succeed();
    }

    static void appliesBeforeModifiers(GameTestHelper helper) {
        withGunConfig(List.of(entry(ItemRegistry.MUSKET.get(), "irons_artifice:damage", 24)), () -> {
            ItemStack powdered = TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 1, ItemRegistry.OVERCHARGED_POWDER.get());
            double damage = composedDamage(ItemRegistry.MUSKET.get(), powdered);
            assertClose(helper, 24 * 1.15, damage, "overcharged powder stacks on configured damage");
        });
        helper.succeed();
    }

    static void disabledSkipsOverrides(GameTestHelper helper) {
        double expected = Guns.MUSKET.baseProfile().getOrDefault(ShotComponents.DAMAGE).compute();
        boolean wasEnabled = ServerConfig.GUN_OVERRIDES_ENABLED.get();
        ServerConfig.GUN_OVERRIDES_ENABLED.set(false);
        try {
            withGunConfig(List.of(entry(ItemRegistry.MUSKET.get(), "irons_artifice:damage", 24)), () -> {
                double damage = composedDamage(ItemRegistry.MUSKET.get(), new ItemStack(ItemRegistry.MUSKET.get()));
                assertClose(helper, expected, damage, "musket damage while overrides are disabled");
            });
        } finally {
            ServerConfig.GUN_OVERRIDES_ENABLED.set(wasEnabled);
        }
        helper.succeed();
    }

    static void onlyTouchesNamedItem(GameTestHelper helper) {
        double expected = Guns.FLINTLOCK_PISTOL.baseProfile().getOrDefault(ShotComponents.DAMAGE).compute();
        withGunConfig(List.of(entry(ItemRegistry.MUSKET.get(), "irons_artifice:damage", 24)), () -> {
            double damage = composedDamage(ItemRegistry.FLINTLOCK_PISTOL.get(), new ItemStack(ItemRegistry.FLINTLOCK_PISTOL.get()));
            assertClose(helper, expected, damage, "flintlock damage untouched by a musket entry");
        });
        helper.succeed();
    }

    static void setsBooleanComponent(GameTestHelper helper) {
        ShotComponentMap components = new ShotComponentMap();
        components.set(ShotComponents.FORCE_AUTO_FIRE, false);
        withGunConfig(List.of(entry(ItemRegistry.MUSKET.get(), "irons_artifice:force_auto_fire", true)), () ->
                GunConfig.handleConfigModifications(ItemRegistry.MUSKET.get(), components));
        helper.assertTrue(components.getOrDefault(ShotComponents.FORCE_AUTO_FIRE), "configured boolean component is set");
        helper.succeed();
    }

    static void skipsComponentAbsentFromMap(GameTestHelper helper) {
        ShotComponentMap components = new ShotComponentMap();
        withGunConfig(List.of(entry(ItemRegistry.MUSKET.get(), "irons_artifice:piercing", 3)), () ->
                GunConfig.handleConfigModifications(ItemRegistry.MUSKET.get(), components));
        helper.assertFalse(components.has(ShotComponents.PIERCING), "component absent from the map is not added");
        helper.succeed();
    }

    static void skipsMismatchedValueType(GameTestHelper helper) {
        ShotComponentMap components = new ShotComponentMap();
        components.set(ShotComponents.DAMAGE, Value.of(18));
        withGunConfig(List.of(entry(ItemRegistry.MUSKET.get(), "irons_artifice:damage", true)), () ->
                GunConfig.handleConfigModifications(ItemRegistry.MUSKET.get(), components));
        assertClose(helper, 18, components.getOrDefault(ShotComponents.DAMAGE).compute(), "boolean ignored for a numeric component");
        helper.succeed();
    }

    private static double composedDamage(Item gun, ItemStack stack) {
        return GunplayManager.compose(null, ((GunItem) gun).getGun(), stack).value(ShotComponents.DAMAGE);
    }

    private static void assertClose(GameTestHelper helper, double expected, double actual, String what) {
        helper.assertTrue(Math.abs(expected - actual) < TOLERANCE, what + ": expected " + expected + ", got " + actual);
    }

    private static UnmodifiableConfig entry(Item item, String component, Object value) {
        Config config = Config.inMemory();
        config.valueMap().put("item", BuiltInRegistries.ITEM.getKey(item).toString());
        config.valueMap().put(component, value);
        return config;
    }

    private static void withGunConfig(List<UnmodifiableConfig> entries, Runnable body) {
        List<? extends UnmodifiableConfig> original = ServerConfig.GUN_OVERRIDES.get();
        ServerConfig.GUN_OVERRIDES.set(entries);
        try {
            body.run();
        } finally {
            ServerConfig.GUN_OVERRIDES.set(original);
        }
    }

    private GunConfigTests() {
    }
}
