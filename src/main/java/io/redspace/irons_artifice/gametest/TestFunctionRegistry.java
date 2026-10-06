package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.gametest.TestCatalog.Expectation;
import io.redspace.irons_artifice.gametest.TestCatalog.ModifierTest;
import io.redspace.irons_artifice.gametest.TestCatalog.PlainTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

/**
 * Registers every test body into {@code Registries.TEST_FUNCTION} from this class's static
 * initializer, which mod construction triggers, because NeoForge seals that register before
 * {@code RegisterGameTestsEvent} fires.
 */
public final class TestFunctionRegistry {
    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, IronsArtifice.MODID);

    static {
        for (PlainTest test : TestCatalog.PLAIN_TESTS) {
            register(test.name(), test.body());
        }
        for (ModifierTest test : ModifierTests.ENTRIES) {
            Consumer<GameTestHelper> withModifier = helper ->
                    test.body().run(helper, new Item[]{test.modifier().get()}, Expectation.CLAIM_HOLDS);
            Consumer<GameTestHelper> withoutModifier = helper ->
                    test.body().run(helper, new Item[0], Expectation.CLAIM_FAILS);
            register(test.name(), withModifier);
            register(test.sadPathName(), withoutModifier);
        }
    }

    private static void register(String name, Consumer<GameTestHelper> body) {
        TEST_FUNCTIONS.register(name, () -> body);
    }

    public static ResourceKey<Consumer<GameTestHelper>> key(String name) {
        return ResourceKey.create(Registries.TEST_FUNCTION, IronsArtifice.id(name));
    }

    public static void register(IEventBus modEventBus) {
        TEST_FUNCTIONS.register(modEventBus);
    }
}
