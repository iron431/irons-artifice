package io.redspace.irons_artifice.client.gui;

import io.redspace.irons_artifice.item.BulletPouchContents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.List;

public class ClientBulletPouchTooltip implements ClientTooltipComponent {
    private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_highlight_back");
    private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_highlight_front");
    private static final Identifier SLOT_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("container/bundle/slot_background");
    private static final int SLOT_SIZE = 24;
    private static final int ITEM_INSET = 4;
    private static final int GRID_COLUMNS = 4;
    private static final int GRID_WIDTH = 96;
    private static final int BOTTOM_MARGIN = 4;
    private static final int LINE_HEIGHT = 9;
    private static final int DESCRIPTION_COLOR = 0xFFAAAAAA;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private final BulletPouchContents contents;

    public ClientBulletPouchTooltip(BulletPouchContents contents) {
        this.contents = contents;
    }

    @Override
    public int getHeight(Font font) {
        int body = contents.isEmpty() ? emptyDescriptionHeight(font) : gridHeight();
        return body + BOTTOM_MARGIN;
    }

    @Override
    public int getWidth(Font font) {
        return GRID_WIDTH;
    }

    @Override
    public boolean showTooltipWithItemInHand() {
        return true;
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        if (contents.isEmpty()) {
            graphics.textWithWordWrap(font, emptyDescription(), x, y, GRID_WIDTH, DESCRIPTION_COLOR);
            return;
        }
        extractGrid(font, graphics, x, y);
        extractSelectedName(font, graphics, x, y, w);
    }

    private void extractGrid(Font font, GuiGraphicsExtractor graphics, int left, int top) {
        List<ItemStackTemplate> shown = shownStacks();
        boolean overflowing = contents.size() > shown.size();
        int bottom = top + gridHeight();
        int slotNumber = 1;
        for (int row = 1; row <= gridRows(); row++) {
            for (int column = 1; column <= GRID_COLUMNS; column++) {
                int drawX = left + (column - 1) * SLOT_SIZE;
                int drawY = bottom - row * SLOT_SIZE;
                if (overflowing && column == 1 && row == 1) {
                    graphics.centeredText(font, "+" + hiddenCount(shown), drawX + SLOT_SIZE / 2, drawY + 10, TEXT_COLOR);
                } else if (slotNumber <= shown.size()) {
                    int index = shown.size() - slotNumber;
                    extractSlot(font, graphics, shown.get(index), index, drawX, drawY);
                    slotNumber++;
                }
            }
        }
    }

    private void extractSlot(Font font, GuiGraphicsExtractor graphics, ItemStackTemplate template, int index, int drawX, int drawY) {
        boolean highlighted = index == contents.selectedIndex();
        ItemStack stack = template.create();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, highlighted ? SLOT_HIGHLIGHT_BACK_SPRITE : SLOT_BACKGROUND_SPRITE, drawX, drawY, SLOT_SIZE, SLOT_SIZE);
        graphics.item(stack, drawX + ITEM_INSET, drawY + ITEM_INSET, index);
        graphics.itemDecorations(font, stack, drawX + ITEM_INSET, drawY + ITEM_INSET);
        if (highlighted) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, drawX, drawY, SLOT_SIZE, SLOT_SIZE);
        }
    }

    private void extractSelectedName(Font font, GuiGraphicsExtractor graphics, int x, int y, int w) {
        ItemStackTemplate selected = contents.selectedStack();
        if (selected == null) {
            return;
        }
        ItemStack stack = selected.create();
        Component name = stack.getStyledHoverName();
        int textWidth = font.width(name.getVisualOrderText());
        int center = x + w / 2 - 12;
        graphics.tooltip(
                font,
                List.of(ClientTooltipComponent.create(name.getVisualOrderText())),
                center - textWidth / 2,
                y - 15,
                DefaultTooltipPositioner.INSTANCE,
                stack.get(DataComponents.TOOLTIP_STYLE)
        );
    }

    private List<ItemStackTemplate> shownStacks() {
        return contents.stacks().subList(0, Math.min(contents.size(), contents.numberOfStacksToShow()));
    }

    private int hiddenCount(List<ItemStackTemplate> shown) {
        return contents.stacks().stream().skip(shown.size()).mapToInt(ItemStackTemplate::count).sum();
    }

    private int gridRows() {
        return Mth.positiveCeilDiv(Math.min(12, contents.size()), GRID_COLUMNS);
    }

    private int gridHeight() {
        return gridRows() * SLOT_SIZE;
    }

    private Component emptyDescription() {
        return Component.translatable("item.irons_artifice.bullet_pouch.empty.description", contents.capacity());
    }

    private int emptyDescriptionHeight(Font font) {
        return font.split(emptyDescription(), GRID_WIDTH).size() * LINE_HEIGHT;
    }
}
