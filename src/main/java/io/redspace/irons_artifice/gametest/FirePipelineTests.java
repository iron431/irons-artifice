package io.redspace.irons_artifice.gametest;

import software.bernie.geckolib.animatable.GeoItem;
import io.redspace.irons_artifice.item.FireDelayState;
import io.redspace.irons_artifice.item.FireOutcome;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.ReloadState;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class FirePipelineTests {

    static void refusals(GameTestHelper helper) {
        ServerPlayer shooter = TestFixtures.shooter(helper, new BlockPos(1, 2, 1), ItemStack.EMPTY);

        helper.assertValueEqual(GunplayManager.tryFire(shooter, TestFixtures.FORWARD),
                FireOutcome.NO_GUN, "empty hand is refused as NO_GUN");

        ItemStack empty = TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 0);
        shooter.setItemSlot(EquipmentSlot.MAINHAND, empty);
        TestFixtures.resetShotState(shooter);
        helper.assertValueEqual(GunplayManager.tryFire(shooter, TestFixtures.FORWARD),
                FireOutcome.EMPTY_MAGAZINE, "empty magazine is refused as EMPTY_MAGAZINE");
        helper.assertValueEqual(GunItem.getMagazine(shooter.getMainHandItem()).count(), 0,
                "refused shot left the magazine untouched");

        ItemStack loaded = TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 1);
        shooter.setItemSlot(EquipmentSlot.MAINHAND, loaded);
        TestFixtures.resetShotState(shooter);
        FireDelayState.start(shooter, shooter.getMainHandItem(), 10, 1.0F);
        helper.assertValueEqual(GunplayManager.tryFire(shooter, TestFixtures.FORWARD),
                FireOutcome.FIRE_DELAY_ACTIVE, "running fire delay is refused as FIRE_DELAY_ACTIVE");

        TestFixtures.resetShotState(shooter);
        ReloadState.set(shooter.getMainHandItem(), new ReloadState(0.0, 20.0, 1.0, 1, 0.0, 0.0));
        helper.assertValueEqual(GunplayManager.tryFire(shooter, TestFixtures.FORWARD),
                FireOutcome.RELOADING, "mid reload is refused as RELOADING");

        // TestFixtures.shooter dismisses the shooter at the end of the tick, which takes the
        // dangling ReloadState set above with it.
        helper.succeed();
    }

    static void successfulShot(GameTestHelper helper) {
        ItemStack gun = TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 2);
        LivingEntity shooter = TestFixtures.firingShooter(helper, new BlockPos(1, 2, 1), gun);

        ItemStack held = shooter.getMainHandItem();
        var profile = GunplayManager.compose(shooter, ((GunItem) held.getItem()).getGun(), held);
        int expectedDelayTicks = profile.fireDelayTicks();

        int before = GunItem.getMagazine(held).count();
        FireOutcome outcome = GunplayManager.tryFire(shooter, TestFixtures.FORWARD);

        helper.assertValueEqual(outcome, FireOutcome.FIRED, "loaded gun fires");
        helper.assertValueEqual(GunItem.getMagazine(shooter.getMainHandItem()).count(), before - 1,
                "one round consumed");
        // Not just "> 0": a port that mis-derives the delay -- dropping the FIRE_RATE divisor in
        // ShotProfile.fireDelayTicks(), say -- still starts a nonzero cycle. expectedDelayTicks
        // comes from the same compose(...) control the rest of the suite uses, not a balance number.
        helper.assertValueEqual(FireDelayState.get(shooter).duration(), expectedDelayTicks,
                "the started fire delay's duration matches profile.fireDelayTicks()");
        helper.assertEntitiesPresent(EntityRegistry.BULLET.get(), 1);

        helper.succeed();
    }

    static void delayIsGunKeyed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        ItemStack musket = TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 1);
        LivingEntity shooter = TestFixtures.firingShooter(helper, new BlockPos(1, 2, 1), musket);

        // GeoItem.getOrAssignId is public and is what GunplayManager.playFireAnimation calls.
        // Assigning real, distinct ids up front is what lets this observe per-gun keying: a
        // never-rendered stack is always keyed UNKEYED, which blocks every gun whatever the keying
        // logic does. That case is asserted separately below.
        long musketId = GeoItem.getOrAssignId(shooter.getMainHandItem(), level);
        ItemStack pistol = TestFixtures.gunWith(ItemRegistry.FLINTLOCK_PISTOL.get(), 1);
        long pistolId = GeoItem.getOrAssignId(pistol, level);
        helper.assertTrue(musketId != pistolId,
                "sanity: the musket and pistol stacks were assigned distinct GeckoLib ids");

        helper.assertValueEqual(GunplayManager.tryFire(shooter, TestFixtures.FORWARD),
                FireOutcome.FIRED, "musket fires");
        helper.assertTrue(FireDelayState.isActive(shooter, shooter.getMainHandItem()),
                "delay blocks the musket that fired, keyed by its real GeckoLib id");
        helper.assertFalse(FireDelayState.isActive(shooter, pistol),
                "delay does not block a different, distinctly-keyed gun");

        // The UNKEYED case. A never-rendered stack starts one, because tryFire records the firing
        // stack's GeckoLib id into the new FireDelayState before playFireAnimation assigns one, and
        // isActive treats it as blocking every gun rather than only the one that fired.
        TestFixtures.resetShotState(shooter);
        ItemStack neverRendered = TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 1);
        shooter.setItemSlot(EquipmentSlot.MAINHAND, neverRendered);
        helper.assertValueEqual(GunplayManager.tryFire(shooter, TestFixtures.FORWARD),
                FireOutcome.FIRED, "second gun fires without ever being assigned a GeckoLib id");
        helper.assertValueEqual(FireDelayState.get(shooter).gunId(), FireDelayState.UNKEYED,
                "a never-rendered stack's cycle is keyed as UNKEYED rather than to a real gun id");
        ItemStack otherGun = TestFixtures.gunWith(ItemRegistry.FLINTLOCK_PISTOL.get(), 1);
        helper.assertTrue(FireDelayState.isActive(shooter, otherGun),
                "an UNKEYED delay blocks a different gun too, per FireDelayState.isActive's documented fallback");

        helper.succeed();
    }
}
