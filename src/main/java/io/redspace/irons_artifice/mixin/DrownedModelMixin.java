package io.redspace.irons_artifice.mixin;

import io.redspace.irons_artifice.client.gun.GunArmPoses;
import io.redspace.irons_artifice.gun.ArmPoseKind;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrownedModel.class)
public class DrownedModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/monster/Zombie;FFFFF)V", at = @At("TAIL"))
    private void irons_artifice$drownedGunAnimation(Zombie entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity.getMainHandItem().getItem() instanceof GunItem gunItem) {
            var pose = gunItem.getGun().armPoseKind() == ArmPoseKind.PISTOL ? GunArmPoses.PISTOL.getValue() : GunArmPoses.RIFLE.getValue();
            pose.applyTransform((DrownedModel<?>) (Object) this, entity, entity.getMainArm());
        }
        if (entity.getOffhandItem().getItem() instanceof GunItem gunItem) {
            var pose = gunItem.getGun().armPoseKind() == ArmPoseKind.PISTOL ? GunArmPoses.PISTOL.getValue() : GunArmPoses.RIFLE.getValue();
            pose.applyTransform((DrownedModel<?>) (Object) this, entity, entity.getMainArm().getOpposite());
        }
    }
}
