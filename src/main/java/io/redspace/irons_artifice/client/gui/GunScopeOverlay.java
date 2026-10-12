package io.redspace.irons_artifice.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;

public final class GunScopeOverlay {
    private static final ResourceLocation SCOPE_TEXTURE = new ResourceLocation("textures/misc/spyglass_scope.png");
    public static final float FOV_MODIFIER = 0.125F;

    private static float scopeScale = 0.5F;

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !minecraft.options.getCameraType().isFirstPerson()) {
            scopeScale = 0.5F;
            return;
        }
        if (!GunItem.isScoping(player)) {
            scopeScale = 0.5F;
            return;
        }
        scopeScale = Mth.lerp(0.5F * minecraft.getDeltaFrameTime(), scopeScale, 1.125F);
        blitScope(graphics, scopeScale);
    }

    /**
     * The mouse sensitivity {@link net.minecraft.client.MouseHandler#turnPlayer()} reads in place of the video setting.
     */
    public static double scopedSensitivity(double sensitivity) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.options.getCameraType().isFirstPerson()) {
            return sensitivity;
        }
        var player = minecraft.player;
        if (player == null || !GunItem.isScoping(player)) {
            return sensitivity;
        }
        double ss = sensitivity * 0.6F + 0.2F;
        double sensitivityMod = ss * ss * ss;
        return sensitivityMod / 8;
    }

    private static void blitScope(GuiGraphics graphics, float scale) {
        float srcWidth = Math.min(graphics.guiWidth(), graphics.guiHeight());
        float ratio = Math.min(graphics.guiWidth() / srcWidth, graphics.guiHeight() / srcWidth) * scale;
        int width = Mth.floor(srcWidth * ratio);
        int height = Mth.floor(srcWidth * ratio);
        int left = (graphics.guiWidth() - width) / 2;
        int top = (graphics.guiHeight() - height) / 2;
        int right = left + width;
        int bottom = top + height;
        RenderSystem.enableBlend();
        graphics.blit(SCOPE_TEXTURE, left, top, -90, 0.0F, 0.0F, width, height, width, height);
        RenderSystem.disableBlend();
        graphics.fill(RenderType.guiOverlay(), 0, bottom, graphics.guiWidth(), graphics.guiHeight(), -90, -16777216);
        graphics.fill(RenderType.guiOverlay(), 0, 0, graphics.guiWidth(), top, -90, -16777216);
        graphics.fill(RenderType.guiOverlay(), 0, top, left, bottom, -90, -16777216);
        graphics.fill(RenderType.guiOverlay(), right, top, graphics.guiWidth(), bottom, -90, -16777216);
    }
}
