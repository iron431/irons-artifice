package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.gametest.TestFixtures.LaneComparison;
import io.redspace.irons_artifice.gametest.TestFixtures.LaneResult;
import io.redspace.irons_artifice.gametest.TestFixtures.LaneShots;
import io.redspace.irons_artifice.modifier.on_hit_handlers.ChainLightningOnHit;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Self-tests for {@link TestFixtures#fireLanes}, the harness every modifier test is built on.
 * <p>
 * A modifier test says something only if the two lanes are comparable to begin with. Were they
 * systematically different -- different targets, different geometry, one lane's shot reaching the
 * other's measurements -- then "the variant lane did more damage" would be evidence about the
 * harness rather than the modifier. So this fires the harness with no modifiers, which makes the
 * two lanes' inputs identical, and asserts the outputs match.
 */
public final class LaneHarnessTests {

    /**
     * Close enough that the bullet reaches the target on its first tick of travel, so the shot is
     * not on the wing long enough for drag or gravity to matter.
     */
    private static final int TARGET_DISTANCE = 8;

    /** Long enough for the bullet to land, hurt the target, and for the knockback to play out. */
    private static final int SETTLE_TICKS = 10;

    /**
     * How far apart the two lanes' damage may land, as a fraction of the larger. Relative so that no
     * balance number is written here: whatever the gun deals, the lanes must agree within this share.
     */
    private static final float DAMAGE_MATCH_TOLERANCE = 0.05F;

    static void harnessIsolatesLanes(GameTestHelper helper) {
        TestFixtures.fireLanes(helper, ItemRegistry.MUSKET.get(), TARGET_DISTANCE, SETTLE_TICKS)
                .thenCompare(lanes -> {
                    LaneResult control = lanes.control();
                    LaneResult variant = lanes.variant();

                    // Separation is what keeps the lanes independent; read off the lane geometry
                    // rather than a number written here.
                    int separation = TestFixtures.laneOrigin(TestFixtures.VARIANT_LANE).getX()
                            - TestFixtures.laneOrigin(TestFixtures.CONTROL_LANE).getX();
                    helper.assertTrue(separation > ChainLightningOnHit.RADIUS,
                            "the lanes stand further apart (" + separation + " blocks) than the widest "
                                    + "on-hit effect reaches (" + ChainLightningOnHit.RADIUS + " blocks)");

                    // A harness that fired the control lane alone and reported its result as both
                    // would pass every other assertion here.
                    helper.assertTrue(control.shooter() != variant.shooter(),
                            "the two lanes were fired by two different shooters");
                    helper.assertTrue(control.target() != variant.target(),
                            "the two lanes were measured on two different targets");

                    helper.assertTrue(control.bulletsSpawned() > 0,
                            "the control lane put at least one bullet in the air");
                    helper.assertTrue(variant.bulletsSpawned() == control.bulletsSpawned(),
                            "both lanes put the same number of bullets in the air (control "
                                    + control.bulletsSpawned() + ", variant " + variant.bulletsSpawned() + ")");

                    helper.assertTrue(control.damageTaken() > 0.0F, "the control lane's target took damage");
                    helper.assertTrue(variant.damageTaken() > 0.0F, "the variant lane's target took damage");

                    // The point of the test: identical inputs must produce comparable outputs, or
                    // every relative assertion built on this harness means nothing.
                    float larger = Math.max(control.damageTaken(), variant.damageTaken());
                    float difference = Math.abs(control.damageTaken() - variant.damageTaken());
                    helper.assertTrue(difference <= DAMAGE_MATCH_TOLERANCE * larger,
                            "the two lanes agree on damage to within " + (DAMAGE_MATCH_TOLERANCE * 100)
                                    + "% (control " + control.damageTaken()
                                    + ", variant " + variant.damageTaken() + ")");

                    // Each lane's displacement is measured against its own pre-shot position, and
                    // the shot pushes downrange.
                    helper.assertTrue(
                            control.targetDisplacement().z > Math.abs(control.targetDisplacement().x),
                            "the control lane's target was pushed downrange, not sideways");
                    helper.assertTrue(
                            variant.targetDisplacement().z > Math.abs(variant.targetDisplacement().x),
                            "the variant lane's target was pushed downrange, not sideways");
                });
    }

    static void controlMissTerminalRejectsTwoMisses(GameTestHelper helper) {
        LivingEntity target = TestFixtures.toughTarget(helper, new BlockPos(1, 1, 3));
        LivingEntity shooter = TestFixtures.firingShooter(helper, new BlockPos(1, 1, 1),
                TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 0));
        LaneResult miss = new LaneResult(0.0F, Vec3.ZERO, 1, target, shooter);
        LaneComparison neitherLanded = new LaneComparison(miss, miss);
        TestCatalog.Expectation.CLAIM_FAILS.check(helper,
                () -> LaneShots.assertVariantLanded(helper, neitherLanded));
        helper.succeed();
    }
}
