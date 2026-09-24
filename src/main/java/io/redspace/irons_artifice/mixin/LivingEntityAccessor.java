package io.redspace.irons_artifice.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    /**
     * The equipment whose attribute modifiers are currently applied. Only maintained on the server, and lags the real equipment until the entity next ticks.
     */
    @Accessor("lastEquipmentItems")
    Map<EquipmentSlot, ItemStack> getLastEquipmentItems();
}
