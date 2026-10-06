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
        return TestFixtures.firingShooter(helper, new BlockPos(1, 2, 1), gun);
    }

    static void earlyShotQueuesAndFlushes(GameTestHelper helper) {
        LivingEntity shooter = armedShooter(helper, 1);

        // Face -X while queueing +Z, so a flush that used the shooter's look angle is caught.
        shooter.setYRot(90.0F);
        shooter.setXRot(0.0F);

        FireDelayState.start(shooter, shooter.getMainHandItem(), 1, 1.0F);

        boolean queued = GunplayManager.queueEarlyShot(shooter, TestFixtures.FORWARD);
        helper.assertTrue(queued, "shot inside the tolerance window was queued");
        helper.assertFalse(PendingShot.get(shooter).isEmpty(), "a pending shot was stored");

        // The entity tick flushes only once the delay has cleared.
        FireDelayState.clear(shooter);
        GunplayManager.flushPendingShot(shooter);

        helper.assertTrue(PendingShot.get(shooter).isEmpty(), "flushing cleared the pending shot");
        helper.assertEntitiesPresent(EntityRegistry.BULLET.get(), 1);

        Bullet bullet = helper.findOneEntity(EntityRegistry.BULLET.get());
        Vec3 velocity = bullet.getDeltaMovement();
        helper.assertTrue(velocity.z > 0 && velocity.z > Math.abs(velocity.x),
                "bullet velocity is dominated by the queued +Z direction, not the shooter's -X facing");

        helper.succeed();
    }

    static void earlyShotRefusedOutsideTolerance(GameTestHelper helper) {
        LivingEntity shooter = armedShooter(helper, 1);

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

        PendingShot stale = new PendingShot(TestFixtures.FORWARD, now - (PendingShot.MAX_AGE_TICKS + 1));
        PendingShot.set(shooter, stale);

        GunplayManager.flushPendingShot(shooter);

        helper.assertTrue(PendingShot.get(shooter).isEmpty(), "flushing cleared the expired shot");
        helper.assertEntityNotPresent(EntityRegistry.BULLET.get());

        helper.succeed();
    }
}
