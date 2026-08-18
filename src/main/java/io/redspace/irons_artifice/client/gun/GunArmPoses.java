package io.redspace.irons_artifice.client.gun;

import io.redspace.irons_artifice.item.ReloadState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

public final class GunArmPoses {
    private static float QUARTER_PI = Mth.PI * 0.25f;
    public static final EnumProxy<HumanoidModel.ArmPose> PISTOL = new EnumProxy<>(
            HumanoidModel.ArmPose.class, false, false, (IArmPoseTransformer) GunArmPoses::applyPistolPose
    );

    public static final EnumProxy<HumanoidModel.ArmPose> RIFLE = new EnumProxy<>(
            HumanoidModel.ArmPose.class, true, true, (IArmPoseTransformer) GunArmPoses::applyRiflePose
    );

    private static <T extends HumanoidRenderState> void applyPistolPose(HumanoidModel<?> model, T renderState, HumanoidArm arm) {
        boolean holdingInRightArm = arm == HumanoidArm.RIGHT;
        ReloadState reloadState = ReloadState.get(renderState.getUseItemStackForArm(arm));
        if (reloadState != null && !reloadState.isFinished()) {
            animateCrossbowCharge(model.rightArm, model.leftArm, 1f, reloadState.percent(renderState.partialTick), holdingInRightArm);
        } else {
            var head = model.head;
            ModelPart armModel = model.getArm(arm);
            armModel.yRot = head.yRot;
            armModel.xRot = -1.5F + head.xRot;
            counteractCrouch(armModel, renderState);
        }
    }

    private static void aimDownSights(ModelPart arm, boolean isRightArm) {
//        if(GunItem.isScoping())
//        arm.x += 4 * (isRightArm ? 1 : -1)
    }

    private static <T extends HumanoidRenderState> void applyRiflePose(HumanoidModel<?> model, T renderState, HumanoidArm arm) {
        boolean holdingInRightArm = arm == HumanoidArm.RIGHT;
        ReloadState reloadState = ReloadState.get(renderState.getUseItemStackForArm(arm));
        ModelPart shootingArm = holdingInRightArm ? model.rightArm : model.leftArm;
        ModelPart supportArm = holdingInRightArm ? model.leftArm : model.rightArm;
        if (reloadState != null && !reloadState.isFinished()) {
            animateCrossbowCharge(model.rightArm, model.leftArm, 1f, reloadState.percent(renderState.partialTick), holdingInRightArm);
        } else {
            var head = model.head;
            shootingArm.z += 1;
            shootingArm.yRot = head.yRot;
            shootingArm.xRot = (-(float) Math.PI / 2F) + head.xRot + 0.1F;
            supportArm.yRot = (holdingInRightArm ? 0.8F : -0.8F) + head.yRot;
            supportArm.xRot = -1.5F + head.xRot;
            supportArm.z -= 3;
            float f = -Math.min(head.yRot, QUARTER_PI) / QUARTER_PI;
            if (head.yRot > 0) {
                supportArm.x += f * 4;
                supportArm.z += f * 2;
                shootingArm.z -= f * 2;
            } else {
                supportArm.z += f * 4;
            }
            counteractCrouch(supportArm, renderState);
            counteractCrouch(shootingArm, renderState);
        }
        supportArm.y += 1;
        shootingArm.y += 2;
    }

    private static void counteractCrouch(ModelPart arm, HumanoidRenderState state) {
        if (state.isCrouching) {
            arm.xRot -= 0.4f;
        }
    }

    public static void animateCrossbowCharge(ModelPart rightArm, ModelPart leftArm, float maxCrossbowChargeDuration, float ticksUsingItem, boolean holdingInRightArm) {
        ModelPart holdingArm = holdingInRightArm ? rightArm : leftArm;
        ModelPart pullingArm = holdingInRightArm ? leftArm : rightArm;
        holdingArm.yRot = holdingInRightArm ? -0.8F : 0.8F;
        holdingArm.xRot = -0.97079635F;
        pullingArm.xRot = holdingArm.xRot;
        float useTicks = Mth.clamp(ticksUsingItem, 0.0F, maxCrossbowChargeDuration);
        float lerpAlpha = useTicks / maxCrossbowChargeDuration;
        lerpAlpha = Mth.sin(lerpAlpha * Mth.TWO_PI) * 0.5f + 0.5f;
        pullingArm.yRot = Mth.lerp(lerpAlpha, 0.4F, 0.85F) * (float) (holdingInRightArm ? 1 : -1);
        pullingArm.xRot = Mth.lerp(lerpAlpha, pullingArm.xRot, (-(float) Math.PI / 2F));
    }
}
