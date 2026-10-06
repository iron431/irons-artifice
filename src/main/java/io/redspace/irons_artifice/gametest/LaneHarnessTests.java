package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.gametest.TestFixtures.LaneResult;
import io.redspace.irons_artifice.modifier.on_hit_handlers.ChainLightningOnHit;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Fires {@link TestFixtures#fireLanes} with no modifiers, so both lanes' inputs are identical,
 * and asserts the outputs match. Every modifier test's relative claim rests on this.
 */
public final class LaneHarnessTests {

    private static final int TARGET_DISTANCE = 8;
    private static final int SETTLE_TICKS = 10;
    private static final float DAMAGE_MATCH_TOLERANCE = 0.05F;

    static void harnessIsolatesLanes(GameTestHelper helper) {
        TestFixtures.fireLanes(helper, ItemRegistry.MUSKET.get(), TARGET_DISTANCE, SETTLE_TICKS)
                .thenCompare(lanes -> {
                    LaneResult control = lanes.control();
                    LaneResult variant = lanes.variant();

                    int separation = TestFixtures.laneOrigin(TestFixtures.VARIANT_LANE).getX()
                            - TestFixtures.laneOrigin(TestFixtures.CONTROL_LANE).getX();
                    helper.assertTrue(separation > ChainLightningOnHit.RADIUS,
                            "the lanes stand further apart (" + separation + " blocks) than the widest "
                                    + "on-hit effect reaches (" + ChainLightningOnHit.RADIUS + " blocks)");

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

                    float larger = Math.max(control.damageTaken(), variant.damageTaken());
                    float difference = Math.abs(control.damageTaken() - variant.damageTaken());
                    helper.assertTrue(difference <= DAMAGE_MATCH_TOLERANCE * larger,
                            "the two lanes agree on damage to within " + (DAMAGE_MATCH_TOLERANCE * 100)
                                    + "% (control " + control.damageTaken()
                                    + ", variant " + variant.damageTaken() + ")");

                    helper.assertTrue(
                            control.targetDisplacement().z > Math.abs(control.targetDisplacement().x),
                            "the control lane's target was pushed downrange, not sideways");
                    helper.assertTrue(
                            variant.targetDisplacement().z > Math.abs(variant.targetDisplacement().x),
                            "the variant lane's target was pushed downrange, not sideways");
                });
    }
}
