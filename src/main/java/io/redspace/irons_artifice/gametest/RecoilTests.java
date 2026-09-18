package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.data.RecoilState;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.item.FireOutcome;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.registry.DataAttachmentRegistry;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class RecoilTests {

    private static LivingEntity armedShooter(GameTestHelper helper, int rounds) {
        ItemStack gun = TestFixtures.gunWith(ItemRegistry.MUSKET.get(), rounds);
        return TestFixtures.firingShooter(helper, new BlockPos(1, 2, 1), gun);
    }

    static void firingAddsRecoil(GameTestHelper helper) {
        LivingEntity shooter = armedShooter(helper, 1);
        long now = helper.getLevel().getGameTime();

        helper.assertValueEqual(RecoilState.current(shooter, now).pitch(), 0.0F, "no recoil before firing");

        helper.assertValueEqual(GunplayManager.tryFire(shooter, TestFixtures.FORWARD),
                FireOutcome.FIRED, "the shot fired");

        RecoilState after = RecoilState.current(shooter, helper.getLevel().getGameTime());
        helper.assertTrue(after.pitch() > 0.0F, "firing raised the recoil pitch offset");

        // No assertion on after.tick() here, deliberately: RecoilState.current(living, now) stamps
        // its return with whatever "now" it was passed, so comparing that against the same
        // expression cannot fail for any implementation, correct or broken.

        helper.succeed();
    }

    static void recoilDecays(GameTestHelper helper) {
        LivingEntity shooter = armedShooter(helper, 1);
        long fireTime = helper.getLevel().getGameTime();

        GunplayManager.tryFire(shooter, TestFixtures.FORWARD);

        float atFire = RecoilState.current(shooter, fireTime).pitch();
        float afterFive = RecoilState.current(shooter, fireTime + 5L).pitch();
        float afterTwenty = RecoilState.current(shooter, fireTime + 20L).pitch();

        helper.assertTrue(afterFive < atFire, "recoil decayed after five ticks");
        helper.assertTrue(afterTwenty < afterFive, "recoil kept decaying after twenty ticks");
        helper.assertTrue(afterTwenty >= 0.0F, "recoil never decays past zero");

        helper.succeed();
    }

    static void impulsesAccumulate(GameTestHelper helper) {
        LivingEntity shooter = armedShooter(helper, 2);
        long now = helper.getLevel().getGameTime();

        ItemStack held = shooter.getMainHandItem();
        var profile = GunplayManager.compose(shooter, ((GunItem) held.getItem()).getGun(), held);

        RecoilState.addImpulse(shooter, now, profile);
        float afterOne = RecoilState.current(shooter, now).pitch();

        RecoilState.addImpulse(shooter, now, profile);
        float afterTwo = RecoilState.current(shooter, now).pitch();

        helper.assertTrue(afterOne > 0.0F, "first impulse raised recoil");
        helper.assertTrue(afterTwo > afterOne, "second impulse in the same tick added to the first");

        helper.succeed();
    }

    /**
     * {@link GunplayManager#tryFire} turns a recoil offset into the shot's direction with
     * {@code pitch = rotation.x - offset.pitch(); yaw = rotation.y + offset.yaw();} -- pitch
     * subtracted, yaw added. Flip either sign and nothing else about the shot changes: the magazine
     * still depletes, the sound still plays, a bullet still spawns. No other test here would catch
     * it.
     * <p>
     * So this builds an independent control aim by applying that combination to
     * {@link TestFixtures#FORWARD}'s rotation through the {@link TestFixtures#rotationOf(Vec3)} /
     * {@link Vec3#directionFromRotation(float, float)} round trip, then fires a shooter carrying a
     * directly-set {@link RecoilState} offset straight along FORWARD. The control shot never touches
     * a nonzero offset, so it exercises none of the code under test. Right signs and both shots land
     * on the same aim; a flipped sign swings the offset shot by roughly twice the offset, far
     * outside the tolerance below.
     */
    static void offsetBendsShotDirection(GameTestHelper helper) {
        LivingEntity shooter = armedShooter(helper, 2);
        long now = helper.getLevel().getGameTime();

        // This test's own numbers, not gun balance ones, picked large enough that a sign flip's
        // effect -- roughly double the offset, tens of degrees -- dwarfs the bullet's spread. That
        // spread is a 3.75-degree cone: MUSKET's 0.5 plus the 2 degrees
        // IGunslingerMob.applyDefaultMobNerfs adds on NORMAL, times IN_AIR_PENALTY because a zombie
        // is airborne on its spawn tick (ShotQueueTests.earlyShotQueuesAndFlushes has the
        // arithmetic). Two shots from a cone that wide differ by at most 2 * sin(3.75 deg) =~ 0.131
        // as unit vectors -- a hard bound, not a probability -- so spread alone cannot cross the
        // 0.15 tolerance below. That is 13% of headroom, not orders of magnitude: a wider gun here
        // would need it revisited.
        float pitchOffset = 20.0F;
        float yawOffset = 15.0F;

        Vec2 base = TestFixtures.rotationOf(TestFixtures.FORWARD);
        Vec3 expectedDirection = Vec3.directionFromRotation(base.x - pitchOffset, base.y + yawOffset);

        helper.assertValueEqual(GunplayManager.tryFire(shooter, expectedDirection),
                FireOutcome.FIRED, "control shot fired");
        Bullet controlBullet = helper.findOneEntity(EntityRegistry.BULLET.get());
        Vec3 controlVelocity = controlBullet.getDeltaMovement().normalize();
        controlBullet.discard();
        TestFixtures.resetShotState(shooter);

        // Set a known offset directly rather than through RecoilState.addImpulse: what is under
        // test is tryFire's use of the offset, not how one accumulates -- that is
        // FIRING_ADDS_RECOIL and IMPULSES_ACCUMULATE's job.
        shooter.setData(DataAttachmentRegistry.RECOIL, new RecoilState(pitchOffset, yawOffset, now));
        helper.assertValueEqual(GunplayManager.tryFire(shooter, TestFixtures.FORWARD),
                FireOutcome.FIRED, "offset shot fired");
        Vec3 offsetVelocity = helper.findOneEntity(EntityRegistry.BULLET.get()).getDeltaMovement().normalize();

        helper.assertTrue(controlVelocity.subtract(TestFixtures.FORWARD).length() > 0.3,
                "sanity: the manually rotated control aim actually differs from straight ahead");
        helper.assertTrue(controlVelocity.subtract(offsetVelocity).length() < 0.15,
                "a real recoil offset bends the shot the same way the pitch-subtracted, yaw-added "
                        + "formula predicts, within the bullet's own random spread");

        helper.succeed();
    }
}
