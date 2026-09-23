package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class FixtureSelfTests {

    static void fixtureBuildsALoadedShooter(GameTestHelper helper) {
        ItemStack gun = TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 1,
                ItemRegistry.LEAD_CORE.get());
        ServerPlayer shooter = TestFixtures.shooter(helper, new BlockPos(1, 1, 1), gun);

        ItemStack held = shooter.getMainHandItem();
        helper.assertTrue(held.getItem() instanceof GunItem, "shooter is holding a gun");
        helper.assertValueEqual(GunItem.getMagazine(held).count(), 1, "magazine loaded by the fixture");

        ShotProfile withModifier = GunplayManager.compose(shooter, ((GunItem) held.getItem()).getGun(), held);
        ShotProfile without = GunplayManager.compose(shooter, ((GunItem) held.getItem()).getGun(),
                TestFixtures.gunWith(ItemRegistry.MUSKET.get(), 1));

        helper.assertTrue(
                withModifier.value(ShotComponents.KNOCKBACK) > without.value(ShotComponents.KNOCKBACK),
                "modifier written by the fixture reached compose and raised knockback");

        helper.succeed();
    }
}
