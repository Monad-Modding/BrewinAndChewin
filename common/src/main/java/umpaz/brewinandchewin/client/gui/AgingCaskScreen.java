package umpaz.brewinandchewin.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.AgingCaskBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.AgingCaskMenu;

public class AgingCaskScreen extends AbstractContainerScreen<AgingCaskMenu> {
    public static final ResourceLocation BACKGROUND_TEXTURE = BrewinAndChewin.asResource("textures/gui/aging_cask.png");

    private static final int TEXTURE_WIDTH = 240;
    private static final int TEXTURE_HEIGHT = 256;

    private static final int[] BUBBLE_LENGTHS = {0, 5, 9, 13, 17, 20, 24};
    private static final int BUBBLE_TICKS_PER_FRAME = 6;

    private static final int SLOT_COVER_U = 177;
    private static final int SLOT_COVER_V = 0;
    private static final int SLOT_COVER_SIZE = 18;

    private static final int WINE_SILHOUETTE_U = 177;
    private static final int WINE_SILHOUETTE_V = 19;
    private static final int WINE_SILHOUETTE_SIZE = 18;

    private static final int BUBBLES_U = 177;
    private static final int BUBBLES_V = 57;
    private static final int BUBBLES_X = 14;
    private static final int BUBBLES_Y = 36;
    private static final int BUBBLES_WIDTH = 9;
    private static final int BUBBLES_HEIGHT = 24;

    private static final int ARROW_U = 177;
    private static final int ARROW_V = 82;
    private static final int ARROW_X = 101;
    private static final int ARROW_Y = 41;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 16;

    public AgingCaskScreen(AgingCaskMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 178;
        this.titleLabelY = 3;
        this.inventoryLabelY = 87;
    }

    private void blitSheet(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(BACKGROUND_TEXTURE, x, y, (float) u, (float) v, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        this.blitSheet(graphics, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if (!this.menu.hasWine()) {
            this.blitSheet(graphics,
                    this.leftPos + AgingCaskMenu.GRID_X + AgingCaskMenu.SLOT_SIZE - 1,
                    this.topPos + AgingCaskMenu.GRID_Y + AgingCaskMenu.SLOT_SIZE - 1,
                    WINE_SILHOUETTE_U, WINE_SILHOUETTE_V, WINE_SILHOUETTE_SIZE, WINE_SILHOUETTE_SIZE);
        }

        for (int i = 0; i < AgingCaskBlockEntity.DISTILLATE_SLOTS; ++i) {
            if (this.menu.isDistillateSlotVisible(i))
                continue;
            this.blitSheet(graphics,
                    this.leftPos + AgingCaskMenu.GRID_X + AgingCaskMenu.distillateSlotColumn(i) * AgingCaskMenu.SLOT_SIZE - 1,
                    this.topPos + AgingCaskMenu.GRID_Y + AgingCaskMenu.distillateSlotRow(i) * AgingCaskMenu.SLOT_SIZE - 1,
                    SLOT_COVER_U, SLOT_COVER_V, SLOT_COVER_SIZE, SLOT_COVER_SIZE);
        }

        int aging = this.menu.getAgingTime();
        if (aging <= 0)
            return;

        int bubbles = BUBBLE_LENGTHS[aging / BUBBLE_TICKS_PER_FRAME % BUBBLE_LENGTHS.length];
        if (bubbles > 0) {
            this.blitSheet(graphics,
                    this.leftPos + BUBBLES_X, this.topPos + BUBBLES_Y + BUBBLES_HEIGHT - bubbles,
                    BUBBLES_U, BUBBLES_V + BUBBLES_HEIGHT - bubbles, BUBBLES_WIDTH, bubbles);
        }

        int progress = this.menu.getAgingProgressScaled(ARROW_WIDTH);
        if (progress > 0) {
            this.blitSheet(graphics, this.leftPos + ARROW_X, this.topPos + ARROW_Y,
                    ARROW_U, ARROW_V, progress, ARROW_HEIGHT);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
