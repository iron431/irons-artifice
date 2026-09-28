package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.gametest.TestCatalog.ModifierTest;
import io.redspace.irons_artifice.gametest.TestCatalog.PlainTest;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.List;
import java.util.function.Consumer;

@EventBusSubscriber(modid = IronsArtifice.MODID)
public final class ArtificeGameTests {
    public static final Identifier ENVIRONMENT = IronsArtifice.id("suite");

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment =
                event.registerEnvironment(ENVIRONMENT, new TestEnvironmentDefinition.AllOf(List.of()));

        for (PlainTest test : TestCatalog.PLAIN_TESTS) {
            registerTest(event, environment, test.name(), test.arena(), test.maxTicks(), test.required());
        }

        for (ModifierTest test : ModifierTests.ENTRIES) {
            registerTest(event, environment, test.name(), test.arena(), test.maxTicks(), true);
            registerTest(event, environment, test.sadPathName(), test.arena(), test.maxTicks(), true);
        }
    }

    private static void registerTest(RegisterGameTestsEvent event,
                                     Holder<TestEnvironmentDefinition<?>> environment,
                                     String name,
                                     Identifier structure,
                                     int maxTicks,
                                     boolean required) {
        ResourceKey<Consumer<GameTestHelper>> function = TestFunctionRegistry.key(name);
        event.registerTest(
                function.identifier(),
                new FunctionGameTestInstance(function, new TestData<>(environment, structure, maxTicks, 0, required)));
    }
}
