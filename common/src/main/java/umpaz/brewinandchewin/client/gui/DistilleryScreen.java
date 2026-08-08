package umpaz.brewinandchewin.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.DistilleryBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.DistilleryMenu;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;

import java.util.List;

public class DistilleryScreen extends AbstractContainerScreen<DistilleryMenu> {
    public static final ResourceLocation BACKGROUND_TEXTURE = BrewinAndChewin.asResource("textures/gui/distillery.png");

    private static final int FLAME_X = 44;
    private static final int FLAME_Y = 36;
    private static final int FLAME_U = 176;
    private static final int FLAME_V = 0;
    private static final int FLAME_WIDTH = 14;
    private static final int FLAME_HEIGHT = 14;

    private static final int ARROW_X = 79;
    private static final int ARROW_Y = 34;
    private static final int ARROW_U = 176;
    private static final int ARROW_V = 14;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 16;

    private static final int WATER_X = 8;
    private static final int WATER_Y = 17;
    private static final int WATER_U = 200;
    private static final int WATER_V = 0;
    private static final int WATER_WIDTH = 12;
    private static final int WATER_HEIGHT = 52;

    public DistilleryScreen(DistilleryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.isLit()) {
            int lit = this.menu.getLitProgressScaled(FLAME_HEIGHT);
            graphics.blit(BACKGROUND_TEXTURE, this.leftPos + FLAME_X, this.topPos + FLAME_Y + FLAME_HEIGHT - lit,
                    FLAME_U, FLAME_V + FLAME_HEIGHT - lit, FLAME_WIDTH, lit);
        }

        int progress = this.menu.getDistillingProgressScaled(ARROW_WIDTH);
        if (progress > 0) {
            graphics.blit(BACKGROUND_TEXTURE, this.leftPos + ARROW_X, this.topPos + ARROW_Y,
                    ARROW_U, ARROW_V, progress, ARROW_HEIGHT);
        }

        int water = this.menu.getWater() * WATER_HEIGHT / DistilleryBlockEntity.MAX_WATER;
        if (water > 0) {
            graphics.blit(BACKGROUND_TEXTURE, this.leftPos + WATER_X, this.topPos + WATER_Y + WATER_HEIGHT - water,
                    WATER_U, WATER_V + WATER_HEIGHT - water, WATER_WIDTH, water);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (this.isHovering(WATER_X, WATER_Y, WATER_WIDTH, WATER_HEIGHT, mouseX, mouseY)) {
            graphics.renderComponentTooltip(this.font, List.of(
                    BnCTextUtils.getTranslation("tooltip.distillery.water", this.menu.getWater(), DistilleryBlockEntity.MAX_WATER)
            ), mouseX, mouseY);
        }
    }
}
