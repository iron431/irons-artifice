package io.redspace.irons_artifice.menu;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.irons_artifice.IronsArtifice;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import static io.redspace.irons_artifice.menu.GunModifierMenu.SLOT_SIZE;

public class GunModifierScreen extends AbstractContainerScreen<GunModifierMenu> {
    private static final ResourceLocation BG_TEXTURE = IronsArtifice.id("textures/gui/gun_modifier_screen.png");
    private static final ResourceLocation SLOT_SPRITE = IronsArtifice.id("modifier_screen/slot");
    private static final float PREVIEW_SCALE = 16.0F * 3.0F;

    public GunModifierScreen(GunModifierMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, menu.gunstack.getHoverName().copy().setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withUnderlined(true)));
        this.imageWidth = 176;
        this.imageHeight = 183;
    }

    @Override
    protected void init() {
        super.init();
        int margin = (SLOT_SIZE - 16) / 2;
        for (var slot : menu.getModifierSlots()) {
            this.addRenderableOnly((graphics, mx, my, a) ->
                    graphics.blitSprite(SLOT_SPRITE, leftPos + slot.x - margin, topPos + slot.y - margin, SLOT_SIZE, SLOT_SIZE)
            );
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        graphics.blit(BG_TEXTURE, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        this.renderGunPreview(graphics);
    }

    private void renderGunPreview(GuiGraphics graphics) {
        ItemStack gun = this.menu.gunstack;
        if (gun.isEmpty()) {
            return;
        }

        ItemRenderer itemRenderer = this.minecraft.getItemRenderer();
        BakedModel model = itemRenderer.getModel(gun, this.minecraft.level, this.minecraft.player, 0);

        float itemX = this.width / 2.0F;
        float itemY = this.topPos + 93 - 24 - PREVIEW_SCALE * .6f;
        float yRot = 15 + Mth.sin(Minecraft.getInstance().player.tickCount * Mth.DEG_TO_RAD * 2) * 5;

        graphics.enableScissor(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight);
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(itemX, itemY, 150.0F);
        poseStack.scale(PREVIEW_SCALE, -PREVIEW_SCALE, PREVIEW_SCALE);
        poseStack.mulPose(Axis.XP.rotationDegrees(15));
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        boolean flatLight = !model.usesBlockLight();
        if (flatLight) {
            Lighting.setupForFlatItems();
        }
        itemRenderer.render(gun, ItemDisplayContext.FIXED, false, poseStack, graphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, model);
        graphics.flush();
        if (flatLight) {
            Lighting.setupFor3DItems();
        }
        poseStack.popPose();
        graphics.disableScissor();
    }
}
