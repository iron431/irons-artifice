package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.entity.SoulfireCoin;
import io.redspace.irons_artifice.registry.IronsArtificeEntities;
import io.redspace.irons_artifice.registry.IronsArtificeItems;
import io.redspace.ironslib.soulfire.SoulFireHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;

/** An unmodified shot through an armed soulfire coin, against the same shot down an empty lane. */
public final class SoulfireCoinTests {

    private static final int TARGET_DISTANCE = 8;
    /** Two blocks short of the target, so the target is the coin's nearest redirect candidate. */
    private static final int COIN_DISTANCE = 6;
    /** Soul fire's next damage tick lands after invulnerability ends, ten ticks past this window. */
    private static final int SETTLE_TICKS = 10;

    static void empowersBullet(GameTestHelper helper) {
        spawnArmedCoinOnAimLine(helper, TestFixtures.VARIANT_LANE);
        TestFixtures.fireLanes(helper, IronsArtificeItems.MUSKET.get(), TARGET_DISTANCE, SETTLE_TICKS)
                .thenCompare(lanes -> {
                    LivingEntity control = lanes.control().target();
                    LivingEntity variant = lanes.variant().target();
                    helper.assertTrue(TestFixtures.getEntities(helper, IronsArtificeEntities.SOUL.get()).isEmpty(),
                            "the variant lane's shot struck the coin");
                    helper.assertFalse(control.isOnFire(), "the shot down the empty lane left its target unlit");
                    helper.assertTrue(variant.isOnFire() && SoulFireHelper.isSoulFire(variant),
                            "the shot through the coin set its target alight with soul fire");
                    float plain = lanes.control().damageTaken();
                    float empowered = lanes.variant().damageTaken();
                    helper.assertTrue(empowered > plain * (1 + SoulfireCoin.DAMAGE_BONUS / 2),
                            "the shot through the coin added soul damage (plain " + plain + ", empowered " + empowered + ")");
                });
    }

    /** Hovering, already armed, and centred on the line from the shooter's eye to the target's hitbox centre. */
    private static void spawnArmedCoinOnAimLine(GameTestHelper helper, int lane) {
        Zombie zombie = EntityType.ZOMBIE.create(helper.getLevel());
        double eyeY = TestFixtures.laneOrigin(lane).getY() + zombie.getEyeHeight();
        double targetCentreY = TestFixtures.laneOrigin(lane).getY() + zombie.getBbHeight() / 2.0;
        double aimLineY = eyeY + (targetCentreY - eyeY) * COIN_DISTANCE / TARGET_DISTANCE;

        SoulfireCoin coin = new SoulfireCoin(IronsArtificeEntities.SOUL.get(), helper.getLevel());
        BlockPos column = TestFixtures.targetPos(lane, COIN_DISTANCE);
        coin.setPos(helper.absoluteVec(new Vec3(column.getX() + 0.5, aimLineY - coin.getBbHeight() / 2.0, column.getZ() + 0.5)));
        coin.setNoGravity(true);
        coin.tickCount = SoulfireCoin.ARMING_TICKS;
        helper.getLevel().addFreshEntity(coin);
    }
}
