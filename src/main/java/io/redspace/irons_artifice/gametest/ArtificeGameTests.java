package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.gametest.TestCatalog.Expectation;
import io.redspace.irons_artifice.gametest.TestCatalog.ModifierTest;
import io.redspace.irons_artifice.gametest.TestCatalog.PlainTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Turns the catalog into the framework's own {@link TestFunction}s. NeoForge finds this class through
 * {@link GameTestHolder} in the mod's scan data and calls the generator once, so a test is one object
 * rather than a registered body plus a registered contract, and there is no registry to seal.
 */
@GameTestHolder(IronsArtifice.MODID)
public final class ArtificeGameTests {
    /** The suite has no per-batch setup, so every test shares the framework's default batch. */
    private static final String BATCH = "defaultBatch";

    @GameTestGenerator
    public static Collection<TestFunction> generateTests() {
        List<TestFunction> functions = new ArrayList<>();

        for (PlainTest test : TestCatalog.PLAIN_TESTS) {
            functions.add(testFunction(test.name(), test.arena(), test.maxTicks(), test.required(), test.body()));
        }

        for (ModifierTest test : ModifierTests.ENTRIES) {
            Consumer<GameTestHelper> withModifier = helper ->
                    test.body().run(helper, new Item[]{test.modifier().get()}, Expectation.CLAIM_HOLDS);
            Consumer<GameTestHelper> withoutModifier = helper ->
                    test.body().run(helper, new Item[0], Expectation.CLAIM_FAILS);
            functions.add(testFunction(test.name(), test.arena(), test.maxTicks(), true, withModifier));
            functions.add(testFunction(test.sadPathName(), test.arena(), test.maxTicks(), true, withoutModifier));
        }

        return functions;
    }

    private static TestFunction testFunction(String name, ResourceLocation arena, int maxTicks, boolean required,
                                             Consumer<GameTestHelper> body) {
        return new TestFunction(BATCH, name, arena.toString(), maxTicks, 0L, required, body);
    }

    private ArtificeGameTests() {
    }
}
