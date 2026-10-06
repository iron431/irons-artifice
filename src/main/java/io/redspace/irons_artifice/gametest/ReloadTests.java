package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.item.FireOutcome;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.ReloadState;
import io.redspace.irons_artifice.item.TopLoadConfig;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class ReloadTests {

    static void reloadProgressesToCompletion(GameTestHelper helper) {
        ItemStack gun = TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 0);
        LivingEntity shooter = TestFixtures.firingShooter(helper, new BlockPos(1, 2, 1), gun);
        ItemStack held = shooter.getMainHandItem();
        GunItem gunItem = (GunItem) held.getItem();

        ReloadState started = ReloadState.start(held, 20, 1.0, 1, null);
        helper.assertTrue(GunItem.isReloading(held), "the gun reports reloading once a reload starts");
        helper.assertFalse(started.isFinished(), "a freshly started reload is not finished");

        // Nothing in the test level ticks a held reload, so drive ReloadState.tickReload here.
        ReloadState finished = null;
        for (int tick = 0; tick < started.durationTicks() + 1 && finished == null; tick++) {
            finished = ReloadState.tickReload(held, gunItem, shooter);
        }

        helper.assertTrue(finished != null,
                "tickReload reported the reload complete within its stated duration");
        helper.assertFalse(GunItem.isReloading(held),
                "the gun no longer reports reloading once tickReload finishes the reload");
        helper.assertTrue(ReloadState.get(held) == null,
                "tickReload removed the reload state from the stack on completion");

        helper.succeed();
    }

    static void reloadBlocksFiring(GameTestHelper helper) {
        ItemStack gun = TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 1);
        LivingEntity shooter = TestFixtures.firingShooter(helper, new BlockPos(1, 2, 1), gun);
        ItemStack held = shooter.getMainHandItem();

        ReloadState.start(held, 20, 1.0, 1, null);

        helper.assertValueEqual(GunplayManager.tryFire(shooter, TestFixtures.FORWARD),
                FireOutcome.RELOADING, "firing mid reload is refused as RELOADING");
        helper.assertValueEqual(GunItem.getMagazine(held).count(), 1,
                "the refused shot left the magazine untouched");

        ReloadState.remove(held);
        helper.assertValueEqual(GunplayManager.tryFire(shooter, TestFixtures.FORWARD),
                FireOutcome.FIRED, "firing works once the reload is gone");

        helper.succeed();
    }

    static void reloadTopLoadAppliesSkip(GameTestHelper helper) {
        ItemStack gun = TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 0);

        // At one round, TopLoadConfig.resumeFrom(1) is exactly loopEnd.
        TopLoadConfig topLoad = new TopLoadConfig(0.2, 0.8, 0.6);
        ReloadState state = ReloadState.start(gun, 20, 1.0, 1, topLoad);

        helper.assertTrue(state.hasSkip(),
                "a top-load config whose skip target is past its skip point reports hasSkip");

        ReloadState beforeWindow = state.increment(2);
        helper.assertValueEqual(beforeWindow.applySkip().progress(), beforeWindow.progress(),
                "before the insert loop, applySkip leaves progress untouched");

        ReloadState insideWindow = state.increment(5);
        helper.assertValueEqual(insideWindow.applySkip().progress(), topLoad.loopEnd(),
                "reaching the insert loop jumps progress to the configured skip target");

        helper.succeed();
    }
}
