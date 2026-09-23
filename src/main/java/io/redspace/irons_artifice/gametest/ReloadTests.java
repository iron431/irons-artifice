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

    /**
     * A mob shooter rather than {@code TestFixtures.shooter}, matching {@link #reloadBlocksFiring},
     * which has no choice: its shot reaches {@code tryFire}'s broadcast-sound path once the reload
     * is removed, and a mock {@code ServerPlayer} cannot receive that broadcast (see
     * {@link TestFixtures#firingShooter}). A mob is safe here for a related reason --
     * {@code ReloadCueStack#play} sends its custom-payload sound packet only to a
     * {@code ServerPlayer}, and falls back to the ordinary {@code Level#playSound} broadcast for
     * anything else. Nothing this test calls goes near
     * {@code GunplayManager.requiresAmmo}'s {@code Player} cast, so no coverage is lost.
     */
    static void reloadProgressesToCompletion(GameTestHelper helper) {
        ItemStack gun = TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 0);
        LivingEntity shooter = TestFixtures.firingShooter(helper, new BlockPos(1, 1, 1), gun);
        ItemStack held = shooter.getMainHandItem();
        GunItem gunItem = (GunItem) held.getItem();

        ReloadState started = ReloadState.start(held, 20, 1.0, 1, null);
        helper.assertTrue(GunItem.isReloading(held), "the gun reports reloading once a reload starts");
        helper.assertFalse(started.isFinished(), "a freshly started reload is not finished");

        // Drive the production path, ReloadState.tickReload, rather than increment/isFinished on a
        // local: it is what advances progress on the stack and calls ReloadState.remove on
        // completion. Nothing in the test level ticks a held reload, so the test does it here, the
        // way reloadTopLoadAppliesSkip drives applySkip.
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
        LivingEntity shooter = TestFixtures.firingShooter(helper, new BlockPos(1, 1, 1), gun);
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

    /**
     * The top-load skip path the two tests above do not touch. {@code ReloadState.tickReload} calls
     * {@code applySkip} itself, but only after each tick's increment, so the skip boundary -- just
     * before against just inside the insert loop -- reads more clearly by calling {@code increment}
     * and {@code applySkip} directly.
     * <p>
     * Left uncovered: a partial top-load through {@code GunplayManager.attemptStartReload} with a
     * real gun's {@code TopLoadConfig} and ammo count, and the "rounds land in the magazine" half
     * through {@code attemptFinishReload}. Nothing here drives the item-use tick that calls them.
     */
    static void reloadTopLoadAppliesSkip(GameTestHelper helper) {
        ItemStack gun = TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 0);

        // This test's own numbers, not gun balance ones: at roundsToLoad = 1,
        // TopLoadConfig.resumeFrom(1) reduces to loopEnd exactly, since loopDuration * (1 - 1) is
        // zero, so skipTo lands on loopEnd below.
        TopLoadConfig topLoad = new TopLoadConfig(0.2, 0.8, 0.6);
        ReloadState state = ReloadState.start(gun, 20, 1.0, 1, topLoad);

        helper.assertTrue(state.hasSkip(),
                "a top-load config whose skip target is past its skip point reports hasSkip");

        // Before skipAt (progress 0.1 < 0.2): applySkip leaves progress where increment left it.
        ReloadState beforeWindow = state.increment(2);
        helper.assertValueEqual(beforeWindow.applySkip().progress(), beforeWindow.progress(),
                "before the insert loop, applySkip leaves progress untouched");

        // Inside the insert loop (progress 0.25, within [0.2, 0.8)): applySkip jumps progress to
        // the configured skip target, which is loopEnd for a single round.
        ReloadState insideWindow = state.increment(5);
        helper.assertValueEqual(insideWindow.applySkip().progress(), topLoad.loopEnd(),
                "reaching the insert loop jumps progress to the configured skip target");

        helper.succeed();
    }
}
