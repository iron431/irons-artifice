package io.redspace.irons_artifice.client.gui;

import io.redspace.irons_artifice.item.BulletContainerContents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ClientBulletContainerItemTooltip implements ClientTooltipComponent {
    private static final int SLOT_BACKGROUND_COLOR = 0xFF8B8B8B;
    private static final int SLOT_HIGHLIGHT_BACK_COLOR = 0xFFC6C6C6;
    private static final int SLOT_BORDER_COLOR = 0xFF373737;
    private static final int SLOT_HIGHLIGHT_FRONT_COLOR = 0x80FFFFFF;
    private static final int SLOT_SIZE = 24;
    private static final int ITEM_INSET = 4;
    private static final int GRID_COLUMNS = 4;
    private static final int GRID_WIDTH = 96;
    private static final int BOTTOM_MARGIN = 4;
    private static final int LINE_HEIGHT = 9;
    private static final int DESCRIPTION_COLOR = 0xFFAAAAAA;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private final BulletContainerContents contents;

    public ClientBulletContainerItemTooltip(BulletContainerContents contents) {
        this.contents = contents;
    }

    @Override
    public int getHeight() {
        int body = contents.isEmpty() ? emptyDescriptionHeight(Minecraft.getInstance().font) : gridHeight();
        return body + BOTTOM_MARGIN;
    }

    @Override
    public int getWidth(Font font) {
        return GRID_WIDTH;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        if (contents.isEmpty()) {
            graphics.drawWordWrap(font, emptyDescription(), x, y, GRID_WIDTH, DESCRIPTION_COLOR);
            return;
        }
        renderGrid(font, graphics, x, y);
        renderSelectedName(font, graphics, x, y, getWidth(font));
    }

    private void renderGrid(Font font, GuiGraphics graphics, int left, int top) {
        List<ItemStack> shown = shownStacks();
        boolean overflowing = contents.size() > shown.size();
        int bottom = top + gridHeight();
        int slotNumber = 1;
        for (int row = 1; row <= gridRows(); row++) {
            for (int column = 1; column <= GRID_COLUMNS; column++) {
                int drawX = left + (column - 1) * SLOT_SIZE;
                int drawY = bottom - row * SLOT_SIZE;
                if (overflowing && column == 1 && row == 1) {
                    graphics.drawCenteredString(font, "+" + hiddenCount(shown), drawX + SLOT_SIZE / 2, drawY + 10, TEXT_COLOR);
                } else if (slotNumber <= shown.size()) {
                    int index = shown.size() - slotNumber;
                    renderSlot(font, graphics, shown.get(index), index, drawX, drawY);
                    slotNumber++;
                }
            }
        }
    }

    private void renderSlot(Font font, GuiGraphics graphics, ItemStack stack, int index, int drawX, int drawY) {
        boolean highlighted = index == contents.selectedIndex();
        graphics.fill(drawX, drawY, drawX + SLOT_SIZE, drawY + SLOT_SIZE, highlighted ? SLOT_HIGHLIGHT_BACK_COLOR : SLOT_BACKGROUND_COLOR);
        graphics.renderOutline(drawX, drawY, SLOT_SIZE, SLOT_SIZE, SLOT_BORDER_COLOR);
        graphics.renderItem(stack, drawX + ITEM_INSET, drawY + ITEM_INSET, index);
        graphics.renderItemDecorations(font, stack, drawX + ITEM_INSET, drawY + ITEM_INSET);
        if (highlighted) {
            graphics.fillGradient(RenderType.guiOverlay(), drawX, drawY, drawX + SLOT_SIZE, drawY + SLOT_SIZE, SLOT_HIGHLIGHT_FRONT_COLOR, SLOT_HIGHLIGHT_FRONT_COLOR, 0);
        }
    }

    private void renderSelectedName(Font font, GuiGraphics graphics, int x, int y, int w) {
        ItemStack selected = contents.selectedStack();
        if (selected == null) {
            return;
        }
        Component name = styledHoverName(selected);
        int textWidth = font.width(name.getVisualOrderText());
        int center = x + w / 2 - 12;
        graphics.renderTooltip(
                font,
                List.of(name.getVisualOrderText()),
                DefaultTooltipPositioner.INSTANCE,
                center - textWidth / 2,
                y - 15
        );
    }

    private static Component styledHoverName(ItemStack stack) {
        MutableComponent name = Component.empty().append(stack.getHoverName()).withStyle(stack.getRarity().color());
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            name.withStyle(ChatFormatting.ITALIC);
        }
        return name;
    }

    private List<ItemStack> shownStacks() {
        return contents.stacks().subList(0, Math.min(contents.size(), contents.numberOfStacksToShow()));
    }

    private int hiddenCount(List<ItemStack> shown) {
        return contents.stacks().stream().skip(shown.size()).mapToInt(ItemStack::getCount).sum();
    }

    private int gridRows() {
        return Mth.positiveCeilDiv(Math.min(12, contents.size()), GRID_COLUMNS);
    }

    private int gridHeight() {
        return gridRows() * SLOT_SIZE;
    }

    private Component emptyDescription() {
        return Component.translatable("irons_artifice.tooltip.bullet_container.description", contents.capacity());
    }

    private int emptyDescriptionHeight(Font font) {
        return font.split(emptyDescription(), GRID_WIDTH).size() * LINE_HEIGHT;
    }
}
