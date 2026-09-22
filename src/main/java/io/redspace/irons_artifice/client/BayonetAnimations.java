package io.redspace.irons_artifice.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class BayonetAnimations {
    // Fixed charge timings (ticks), matching the old vanilla spear parameters
    private static final int FINISH_RAISING_TICK = 10;
    private static final int START_SWAYING_TICK = 40;
    private static final int FINISH_SWAYING_TICK = 60;
    private static final int START_LOWERING_TICK = 105;
    private static final int FINISH_LOWERING_TICK = 145;
    private static final int FINISH_RAISING_BACK_TICK = 235;

    private static float progress(float time, float start, float end) {
        return Mth.clamp(Mth.inverseLerp(time, start, end), 0.0F, 1.0F);
    }

    private static float outQuart(float t) {
        return 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t) * (1.0F - t);
    }

    private static float outCubic(float t) {
        return 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
    }

    private static float inOutSine(float t) {
        return -(Mth.cos(t * Mth.PI) - 1.0F) / 2.0F;
    }

    private static float outCirc(float t) {
        return Mth.sqrt(1.0F - (t - 1.0F) * (t - 1.0F));
    }

    private static float inOutBack(float t) {
        float c1 = 1.70158F;
        float c2 = c1 * 1.525F;
        if (t < 0.5F) {
            return ((2.0F * t) * (2.0F * t) * ((c2 + 1.0F) * 2.0F * t - c2)) / 2.0F;
        } else {
            return (((2.0F * t - 2.0F) * (2.0F * t - 2.0F) * ((c2 + 1.0F) * (2.0F * t - 2.0F) + c2)) + 2.0F) / 2.0F;
        }
    }

    private static float inOutElastic(float t) {
        if (t <= 0.0F) {
            return 0.0F;
        }
        if (t >= 1.0F) {
            return 1.0F;
        }
        float c = ((float) Math.PI * 2.0F) / 4.5F;
        if (t < 0.5F) {
            return -(float) (Math.pow(2.0, 20.0 * t - 10.0) * Math.sin((20.0 * t - 11.125) * c)) / 2.0F;
        } else {
            return (float) (Math.pow(2.0, -20.0 * t + 10.0) * Math.sin((20.0 * t - 11.125) * c)) / 2.0F;
        }
    }

    private static float hitFeedbackAmount(float ticksSinceFeedbackStart) {
        return 0.4F * (outQuart(progress(ticksSinceFeedbackStart, 1.0F, 3.0F)) - inOutSine(progress(ticksSinceFeedbackStart, 3.0F, 10.0F)));
    }

    public static void firstPersonUse(float ticksSinceHitFeedback, PoseStack poseStack, float timeHeld, HumanoidArm arm, ItemStack itemStack) {
        if (!itemStack.has(DataComponentRegistry.BAYONET.get())) {
            return;
        }
        BayonetAnimations.UseParams params = BayonetAnimations.UseParams.forTimeHeld(timeHeld);
        int invert = arm == HumanoidArm.RIGHT ? 1 : -1;
        Vec3 mainPose = new Vec3(-0.5, -0.075, -0.75);
        double mainPoseInterpolation = inOutBack((params.raiseProgress() - params.swayProgress() * 0.5f)) /** (1 - params.lowerProgress())*/;
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
//                        -65.0F * inOutBack(params.raiseProgress())
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
                                invert * (-angle * inOutBack(params.raiseProgress()) + angle * params.swayProgress() * 0.25f + 2.0F * params.swayScaleSlow())
                        ),
                0,
                0.0F,
                0.0F
        );
        poseStack.translate(0.0F, -hitFeedbackAmount(ticksSinceHitFeedback), 0.0F);
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
        public static BayonetAnimations.UseParams forTimeHeld(float time) {
            float raiseProgress = BayonetAnimations.progress(time, 0.0F, (float) FINISH_RAISING_TICK);
            float raiseProgressStart = BayonetAnimations.progress(raiseProgress, 0.0F, 0.5F);
            float raiseProgressMiddle = BayonetAnimations.progress(raiseProgress, 0.5F, 0.8F);
            float raiseProgressEnd = BayonetAnimations.progress(raiseProgress, 0.8F, 1.0F);
            float swayProgress = BayonetAnimations.progress(time, (float) START_SWAYING_TICK, (float) FINISH_SWAYING_TICK);
            float lowerProgress = outCubic(inOutElastic(BayonetAnimations.progress(time - 20.0F, (float) START_LOWERING_TICK, (float) FINISH_LOWERING_TICK)));
            float raiseBackProgress = BayonetAnimations.progress(time, (float) (FINISH_RAISING_BACK_TICK - 5), (float) FINISH_RAISING_BACK_TICK);
            float swayIntensity = 2.0F * outCirc(swayProgress);
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
}
