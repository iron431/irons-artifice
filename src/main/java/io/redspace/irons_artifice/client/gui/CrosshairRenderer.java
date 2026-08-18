package io.redspace.irons_artifice.client.gui;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.RecoilManager;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.ReloadState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Matrix3x2fStack;

@EventBusSubscriber(modid = IronsArtifice.MODID, value = Dist.CLIENT)
public final class CrosshairRenderer {
    private static final int COLOR = 0xFFFFFFFF;
    private static final int THICKNESS = 1;
    private static final int LENGTH = 3;
    private static final float GAP_BASE = 0.0F;

    private static float crosshairGapCursor = 0.0F;
    private static float crosshairGapCursorO = 0.0F;
    private static int reloadAnimationDuration;
    private static int reloadAnimationTick;

    public static boolean renderGunCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.options.getCameraType().isFirstPerson()) {
            return false;
        }
        LocalPlayer player = minecraft.player;
        if (player == null || player.isSpectator()) {
            return false;
        }
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof GunItem gunItem)) {
            return false;
        }

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        float degreesSpread = localCrosshairGap(partialTick);
        float gap = Math.max(GAP_BASE + degreesToGuiPixels(degreesSpread, graphics.guiHeight(), minecraft.gameRenderer.getMainCamera().getFov()), 0);
        graphics.nextStratum();
        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(graphics.guiWidth() / 2 - 1, graphics.guiHeight() / 2);
        if (reloadAnimationTick > 0) {
            poseStack.translate(0.5f, 0.5f);
            float f = (reloadAnimationTick - partialTick) / reloadAnimationDuration;

//            float remaining = 1f - Mth.lerp(partialTick, reloadProgressO, reloadProgress);
            f = crosshairAnimationInterpolation(f);
            poseStack.rotate(f * 180 * Mth.DEG_TO_RAD);
            poseStack.translate(-0.5f, -0.5f);
        }
        drawCross(graphics, gap);
        poseStack.popMatrix();

        return true;
    }

    private static float crosshairAnimationInterpolation(float remaining) {
        float percent = 1 - remaining;
        return 1 - (percent * percent * percent * percent * percent);
    }

    private static float degreesToGuiPixels(float degreesSpread, int guiHeight, float fovDegrees) {
        if (degreesSpread <= 0 || guiHeight <= 0 || fovDegrees <= 0) {
            return 0;
        }
        float halfFovRad = fovDegrees * Mth.DEG_TO_RAD * 0.5f;
        float spreadRad = degreesSpread * Mth.DEG_TO_RAD;
        float denom = (float) Math.tan(halfFovRad);
        if (denom <= 1.0E-6f) {
            return 0;
        }
        return (guiHeight * 0.5f) * (float) Math.tan(spreadRad) / denom;
    }

    private static void updateReloadProgress() {
        if (reloadAnimationTick > 0) {
            reloadAnimationTick--;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        ReloadState reloadState = ReloadState.get(held);
        if (reloadState == null) {
            if (reloadAnimationTick > 2 || reloadAnimationTick == 0) {
                reloadAnimationDuration = 0;
                reloadAnimationTick = 0;
            }
            return;
        }
        if (reloadAnimationDuration == 0) {
            reloadAnimationDuration = reloadState.durationTicks();
            reloadAnimationTick = reloadAnimationDuration;
        }
    }

    private static void updateCrosshairCursor() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof GunItem gunItem)) {
            return;
        }
        ShotProfile profile = GunplayManager.compose(player, gunItem.getGun(), held);
        float spread = GunplayManager.getSpreadForEntity(profile, player);
        float recoilMagnitude = RecoilManager.localRecoilMagnitude();

        float targetDegrees = recoilMagnitude * 0.5f + spread;
        crosshairGapCursor = Mth.lerp(0.25f, crosshairGapCursor, targetDegrees);
        if (Math.abs(crosshairGapCursor) < 0.01) {
            crosshairGapCursor = 0;
        }
    }

    public static float localCrosshairGap(float partialTick) {
        return Mth.lerp(partialTick, crosshairGapCursorO, crosshairGapCursor);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().isPaused()) {
            return;
        }
        crosshairGapCursorO = crosshairGapCursor;
        updateCrosshairCursor();
        updateReloadProgress();
    }

    private static void drawCross(GuiGraphicsExtractor graphics, float gap) {
        Matrix3x2fStack poseStack = graphics.pose();
        int length = LENGTH + (int) (gap / 40);
        // left prong;
        poseStack.pushMatrix();
        poseStack.translate(-gap - length, 0);
        graphics.fill(RenderPipelines.GUI_INVERT, 0, 0, length, THICKNESS, COLOR);
        poseStack.popMatrix();
        // right prong
        poseStack.pushMatrix();
        poseStack.translate(1 + gap, 0);
        graphics.fill(RenderPipelines.GUI_INVERT, 0, 0, length, THICKNESS, COLOR);
        poseStack.popMatrix();
        // top prong;
        poseStack.pushMatrix();
        poseStack.translate(0, -gap - length);
        graphics.fill(RenderPipelines.GUI_INVERT, 0, 0, THICKNESS, length, COLOR);
        poseStack.popMatrix();
        // down prong
        poseStack.pushMatrix();
        poseStack.translate(0, 1 + gap);
        graphics.fill(RenderPipelines.GUI_INVERT, 0, 0, THICKNESS, length, COLOR);
        poseStack.popMatrix();

    }
}
