package io.redspace.irons_artifice.gametest;

import io.redspace.irons_artifice.item.PendingShot;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class DataAttachmentTests {

    /** A mob rather than a mock player: the clearing logic lives on LivingEntity, and a mock player's equipment-change event does not fire. */
    static void pendingShotClearsOnSwap(GameTestHelper helper) {
        LivingEntity entity = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
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
