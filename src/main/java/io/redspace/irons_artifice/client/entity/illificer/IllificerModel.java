package io.redspace.irons_artifice.client.entity.illificer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.irons_artifice.client.gun.GunArmPoses;
import io.redspace.irons_artifice.entity.Illificer;
import io.redspace.irons_artifice.gun.ArmPoseKind;
import io.redspace.irons_artifice.item.FireDelayState;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class IllificerModel extends IllagerModel<Illificer> {
    protected final ModelPart rightArm;
    protected final ModelPart leftArm;
    /**
     * Main arm posed in the idle gun stance by the most recent {@link #setupAnim} call, or null when the
     * illificer is not holding a gun idly. Consumed by {@link #translateToHand} since its 1.21.1 signature
     * carries no entity reference.
     */
    private HumanoidArm idleGunArm;

    public IllificerModel(ModelPart root) {
        super(root);
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.getHat().visible = true;
    }

    @Override
    public void setupAnim(Illificer entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.idleGunArm = null;
        if (entity.getMainHandItem().getItem() instanceof GunItem gun) {
            if (entity.isAggressive() || FireDelayState.isActive(entity, entity.getMainHandItem()) || GunItem.isReloading(entity.getMainHandItem())) {
                var humanoidProxy = new HumanoidModel<>(this.root());
                var pose = gun.getGun().armPoseKind() == ArmPoseKind.PISTOL ? GunArmPoses.PISTOL.getValue() : GunArmPoses.RIFLE.getValue();
                pose.applyTransform(humanoidProxy, entity, entity.getMainArm());
            } else {
                this.idleGunArm = entity.getMainArm();
                var arm = getArm(this.idleGunArm);
                arm.xRot *= 0.25f;
                arm.xRot -= Mth.PI / 6f;
            }
        }
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        super.translateToHand(arm, poseStack);
        if (this.idleGunArm != null && arm == this.idleGunArm) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.translate(0, -1, 0);
        }
    }

    protected ModelPart getArm(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
    }
}
