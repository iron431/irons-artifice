package io.redspace.irons_artifice.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.RecoilManager;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.ReloadState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = IronsArtifice.MODID, value = Dist.CLIENT)
public final class CrosshairRenderer {
    private static final int COLOR = 0xFFFFFFFF;
    private static final int THICKNESS = 1;
    private static final int LENGTH = 3;
    private static final float GAP_BASE = 0.0F;

    // 1.21.1 has no invert-blend GUI pipeline, so the crosshair uses a dedicated
    // RenderType with the classic ONE_MINUS_DST_COLOR / ONE_MINUS_SRC_COLOR blend
    // (the same invert effect vanilla crosshairs historically used).
    private static final RenderType GUI_INVERT = RenderType.create(
            "irons_artifice:gui_invert",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(new RenderStateShard.TransparencyStateShard(
                            "invert_transparency",
                            () -> {
                                RenderSystem.enableBlend();
                                RenderSystem.blendFuncSeparate(
                                        GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                                        GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                                        GlStateManager.SourceFactor.ZERO,
                                        GlStateManager.DestFactor.ONE);
                            },
                            () -> {
                                RenderSystem.disableBlend();
                                RenderSystem.defaultBlendFunc();
                            }))
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(RenderType.OutlineProperty.NONE));

    private static float crosshairGapCursor = 0.0F;
    private static float crosshairGapCursorO = 0.0F;
    private static int reloadAnimationDuration;
    private static int reloadAnimationTick;

    public static boolean renderGunCrosshair(GuiGraphics graphics, DeltaTracker deltaTracker) {
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
        float gap = Math.max(GAP_BASE + degreesToGuiPixels(degreesSpread, graphics.guiHeight(), minecraft.gameRenderer.getFov(minecraft.gameRenderer.getMainCamera(), partialTick, true)), 0);
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(graphics.guiWidth() / 2 - 1, graphics.guiHeight() / 2, 0);
        if (reloadAnimationTick > 0) {
            poseStack.translate(0.5f, 0.5f, 0);
            float f = (reloadAnimationTick - partialTick) / reloadAnimationDuration;

//            float remaining = 1f - Mth.lerp(partialTick, reloadProgressO, reloadProgress);
            f = crosshairAnimationInterpolation(f);
            poseStack.mulPose(Axis.ZP.rotation(f * (float) Math.PI));
            poseStack.translate(-0.5f, -0.5f, 0);
        }
        drawCross(graphics, gap);
        if (GunItem.isScoping(player)) {
            drawScopeCrosshair(graphics, gap);
        }
        poseStack.popPose();

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
        float halfFovRad = fovDegrees * ((float) Math.PI / 180) * 0.5f;
        float spreadRad = degreesSpread * ((float) Math.PI / 180);
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

    private static void drawScopeCrosshair(GuiGraphics graphics, float gap) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(-gap, 0.25f, 0);
        poseStack.scale(0.5f, 0.5f, 1.0f);
        poseStack.scale((gap) * 4 + 2, 1f, 1.0f);
        graphics.fill(GUI_INVERT, 0, 0, 1, THICKNESS, COLOR);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.25f, -gap, 0);
        poseStack.scale(0.5f, 0.5f, 1.0f);
        poseStack.scale(1f, (gap) * 4 + 2, 1.0f);
        graphics.fill(GUI_INVERT, 0, 0, THICKNESS, 1, COLOR);
        poseStack.popPose();
    }

    private static void drawCross(GuiGraphics graphics, float gap) {
        PoseStack poseStack = graphics.pose();
        int length = LENGTH + (int) (gap / 40);
        // left prong;
        poseStack.pushPose();
        poseStack.translate(-gap - length, 0, 0);
        graphics.fill(GUI_INVERT, 0, 0, length, THICKNESS, COLOR);
        poseStack.popPose();
        // right prong
        poseStack.pushPose();
        poseStack.translate(1 + gap, 0, 0);
        graphics.fill(GUI_INVERT, 0, 0, length, THICKNESS, COLOR);
        poseStack.popPose();
        // top prong;
        poseStack.pushPose();
        poseStack.translate(0, -gap - length, 0);
        graphics.fill(GUI_INVERT, 0, 0, THICKNESS, length, COLOR);
        poseStack.popPose();
        // down prong
        poseStack.pushPose();
        poseStack.translate(0, 1 + gap, 0);
        graphics.fill(GUI_INVERT, 0, 0, THICKNESS, length, COLOR);
        poseStack.popPose();

    }
}
