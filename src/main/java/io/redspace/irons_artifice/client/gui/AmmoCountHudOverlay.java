package io.redspace.irons_artifice.client.gui;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.config.ClientConfig;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.MagazineContents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Matrix3x2fStack;

@EventBusSubscriber(modid = IronsArtifice.MODID, value = Dist.CLIENT)
public final class AmmoCountHudOverlay {
    private static final Identifier BULLET_ICON = IronsArtifice.id("textures/gui/bullet_icon.png");

    private static int previousAmmoCount = -1;
    private static int flashTicksRemaining;
    private static int flashDuration;

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!ClientConfig.ENABLED.get()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui) {
            return;
        }
        LocalPlayer player = minecraft.player;
        if (player == null || player.isSpectator()) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof GunItem gunItem)) {
            return;
        }

        Font font = minecraft.font;
        MagazineContents magazine = GunItem.getMagazine(held);
        int loaded = magazine.count();
        int capacity = gunItem.magazineCapacity();
        // todo: is this expensive to do every render frame?
        int reserve = GunplayManager.countBullets(player);

        float loadedScale = ClientConfig.LOADED_SCALE.get().floatValue();
        float capacityScale = loadedScale * 0.75f;
        float reserveScale = 1f;
        boolean showIcon = ClientConfig.SHOW_ICON.get();
        boolean showMagazine = ClientConfig.SHOW_MAGAZINE.get();
        boolean showReserve = ClientConfig.SHOW_RESERVE.get();
        boolean shadow = ClientConfig.TEXT_SHADOW.get();
        int iconSize = ClientConfig.ICON_SIZE.get();
        ClientConfig.Anchor anchor = ClientConfig.ANCHOR.get();

        String loadedText = Integer.toString(loaded);
        String capacityText = "/" + capacity;
        String reserveText = Integer.toString(reserve);

        float loadedWidth = font.width(loadedText) * loadedScale;
        float capacityWidth = showMagazine ? font.width(capacityText) * capacityScale : 0;
        float magazineWidth = loadedWidth + capacityWidth;
        float reserveWidth = showReserve ? font.width(reserveText) * reserveScale : 0;
        float textColumnWidth = Math.max(magazineWidth, reserveWidth);

        float iconBlock = showIcon ? iconSize + 4 : 0;
        float totalWidth = iconBlock + textColumnWidth;
        float magazineHeight = font.lineHeight * loadedScale;
        float reserveHeight = showReserve ? font.lineHeight * reserveScale : 0;
        float gapBetweenRows = showReserve ? 2.0F : 0.0F;
        float totalHeight = Math.max(showIcon ? iconSize : 0, magazineHeight + gapBetweenRows + reserveHeight);

        Vec2 origin = resolveOrigin(graphics.guiWidth(), graphics.guiHeight(), totalWidth, totalHeight);
        float left = origin.x;
        float top = origin.y;

        int magColor = magazineTextColor(loaded, capacity);
        int reserveColor = ClientConfig.colorReserve();
        int iconColor = ClientConfig.colorIcon();

        graphics.nextStratum();

        float textLeft = left + iconBlock;
        float magazineTop = top + Math.max(0, (totalHeight - (magazineHeight + gapBetweenRows + reserveHeight)) * 0.5F);
        if (showIcon) {
            float iconY = top + (totalHeight - iconSize) * 0.5F;
            graphics.blit(RenderPipelines.GUI_TEXTURED, BULLET_ICON, Math.round(left), Math.round(iconY), 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize, iconColor);
        }

        float magazineBaselineY = magazineTop;
        float loadedLeft = textLeft;
        float magazineLeft = textLeft + (textColumnWidth - magazineWidth);
        loadedLeft = magazineLeft;
        drawScaledText(graphics, font, loadedText, magazineLeft, magazineBaselineY, loadedScale, magColor, shadow);

        if (showMagazine) {
            float capacityY = magazineBaselineY + (font.lineHeight * loadedScale - font.lineHeight * capacityScale);
            drawScaledText(graphics, font, capacityText, magazineLeft + loadedWidth, capacityY, capacityScale, magColor, shadow);
        }

        if (showReserve) {
            float reserveTop = magazineTop + magazineHeight + gapBetweenRows;
            float reserveLeft = textLeft;
            if (anchor.isRight()) {
                reserveLeft += (textColumnWidth - reserveWidth);
            } else if (anchor.isCenter()) {
                reserveLeft += (textColumnWidth - reserveWidth) / 2;
            }
            drawScaledText(graphics, font, reserveText, reserveLeft, reserveTop, reserveScale, reserveColor, shadow);
        }

        if (flashTicksRemaining > 0 && ClientConfig.FLASH_ENABLED.get()) {
            float flashOriginX = left + totalWidth / 2;
            float flashOriginY = magazineBaselineY;
            float flashBaseScale = loadedScale;
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
            float endScale = ClientConfig.FLASH_END_SCALE.get().floatValue();
            float travel = ClientConfig.FLASH_TRAVEL.get().floatValue();
            String text = "0";
            renderFlash(graphics, text, font, partialTick, shadow, flashOriginX, flashOriginY, flashBaseScale, travel * 0.5f, endScale * 0.33f);
            renderFlash(graphics, text, font, partialTick, shadow, flashOriginX, flashOriginY, flashBaseScale, travel * 0.75f, endScale * 0.66f);
            renderFlash(graphics, text, font, partialTick, shadow, flashOriginX, flashOriginY, flashBaseScale, travel, endScale);
        }
    }

    private static void renderFlash(GuiGraphicsExtractor graphics, String flashText, Font font, float partialTick, boolean shadow, float startX, float startY, float baseScale, float travel, float endScale) {
        float remaining = flashTicksRemaining - partialTick;
        float progress = 1.0F - Mth.clamp(remaining / Math.max(1, flashDuration), 0.0F, 1.0F);
        float eased = 1.0F - (1.0F - progress) * (1.0F - progress) * (1.0F - progress) * (1.0F - progress);

        float scale = baseScale * Mth.lerp(eased, 1.0F, endScale);
        float alpha = 1.0F - eased;
        if (alpha <= 0.01F) {
            return;
        }

        int color = ARGB.color(alpha, ClientConfig.colorFlash());

        // interpolate towards the center of the screen
        float textWidth = font.width(flashText) * scale;
        float textHeight = font.lineHeight * scale;
        float targetX = (graphics.guiWidth()) * 0.5F;
        float targetY = (graphics.guiHeight()) * 0.5F;
        float amount = eased * travel;
        float x = Mth.lerp(amount, startX, targetX) - textWidth * 0.5f;
        float y = Mth.lerp(amount, startY, targetY) - textHeight * 0.5f;
        drawScaledText(graphics, font, flashText, x, y, scale, color, shadow);
    }

    private static Vec2 resolveOrigin(int guiWidth, int guiHeight, float width, float height) {
        int ox = ClientConfig.OFFSET_X.get();
        int oy = ClientConfig.OFFSET_Y.get();
        return switch (ClientConfig.ANCHOR.get()) {
            case BOTTOM_RIGHT -> new Vec2(guiWidth - ox - width, guiHeight - oy - height);
            case BOTTOM_LEFT -> new Vec2(ox, guiHeight - oy - height);
            case TOP_RIGHT -> new Vec2(guiWidth - ox - width, oy);
            case TOP_LEFT -> new Vec2(ox, oy);
            case HOTBAR -> new Vec2(ox + (guiWidth) / 2 - width,
                    guiHeight - oy - height - Math.max(Minecraft.getInstance().gui.leftHeight, Minecraft.getInstance().gui.rightHeight));
        };
    }

    static int magazineTextColor(int loaded, int capacity) {
        if (loaded <= 0) {
            return ClientConfig.colorEmpty();
        }
        int full = ClientConfig.colorFull();
        // Full magazine is always white — takes precedence over the gold "1 ammo" endpoint (e.g. 1/1).
        if (loaded >= capacity) {
            return full;
        }
        float t = Mth.clamp((loaded - 1) / (float) (capacity - 1), 0.0F, 1.0F);
        return ARGB.srgbLerp(t, ClientConfig.colorLow(), full);
    }

    private static void drawScaledText(GuiGraphicsExtractor graphics, Font font, String text, float x, float y, float scale, int color, boolean shadow) {
        if (ARGB.alpha(color) == 0) {
            return;
        }
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(scale, scale);
        graphics.text(font, text, 0, 0, color, shadow);
        pose.popMatrix();
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            previousAmmoCount = -1;
            flashTicksRemaining = 0;
            return;
        }

        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof GunItem) {
            int loaded = GunItem.getMagazine(held).count();
            if (previousAmmoCount > 0 && loaded == 0 && ClientConfig.FLASH_ENABLED.get()) {
                flashDuration = ClientConfig.FLASH_DURATION_TICKS.get();
                flashTicksRemaining = flashDuration;
            }
            previousAmmoCount = loaded;
        } else {
            previousAmmoCount = -1;
        }

        if (flashTicksRemaining > 0) {
            flashTicksRemaining--;
        }
    }
}
