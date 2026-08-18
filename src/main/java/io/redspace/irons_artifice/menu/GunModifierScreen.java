package io.redspace.irons_artifice.menu;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.gui.GunPreviewRenderState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import static io.redspace.irons_artifice.menu.GunModifierMenu.SLOT_SIZE;

public class GunModifierScreen extends AbstractContainerScreen<GunModifierMenu> {
    private static final Identifier BG_TEXTURE = IronsArtifice.id("textures/gui/gun_modifier_screen.png");
    private static final Identifier SLOT_SPRITE = IronsArtifice.id("modifier_screen/slot");
    private static final float PREVIEW_SCALE = 16.0F * 3.0F;

    public GunModifierScreen(GunModifierMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, menu.gunstack.getHoverName().copy().setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withUnderlined(true)), 176, 183);

    }

    @Override
    protected void init() {
        super.init();
        int margin = (SLOT_SIZE - 16) / 2;
        for (var slot : menu.getModifierSlots()) {
            this.addRenderableOnly((graphics, mx, my, a) ->
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, leftPos + slot.x - margin, topPos + slot.y - margin, SLOT_SIZE, SLOT_SIZE)
            );
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BG_TEXTURE, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        this.renderGunPreview(graphics);
    }

    private void renderGunPreview(GuiGraphicsExtractor graphics) {
        ItemStack gun = this.menu.gunstack;
        if (gun.isEmpty()) {
            return;
        }

        TrackingItemStackRenderState itemState = new TrackingItemStackRenderState();
        this.minecraft.getItemModelResolver().updateForTopItem(
                itemState, gun, ItemDisplayContext.FIXED, this.minecraft.level, this.minecraft.player, 0);

        ScreenRectangle scissor = new ScreenRectangle(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        float itemX = this.width / 2.0F;
        float itemY = this.topPos + 93 - 24 - PREVIEW_SCALE * .6f;
        float yRot = 15 + Mth.sin(Minecraft.getInstance().player.tickCount * Mth.DEG_TO_RAD * 2) * 5;
        graphics.submitPictureInPictureRenderState(new GunPreviewRenderState(
                itemState,
                itemX,
                itemY,
                yRot,
                scissor.left(),
                scissor.top(),
                scissor.right(),
                scissor.bottom(),
                PREVIEW_SCALE,
                scissor
        ));
    }
}
