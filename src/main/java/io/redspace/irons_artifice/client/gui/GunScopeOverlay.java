package io.redspace.irons_artifice.client.gui;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculatePlayerTurnEvent;

@EventBusSubscriber(modid = IronsArtifice.MODID, value = Dist.CLIENT)
public final class GunScopeOverlay {
    private static final Identifier SCOPE_TEXTURE = Identifier.withDefaultNamespace("textures/misc/spyglass_scope.png");
    public static final float FOV_MODIFIER = 0.125F;

    private static float scopeScale = 0.5F;

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
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
        scopeScale = Mth.lerp(0.5F * deltaTracker.getGameTimeDeltaTicks(), scopeScale, 1.125F);
        blitScope(graphics, scopeScale);
    }

    @SubscribeEvent
    public static void onMouseInput(CalculatePlayerTurnEvent event){
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }
        var player = minecraft.player;
        if (player == null || !GunItem.isScoping(player)) {
            return;
        }
        double ss = event.getMouseSensitivity() * 0.6F + 0.2F;
        double sensitivityMod = ss * ss * ss;
        event.setMouseSensitivity(sensitivityMod / 8);
    }

    private static void blitScope(GuiGraphicsExtractor graphics, float scale) {
        float srcWidth = Math.min(graphics.guiWidth(), graphics.guiHeight());
        float ratio = Math.min(graphics.guiWidth() / srcWidth, graphics.guiHeight() / srcWidth) * scale;
        int width = Mth.floor(srcWidth * ratio);
        int height = Mth.floor(srcWidth * ratio);
        int left = (graphics.guiWidth() - width) / 2;
        int top = (graphics.guiHeight() - height) / 2;
        int right = left + width;
        int bottom = top + height;
        graphics.blit(RenderPipelines.GUI_TEXTURED, SCOPE_TEXTURE, left, top, 0.0F, 0.0F, width, height, width, height);
        graphics.fill(RenderPipelines.GUI, 0, bottom, graphics.guiWidth(), graphics.guiHeight(), -16777216);
        graphics.fill(RenderPipelines.GUI, 0, 0, graphics.guiWidth(), top, -16777216);
        graphics.fill(RenderPipelines.GUI, 0, top, left, bottom, -16777216);
        graphics.fill(RenderPipelines.GUI, right, top, graphics.guiWidth(), bottom, -16777216);
    }
}
