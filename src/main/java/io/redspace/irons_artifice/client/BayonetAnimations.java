package io.redspace.irons_artifice.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironslib.kinetic_weapon.KineticAnimation;
import io.redspace.ironslib.util.Ease;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.KineticWeapon;
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
        KineticWeapon kineticWeapon = itemStack.get(DataComponents.KINETIC_WEAPON);
        if (kineticWeapon == null) {
            return;
        }
        UseParams params = UseParams.fromKineticWeapon(kineticWeapon, timeHeld);
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

    public record UseParams(
            float raiseProgress,
            float raiseProgressStart,
            float raiseProgressMiddle,
            float raiseProgressEnd,
            float swayProgress,
            float lowerProgress,
            float raiseBackProgress,
            float swayIntensity,
            float swayScaleSlow,
            float swayScaleFast
    ) {
        public static UseParams fromKineticWeapon(KineticWeapon kineticWeapon, float time) {
            int finishRaisingTick = kineticWeapon.delayTicks();
            int finishSwayingTick = kineticWeapon.dismountConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0) + finishRaisingTick;
            int startSwayingTick = finishSwayingTick - 20;
            int finishLoweringTick = kineticWeapon.knockbackConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0) + finishRaisingTick;
            int startLoweringTick = finishLoweringTick - 40;
            int finishRaisingBackTick = kineticWeapon.damageConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0) + finishRaisingTick;
            float raiseProgress = progress(time, 0.0F, (float) finishRaisingTick);
            float raiseProgressStart = progress(raiseProgress, 0.0F, 0.5F);
            float raiseProgressMiddle = progress(raiseProgress, 0.5F, 0.8F);
            float raiseProgressEnd = progress(raiseProgress, 0.8F, 1.0F);
            float swayProgress = progress(time, (float) startSwayingTick, (float) finishSwayingTick);
            float lowerProgress = Ease.outCubic(Ease.inOutElastic(progress(time - 20.0F, (float) startLoweringTick, (float) finishLoweringTick)));
            float raiseBackProgress = progress(time, (float) (finishRaisingBackTick - 5), (float) finishRaisingBackTick);
            float swayIntensity = 2.0F * Ease.outCirc(swayProgress) - 2.0F * Ease.inCirc(raiseBackProgress);
            float swayScaleSlow = Mth.sin(time * 19.0F * Mth.DEG_TO_RAD) * swayIntensity;
            float swayScaleFast = Mth.sin(time * 30.0F * Mth.DEG_TO_RAD) * swayIntensity;
            return new UseParams(
                    raiseProgress,
                    raiseProgressStart,
                    raiseProgressMiddle,
                    raiseProgressEnd,
                    swayProgress,
                    lowerProgress,
                    raiseBackProgress,
                    swayIntensity,
                    swayScaleSlow,
                    swayScaleFast
            );
        }
    }
}
