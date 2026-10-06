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
        return TestFixtures.firingShooter(helper, new BlockPos(1, 1, 1), gun);
    }

    static void firingAddsRecoil(GameTestHelper helper) {
        LivingEntity shooter = armedShooter(helper, 1);
        long now = helper.getLevel().getGameTime();

        helper.assertValueEqual(RecoilState.current(shooter, now).pitch(), 0.0F, "no recoil before firing");

        helper.assertValueEqual(GunplayManager.tryFire(shooter, TestFixtures.FORWARD),
                FireOutcome.FIRED, "the shot fired");

        RecoilState after = RecoilState.current(shooter, helper.getLevel().getGameTime());
        helper.assertTrue(after.pitch() > 0.0F, "firing raised the recoil pitch offset");

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
     * tryFire applies a recoil offset as {@code pitch - offset.pitch(), yaw + offset.yaw()}. The
     * control shot is aimed along that same rotation by hand; the offset shot is aimed straight
     * ahead with the offset set on the shooter. A flipped sign swings the offset shot by roughly
     * twice the offset, far outside the tolerance.
     */
    static void offsetBendsShotDirection(GameTestHelper helper) {
        LivingEntity shooter = armedShooter(helper, 2);
        long now = helper.getLevel().getGameTime();

        // Large against the mob-fired musket's 3.75-degree cone, whose worst case separates two
        // unit vectors by 2 * sin(3.75 deg) = 0.131, under the 0.15 tolerance below.
        float pitchOffset = 20.0F;
        float yawOffset = 15.0F;

        Vec2 base = TestFixtures.FORWARD.rotation();
        Vec3 expectedDirection = Vec3.directionFromRotation(base.x - pitchOffset, base.y + yawOffset);

        helper.assertValueEqual(GunplayManager.tryFire(shooter, expectedDirection),
                FireOutcome.FIRED, "control shot fired");
        Bullet controlBullet = helper.findOneEntity(EntityRegistry.BULLET.get());
        Vec3 controlVelocity = controlBullet.getDeltaMovement().normalize();
        controlBullet.discard();
        TestFixtures.resetShotState(shooter);

        shooter.setData(DataAttachmentRegistry.RECOIL, new RecoilState(pitchOffset, yawOffset, now));
        helper.assertValueEqual(GunplayManager.tryFire(shooter, TestFixtures.FORWARD),
                FireOutcome.FIRED, "offset shot fired");
        Vec3 offsetVelocity = helper.findOneEntity(EntityRegistry.BULLET.get()).getDeltaMovement().normalize();

        helper.assertTrue(controlVelocity.subtract(TestFixtures.FORWARD).length() > 0.3,
                "the rotated control aim differs from straight ahead");
        helper.assertTrue(controlVelocity.subtract(offsetVelocity).length() < 0.15,
                "the recoil offset bent the shot the way the pitch-subtracted, yaw-added formula predicts");

        helper.succeed();
    }
}
