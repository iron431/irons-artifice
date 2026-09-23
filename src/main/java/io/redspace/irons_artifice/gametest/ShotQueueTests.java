package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.item.FireDelayState;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.PendingShot;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class ShotQueueTests {

    private static LivingEntity armedShooter(GameTestHelper helper, int rounds) {
        ItemStack gun = TestFixtures.gunWith(ItemRegistry.MUSKET.get(), rounds);
        return TestFixtures.firingShooter(helper, new BlockPos(1, 1, 1), gun);
    }

    static void earlyShotQueuesAndFlushes(GameTestHelper helper) {
        LivingEntity shooter = armedShooter(helper, 1);

        // Face the shooter well off the direction being queued (+Z), so the two can never be
        // confused: a port that swapped pending.direction() for the shooter's look angle at the
        // flush call site sends the bullet along -X, not slightly off +Z.
        shooter.setYRot(90.0F);
        shooter.setXRot(0.0F);

        // One tick left on the cycle is inside EARLY_SHOT_TOLERANCE_TICKS.
        FireDelayState.start(shooter, shooter.getMainHandItem(), 1, 1.0F);

        boolean queued = GunplayManager.queueEarlyShot(shooter, TestFixtures.FORWARD);
        helper.assertTrue(queued, "shot inside the tolerance window was queued");
        helper.assertFalse(PendingShot.get(shooter).isEmpty(), "a pending shot was stored");

        // ServerEvents.onEntityTick flushes only after FireDelayState.tick has cleared the cycle,
        // and tryFire refuses with FIRE_DELAY_ACTIVE while one is running -- so flushing without
        // clearing first would fail against a correct GunplayManager. Simulating the elapsed cycle
        // is cheaper than ticking the mob, which this fixture needs for nothing else.
        FireDelayState.clear(shooter);
        GunplayManager.flushPendingShot(shooter);

        helper.assertTrue(PendingShot.get(shooter).isEmpty(), "flushing cleared the pending shot");
        helper.assertEntitiesPresent(EntityRegistry.BULLET.get(), 1);

        // The bullet must have taken the queued +Z, not the -X facing set above.
        // GunplayManager.fireShot hands the direction straight to Bullet#shoot, and Bullet
        // overrides getMovementToShoot to use Utils.directionWithinCone rather than
        // Projectile#shoot's per-axis inaccuracy, so spread is a cone half-angle in degrees.
        //
        // Two things widen that cone past MUSKET's own 0.5. compose posts ComposeShotEvent, where
        // IGunslingerMob.applyDefaultMobNerfs adds 4 - difficultyId degrees -- +2 on the NORMAL
        // this server runs -- giving 2.5. Then getSpreadForEntity multiplies by IN_AIR_PENALTY
        // (1.5) for a shooter off the ground, which this zombie is on its spawn tick: 3.75.
        //
        // A 3.75-degree cone around +Z bounds the wrong-axis component at sin(3.75 deg) =~ 0.065
        // against cos(3.75 deg) =~ 0.998 on +Z, so comparing magnitudes cannot mistake -X for +Z
        // wherever in the cone the shot lands.
        Bullet bullet = helper.findOneEntity(EntityRegistry.BULLET.get());
        Vec3 velocity = bullet.getDeltaMovement();
        helper.assertTrue(velocity.z > 0 && velocity.z > Math.abs(velocity.x),
                "bullet velocity is dominated by the queued +Z direction, not the shooter's -X facing");

        helper.succeed();
    }

    static void earlyShotRefusedOutsideTolerance(GameTestHelper helper) {
        LivingEntity shooter = armedShooter(helper, 1);

        // Far more remaining than the tolerance allows.
        FireDelayState.start(shooter, shooter.getMainHandItem(),
                GunplayManager.EARLY_SHOT_TOLERANCE_TICKS + 20, 1.0F);

        boolean queued = GunplayManager.queueEarlyShot(shooter, TestFixtures.FORWARD);

        helper.assertFalse(queued, "shot outside the tolerance window was refused");
        helper.assertTrue(PendingShot.get(shooter).isEmpty(), "nothing was stored for a refused shot");

        helper.succeed();
    }

    static void pendingShotExpires(GameTestHelper helper) {
        LivingEntity shooter = armedShooter(helper, 1);
        long now = helper.getLevel().getGameTime();

        // Older than MAX_AGE_TICKS, so flushPendingShot's own hasExpired() call must treat it as
        // stale. Asserting stale.hasExpired(now) here would only re-run that formula on data built
        // to satisfy it.
        PendingShot stale = new PendingShot(TestFixtures.FORWARD, now - (PendingShot.MAX_AGE_TICKS + 1));
        PendingShot.set(shooter, stale);

        GunplayManager.flushPendingShot(shooter);

        helper.assertTrue(PendingShot.get(shooter).isEmpty(), "flushing cleared the expired shot");
        helper.assertEntityNotPresent(EntityRegistry.BULLET.get());

        helper.succeed();
    }
}
