package io.redspace.irons_artifice.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironslib.kinetic_weapon.KineticAnimation;
import io.redspace.ironslib.kinetic_weapon.KineticWeapon;
import io.redspace.ironslib.kinetic_weapon.client.SpearAnimations;
import io.redspace.ironslib.util.Ease;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class BayonetAnimations implements KineticAnimation {

    public static final BayonetAnimations INSTANCE = new BayonetAnimations();

    private BayonetAnimations() {
    }

    private static float progress(float time, float start, float end) {
        return Mth.clamp(Mth.inverseLerp(time, start, end), 0.0F, 1.0F);
    }

    private static float hitFeedbackAmount(float ticksSinceFeedbackStart) {
        return 0.4F * (Ease.outQuart(progress(ticksSinceFeedbackStart, 1.0F, 3.0F)) - Ease.inOutSine(progress(ticksSinceFeedbackStart, 3.0F, 10.0F)));
    }

    @Override
    public void firstPersonUse(float ticksSinceKineticHitFeedback, PoseStack poseStack, float timeHeld, HumanoidArm arm, ItemStack itemStack) {
        KineticWeapon kineticWeapon = KineticWeapon.get(itemStack);
        if (kineticWeapon == null) {
            return;
        }
        SpearAnimations.UseParams params = SpearAnimations.UseParams.fromKineticWeapon(kineticWeapon, timeHeld);
        int invert = arm == HumanoidArm.RIGHT ? 1 : -1;
        Vec3 mainPose = new Vec3(-0.5, -0.075, -0.75);
        double mainPoseInterpolation = Ease.inOutBack((params.raiseProgress() - params.swayProgress() * 0.5f)) /** (1 - params.lowerProgress())*/;
        poseStack.translate(
                invert
                        * (
                        mainPoseInterpolation * mainPose.x
                                + params.raiseProgressEnd() * -0.05F
                                + params.swayProgress() * -0.1F
                                + params.swayScaleSlow() * 0.005F
                ),
                mainPoseInterpolation * mainPose.y + params.raiseProgressMiddle() * 0.075F + params.swayScaleFast() * 0.01F,
                mainPoseInterpolation * mainPose.z + params.raiseProgressStart() * 0.2 + params.raiseProgressEnd() * -0.2 + params.swayScaleSlow() * 0.005F
        );
        poseStack.rotateAround(
                Axis.XP
                        .rotationDegrees(
//                        -65.0F * Ease.inOutBack(params.raiseProgress())
                                -25.0F * params.lowerProgress()
                                        + -0.5F * params.swayScaleFast()
                        ),
                0.0F,
                0.1F,
                0.0F
        );
        float angle = 45;
        poseStack.rotateAround(
                Axis.ZN
                        .rotationDegrees(
                                invert * (-angle * Ease.inOutBack(params.raiseProgress()) + angle * params.swayProgress() * 0.25f + 2.0F * params.swayScaleSlow())
                        ),
                0,
                0.0F,
                0.0F
        );
        poseStack.translate(0.0F, -hitFeedbackAmount(ticksSinceKineticHitFeedback), 0.0F);
    }
}
