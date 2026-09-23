package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.gametest.TestFixtures.LaneComparison;
import io.redspace.irons_artifice.gametest.TestFixtures.LaneShots;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.modifier.ModifierItem;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Every test in the suite. {@link TestFunctionRegistry} and {@link ArtificeGameTests} loop these lists. */
public final class TestCatalog {
    public static final Identifier BOX_SMALL = IronsArtifice.id("box_small");
    public static final Identifier RANGE_TWO_LANE = IronsArtifice.id("range_two_lane");

    /** Whether a modifier test's claim is expected to hold (modifier present) or fail (modifier removed). */
    public enum Expectation {
        CLAIM_HOLDS {
            @Override
            public void check(GameTestHelper helper, Runnable claim) {
                claim.run();
            }
        },
        CLAIM_FAILS {
            @Override
            public void check(GameTestHelper helper, Runnable claim) {
                try {
                    claim.run();
                } catch (GameTestAssertException expected) {
                    return;
                }
                throw helper.assertionException(Component.literal(
                        "the claim held with the modifier removed, so this test does not depend on its modifier"));
            }
        };

        public abstract void check(GameTestHelper helper, Runnable claim);
    }

    public record PlainTest(String name, Identifier arena, int maxTicks, boolean required,
                            Consumer<GameTestHelper> body) {
        public PlainTest(String name, Identifier arena, int maxTicks, Consumer<GameTestHelper> body) {
            this(name, arena, maxTicks, true, body);
        }
    }

    @FunctionalInterface
    public interface ModifierTestBody {
        void run(GameTestHelper helper, Item[] variantModifiers, Expectation expectation);
    }

    public record ModifierTest(DeferredItem<ModifierItem> modifier, String name, Identifier arena, int maxTicks,
                               ModifierTestBody body) {
        public String sadPathName() {
            return name + "_without_modifier";
        }
    }

    @FunctionalInterface
    public interface LaneClaim {
        void check(GameTestHelper helper, LaneComparison lanes);
    }

    @FunctionalInterface
    public interface LaneClaimWith<S> {
        void check(GameTestHelper helper, S setup, LaneComparison lanes);
    }

    public enum Terminal { CONTROL_LANDS, CONTROL_MISSES }

    public record LaneOptions(Supplier<? extends Item> gun, EntityType<? extends Mob> targetType,
                              int targetDistance, int settleTicks, Terminal terminal, int maxTicks) {
        public static final LaneOptions DEFAULT =
                new LaneOptions(ItemRegistry.MUSKET, EntityType.ZOMBIE, 8, 20, Terminal.CONTROL_LANDS, 200);

        public LaneOptions withGun(Supplier<? extends Item> gun) {
            return new LaneOptions(gun, targetType, targetDistance, settleTicks, terminal, maxTicks);
        }

        public LaneOptions withTargetType(EntityType<? extends Mob> targetType) {
            return new LaneOptions(gun, targetType, targetDistance, settleTicks, terminal, maxTicks);
        }

        public LaneOptions withTargetDistance(int targetDistance) {
            return new LaneOptions(gun, targetType, targetDistance, settleTicks, terminal, maxTicks);
        }

        public LaneOptions withSettleTicks(int settleTicks) {
            return new LaneOptions(gun, targetType, targetDistance, settleTicks, terminal, maxTicks);
        }

        public LaneOptions expectingControlMiss() {
            return new LaneOptions(gun, targetType, targetDistance, settleTicks, Terminal.CONTROL_MISSES, maxTicks);
        }

        public LaneOptions withMaxTicks(int maxTicks) {
            return new LaneOptions(gun, targetType, targetDistance, settleTicks, terminal, maxTicks);
        }
    }

    public static ModifierTest lanes(DeferredItem<ModifierItem> modifier, String name, LaneClaim claim) {
        return lanes(modifier, name, LaneOptions.DEFAULT, claim);
    }

    public static ModifierTest lanes(DeferredItem<ModifierItem> modifier, String name, LaneOptions options,
                                     LaneClaim claim) {
        return lanes(modifier, name, options, helper -> null, (helper, unused, lanes) -> claim.check(helper, lanes));
    }

    public static <S> ModifierTest lanes(DeferredItem<ModifierItem> modifier, String name,
                                         Function<GameTestHelper, S> setup, LaneClaimWith<S> claim) {
        return lanes(modifier, name, LaneOptions.DEFAULT, setup, claim);
    }

    /** Runs {@code setup} before the lanes are armed and hands its result to the claim. */
    public static <S> ModifierTest lanes(DeferredItem<ModifierItem> modifier, String name, LaneOptions options,
                                         Function<GameTestHelper, S> setup, LaneClaimWith<S> claim) {
        return new ModifierTest(modifier, name, RANGE_TWO_LANE, options.maxTicks(),
                (helper, variantModifiers, expectation) -> {
                    S built = setup.apply(helper);
                    LaneShots shots = TestFixtures.fireLanes(helper, options.gun().get(), options.targetDistance(),
                            options.settleTicks(), options.targetType(), variantModifiers).expecting(expectation);
                    Consumer<LaneComparison> bound = lanes -> claim.check(helper, built, lanes);
                    switch (options.terminal()) {
                        case CONTROL_LANDS -> shots.thenCompare(bound);
                        case CONTROL_MISSES -> shots.thenCompareExpectingControlMiss(bound);
                        default -> throw new IllegalStateException("unhandled terminal " + options.terminal());
                    }
                });
    }

    public record Composed(ItemStack stack, ShotProfile profile, LivingEntity shooter) {
    }

    @FunctionalInterface
    public interface ComposeClaim {
        void check(GameTestHelper helper, Composed control, Composed variant);
    }

    /** A shooter and the change tryFire made to its delta movement. */
    public record Pushed(LivingEntity shooter, Vec3 push) {
    }

    @FunctionalInterface
    public interface ShooterClaim {
        void check(GameTestHelper helper, Pushed control, Pushed variant);
    }

    /** No shot: composes a plain musket and one carrying the modifier, each held by its own mob. */
    public static ModifierTest compose(DeferredItem<ModifierItem> modifier, String name, ComposeClaim claim) {
        return new ModifierTest(modifier, name, BOX_SMALL, 20, (helper, variantModifiers, expectation) -> {
            Composed control = composed(helper, new BlockPos(1, 1, 1),
                    TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 0));
            Composed variant = composed(helper, new BlockPos(4, 1, 4),
                    TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 0, variantModifiers));
            expectation.check(helper, () -> claim.check(helper, control, variant));
            helper.succeed();
        });
    }

    public static ModifierTest bespoke(DeferredItem<ModifierItem> modifier, String name, Identifier arena,
                                       int maxTicks, ModifierTestBody body) {
        return new ModifierTest(modifier, name, arena, maxTicks, body);
    }

    private static Composed composed(GameTestHelper helper, BlockPos pos, ItemStack gun) {
        LivingEntity shooter = TestFixtures.firingShooter(helper, pos, gun);
        ShotProfile profile = GunplayManager.compose(shooter, ((GunItem) gun.getItem()).getGun(), gun);
        return new Composed(gun, profile, shooter);
    }

    /** Two shooters in the box fire once on the same tick; the claim reads them straight after. */
    public static ModifierTest fireInPlace(DeferredItem<ModifierItem> modifier, String name,
                                           Supplier<? extends Item> gun, ShooterClaim claim) {
        return new ModifierTest(modifier, name, BOX_SMALL, 20, (helper, variantModifiers, expectation) -> {
            LivingEntity control = TestFixtures.firingShooter(helper, new BlockPos(1, 1, 1),
                    TestFixtures.gunWith(gun.get(), 1));
            LivingEntity variant = TestFixtures.firingShooter(helper, new BlockPos(4, 1, 4),
                    TestFixtures.gunWith(gun.get(), 1, variantModifiers));
            TestFixtures.shieldFromDaylight(control);
            TestFixtures.shieldFromDaylight(variant);
            helper.startSequence()
                    .thenExecuteAfter(TestFixtures.GROUNDING_TICKS,
                            () -> TestFixtures.withFailureNet(helper, "firing in place", () -> {
                                Pushed controlPush = fire(helper, "control", control);
                                Pushed variantPush = fire(helper, "variant", variant);
                                expectation.check(helper, () -> claim.check(helper, controlPush, variantPush));
                            }))
                    .thenSucceed();
        });
    }

    /** A grounded mob already carries a tick of gravity, so the push is the change, not the vector. */
    private static Pushed fire(GameTestHelper helper, String lane, LivingEntity shooter) {
        Vec3 before = shooter.getDeltaMovement();
        TestFixtures.assertFired(helper, lane, GunplayManager.tryFire(shooter, TestFixtures.FORWARD));
        return new Pushed(shooter, shooter.getDeltaMovement().subtract(before));
    }

    public static final List<PlainTest> PLAIN_TESTS = List.of(
            new PlainTest("components_survive_save", BOX_SMALL, 20, PersistenceTests::componentsSurviveSave),
            new PlainTest("components_survive_network", BOX_SMALL, 20, PersistenceTests::componentsSurviveNetwork),
            new PlainTest("attachment_defaults", BOX_SMALL, 20, DataAttachmentTests::attachmentDefaults),
            new PlainTest("pending_shot_clears_on_swap", BOX_SMALL, 60, DataAttachmentTests::pendingShotClearsOnSwap),
            new PlainTest("every_gun_composes_with_every_modifier", BOX_SMALL, 60, LoadSmokeTests::everyGunComposesWithEveryModifier),
            new PlainTest("fixture_builds_a_loaded_shooter", BOX_SMALL, 20, FixtureSelfTests::fixtureBuildsALoadedShooter),
            new PlainTest("fire_refusals", BOX_SMALL, 40, FirePipelineTests::refusals),
            new PlainTest("successful_shot", BOX_SMALL, 40, FirePipelineTests::successfulShot),
            new PlainTest("delay_is_gun_keyed", BOX_SMALL, 40, FirePipelineTests::delayIsGunKeyed),
            new PlainTest("early_shot_queues_and_flushes", BOX_SMALL, 60, ShotQueueTests::earlyShotQueuesAndFlushes),
            new PlainTest("early_shot_refused_outside_tolerance", BOX_SMALL, 40, ShotQueueTests::earlyShotRefusedOutsideTolerance),
            new PlainTest("pending_shot_expires", BOX_SMALL, 40, ShotQueueTests::pendingShotExpires),
            new PlainTest("firing_adds_recoil", BOX_SMALL, 40, RecoilTests::firingAddsRecoil),
            new PlainTest("recoil_decays", BOX_SMALL, 40, RecoilTests::recoilDecays),
            new PlainTest("recoil_impulses_accumulate", BOX_SMALL, 40, RecoilTests::impulsesAccumulate),
            new PlainTest("recoil_offset_bends_shot_direction", BOX_SMALL, 40, RecoilTests::offsetBendsShotDirection),
            new PlainTest("reload_progresses_to_completion", BOX_SMALL, 60, ReloadTests::reloadProgressesToCompletion),
            new PlainTest("reload_blocks_firing", BOX_SMALL, 40, ReloadTests::reloadBlocksFiring),
            new PlainTest("reload_top_load_applies_skip", BOX_SMALL, 20, ReloadTests::reloadTopLoadAppliesSkip),
            new PlainTest("harness_isolates_lanes", RANGE_TWO_LANE, 200, LaneHarnessTests::harnessIsolatesLanes),
            new PlainTest("control_miss_terminal_rejects_two_misses", BOX_SMALL, 20, LaneHarnessTests::controlMissTerminalRejectsTwoMisses),
            new PlainTest("every_modifier_has_a_test", BOX_SMALL, 20, ModifierCoverageTests::everyModifierHasATest)
    );

    private TestCatalog() {
    }
}
