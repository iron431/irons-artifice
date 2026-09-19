package io.redspace.irons_artifice.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.irons_artifice.item.kinetic.KineticWeapon;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class BayonetAnimations {
    private static float progress(float time, float start, float end) {
        return Mth.clamp(Mth.inverseLerp(time, start, end), 0.0F, 1.0F);
    }

    private static float hitFeedbackAmount(float ticksSinceFeedbackStart) {
        return 0.4F * (Ease.outQuart(progress(ticksSinceFeedbackStart, 1.0F, 3.0F)) - Ease.inOutSine(progress(ticksSinceFeedbackStart, 3.0F, 10.0F)));
    }

    public static void firstPersonUse(float ticksSinceKineticHitFeedback, PoseStack poseStack, float timeHeld, HumanoidArm arm, ItemStack itemStack) {
        KineticWeapon kineticWeapon = KineticWeapon.get(itemStack);
        if (kineticWeapon != null) {
            BayonetAnimations.UseParams params = BayonetAnimations.UseParams.fromKineticWeapon(kineticWeapon, timeHeld);
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

    record UseParams(
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
        public static BayonetAnimations.UseParams fromKineticWeapon(KineticWeapon kineticWeapon, float time) {
            int finishRaisingTick = kineticWeapon.delayTicks();
            int finishSwayingTick = kineticWeapon.dismountConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0) + finishRaisingTick;
            int startSwayingTick = finishSwayingTick - 20;
            int finishLoweringTick = kineticWeapon.knockbackConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0) + finishRaisingTick;
            int startLoweringTick = finishLoweringTick - 40;
            int finishRaisingBackTick = kineticWeapon.damageConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0) + finishRaisingTick;
            float raiseProgress = BayonetAnimations.progress(time, 0.0F, (float) finishRaisingTick);
            float raiseProgressStart = BayonetAnimations.progress(raiseProgress, 0.0F, 0.5F);
            float raiseProgressMiddle = BayonetAnimations.progress(raiseProgress, 0.5F, 0.8F);
            float raiseProgressEnd = BayonetAnimations.progress(raiseProgress, 0.8F, 1.0F);
            float swayProgress = BayonetAnimations.progress(time, (float) startSwayingTick, (float) finishSwayingTick);
            float lowerProgress = Ease.outCubic(Ease.inOutElastic(BayonetAnimations.progress(time - 20.0F, (float) startLoweringTick, (float) finishLoweringTick)));
            float raiseBackProgress = BayonetAnimations.progress(time, (float) (finishRaisingBackTick - 5), (float) finishRaisingBackTick);
            float swayIntensity = 2.0F * Ease.outCirc(swayProgress);
            float swayScaleSlow = Mth.sin(time * 19.0F * (float) (Math.PI / 180.0)) * swayIntensity;
            float swayScaleFast = Mth.sin(time * 30.0F * (float) (Math.PI / 180.0)) * swayIntensity;
            return new BayonetAnimations.UseParams(
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

    static class Ease {
        public static float outQuart(float x) {
            return 1.0F - Mth.square(Mth.square(1.0F - x));
        }

        public static float outCubic(float x) {
            return 1.0F - Mth.square(1.0F - x) * (1.0F - x);
        }

        public static float outCirc(float x) {
            return (float) Math.sqrt(1.0F - Mth.square(x - 1.0F));
        }

        public static float inOutSine(float x) {
            return -(Mth.cos(3.1415927F * x) - 1.0F) / 2.0F;
        }

        public static float inOutBack(float x) {
            if (x < 0.5F) {
                return 4.0F * x * x * (7.189819F * x - 2.5949094F) / 2.0F;
            } else {
                float dt = 2.0F * x - 2.0F;
                return (dt * dt * (3.5949094F * dt + 2.5949094F) + 2.0F) / 2.0F;
            }
        }

        public static float inOutElastic(float x) {
            if (x == 0.0F) {
                return 0.0F;
            } else if (x == 1.0F) {
                return 1.0F;
            } else {
                double sin = Math.sin((20.0 * x - 11.125) * 1.3962634801864624);
                return x < 0.5F ? (float) (-(Math.pow(2.0, 20.0 * x - 10.0) * sin) / 2.0) : (float) (Math.pow(2.0, -20.0 * x + 10.0) * sin / 2.0 + 1.0);
            }
        }
    }
}
