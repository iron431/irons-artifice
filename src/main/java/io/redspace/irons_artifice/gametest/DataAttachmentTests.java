package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.advancement.ShotCombatTracker;
import io.redspace.irons_artifice.data.LastHitTarget;
import io.redspace.irons_artifice.data.RecentShots;
import io.redspace.irons_artifice.data.RecoilState;
import io.redspace.irons_artifice.item.FireDelayState;
import io.redspace.irons_artifice.item.PendingShot;
import io.redspace.irons_artifice.registry.DataAttachmentRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class DataAttachmentTests {

    public static void attachmentDefaults(GameTestHelper helper) {
        ServerPlayer player = TestFixtures.shooter(helper, new BlockPos(1, 2, 1), ItemStack.EMPTY);
        long now = helper.getLevel().getGameTime();

        helper.assertValueEqual(FireDelayState.get(player).duration(), 0, "fire delay default duration");
        helper.assertTrue(PendingShot.get(player).isEmpty(), "pending shot defaults to empty");
        helper.assertValueEqual(RecoilState.current(player, now).pitch(), 0.0F, "recoil default pitch");
        helper.assertValueEqual(RecoilState.current(player, now).yaw(), 0.0F, "recoil default yaw");
        helper.assertTrue(LastHitTarget.get(player).uuid() == null, "last hit target defaults to none");
        helper.assertValueEqual(RecentShots.count(player), 0, "recent shots defaults to zero");

        // ShotCombatTracker's default is ShotCombatTracker::new, a fresh mutable instance rather
        // than a shared sentinel, so assert on behaviour rather than identity.
        ShotCombatTracker tracker = player.getData(DataAttachmentRegistry.SHOT_COMBAT.get());
        helper.assertFalse(tracker.instaReloaded(now), "shot combat tracker starts without an insta-reload");

        // Set and read back.
        PendingShot.set(player, new PendingShot(new Vec3(0.0, 0.0, 1.0), now));
        helper.assertFalse(PendingShot.get(player).isEmpty(), "pending shot reads back after set");

        FireDelayState.start(player, new ItemStack(ItemRegistry.MUSKET.get()), 10, 1.0F);
        helper.assertValueEqual(FireDelayState.get(player).duration(), 10, "fire delay reads back after start");

        FireDelayState.clear(player);
        helper.assertValueEqual(FireDelayState.get(player).duration(), 0, "fire delay cleared");

        // RecentShots.count() returns 0 without calling getData() when nothing is attached yet, so
        // the assertion above never exercises the default-supplier wiring. Tracking a shot forces a
        // real getData()/setData() round trip through the registry.
        RecentShots.trackShot(player);
        helper.assertValueEqual(RecentShots.count(player), 1, "recent shots reads back after trackShot");

        tracker.markInstaReload(now);
        player.setData(DataAttachmentRegistry.SHOT_COMBAT.get(), tracker);
        helper.assertTrue(player.getData(DataAttachmentRegistry.SHOT_COMBAT.get()).instaReloaded(now),
                "shot combat tracker reads back after markInstaReload");

        helper.succeed();
    }

    static void pendingShotClearsOnSwap(GameTestHelper helper) {
        // Observed, cause not established: a mock ServerPlayer's LivingEquipmentChangeEvent did
        // not fire after setItemSlot during this suite's development. The clearing logic lives on
        // LivingEntity rather than Player, so a mob exercises the same path and sidesteps the open
        // question.
        LivingEntity entity = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, 3, 1));
        entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemRegistry.MUSKET.get()));

        helper.startSequence()
                .thenIdle(2)
                .thenExecute(() -> PendingShot.set(entity,
                        new PendingShot(new Vec3(0.0, 0.0, 1.0), helper.getLevel().getGameTime())))
                .thenExecute(() -> helper.assertFalse(PendingShot.get(entity).isEmpty(),
                        "pending shot was stored before the swap"))
                .thenExecute(() -> entity.setItemSlot(EquipmentSlot.MAINHAND,
                        new ItemStack(ItemRegistry.FLINTLOCK_PISTOL.get())))
                .thenIdle(2)
                .thenExecute(() -> helper.assertTrue(PendingShot.get(entity).isEmpty(),
                        "pending shot cleared when the mainhand changed"))
                .thenSucceed();
    }
}
