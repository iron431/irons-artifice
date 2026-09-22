package io.redspace.irons_artifice.menu;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.irons_artifice.IronsArtifice;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import static io.redspace.irons_artifice.menu.GunModifierMenu.SLOT_SIZE;

public class GunModifierScreen extends AbstractContainerScreen<GunModifierMenu> {
    private static final ResourceLocation BG_TEXTURE = IronsArtifice.id("textures/gui/gun_modifier_screen.png");
    private static final ResourceLocation SLOT_SPRITE = IronsArtifice.id("modifier_screen/slot");
    private static final float PREVIEW_SCALE = 3.0F;

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
        graphics.blit(BG_TEXTURE, xo, yo, 0, 0, this.imageWidth, this.imageHeight);
        this.renderGunPreview(graphics);
    }

    private void renderGunPreview(GuiGraphics graphics) {
        ItemStack gun = this.menu.gunstack;
        if (gun.isEmpty() || this.minecraft.player == null) {
            return;
        }

        float itemX = this.width / 2.0F;
        float itemY = this.topPos + 93 - 24 - 16.0F * PREVIEW_SCALE * 0.6F;
        float yRot = 15 + Mth.sin(this.minecraft.player.tickCount * ((float) Math.PI / 180) * 2) * 5;
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(itemX, itemY, 150.0F);
        pose.scale(PREVIEW_SCALE, PREVIEW_SCALE, PREVIEW_SCALE);
        pose.mulPose(Axis.YP.rotationDegrees(yRot));
        graphics.renderItem(gun, -8, -8);
        pose.popPose();
    }
}
