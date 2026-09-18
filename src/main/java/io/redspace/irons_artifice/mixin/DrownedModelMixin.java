package io.redspace.irons_artifice.mixin;

import io.redspace.irons_artifice.client.gun.GunArmPoses;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrownedModel.class)
public class DrownedModelMixin {
    @SuppressWarnings("unchecked")
    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void irons_artifice$drownedGunAnimation(Zombie entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        GunArmPoses.applyHeldGunPoses((HumanoidModel<Zombie>) (Object) this, entity);
    }
}
