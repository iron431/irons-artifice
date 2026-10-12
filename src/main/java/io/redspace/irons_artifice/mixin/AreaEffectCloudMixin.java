package io.redspace.irons_artifice.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.redspace.irons_artifice.modifier.on_hit_handlers.VenomOnHit;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AreaEffectCloud.class)
public abstract class AreaEffectCloudMixin {
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isAffectedByPotions()Z"
            )
    )
    private boolean irons_artifice$venomCloudSparesOwner(LivingEntity entity, Operation<Boolean> original) {
        return original.call(entity) && VenomOnHit.cloudAffects((AreaEffectCloud) (Object) this, entity);
    }
}
