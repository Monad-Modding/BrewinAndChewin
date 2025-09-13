package umpaz.brewinandchewin.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.utility.BnCRectangle;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KegScreen extends AbstractContainerScreen<KegMenu> implements RecipeUpdateListener
{
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
    public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(BrewinAndChewin.MODID, "textures/gui/keg.png");
    private static final BnCRectangle PROGRESS_ARROW = new BnCRectangle(80, 25, 0, 18);
    public static final BnCRectangle COLD_BAR = new BnCRectangle(35, 55, 8, 4);
    public static final BnCRectangle CHILLY_BAR = new BnCRectangle(43, 55, 9, 4);
    public static final BnCRectangle WARM_BAR = new BnCRectangle(60, 55, 9, 4);
    public static final BnCRectangle HOT_BAR = new BnCRectangle(69, 55, 8, 4);
    private static final BnCRectangle LEFT_BUBBLE = new BnCRectangle(109, 44, 9, 24);
    private static final BnCRectangle RIGHT_BUBBLE = new BnCRectangle(147, 44, 9, 24);

    private final KegRecipeBookComponent recipeBookComponent = new KegRecipeBookComponent(Minecraft.getInstance().level.getRecipeManager());
    private boolean widthTooNarrow;

    public KegScreen(KegMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.leftPos = 0;
        this.topPos = 0;
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 28;
    }

    @Override
    public void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.titleLabelX = 38;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        if (BnCConfiguration.ENABLE_RECIPE_BOOK_KEG.get()) {
            this.addRenderableWidget(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (button) -> {
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                button.setPosition(this.leftPos + 5, this.height / 2 - 49);
            }));
        } else {
            this.recipeBookComponent.hide();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        }

        this.addWidget(this.recipeBookComponent);
        this.setInitialFocus(this.recipeBookComponent);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.recipeBookComponent.tick();
    }

    @Override
    public void render(GuiGraphics gui, final int mouseX, final int mouseY, float partialTicks) {
        this.renderBackground(gui);
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(gui, partialTicks, mouseX, mouseY);
            this.recipeBookComponent.render(gui, mouseX, mouseY, partialTicks);
        } else {
            this.recipeBookComponent.render(gui, mouseX, mouseY, partialTicks);
            super.render(gui, mouseX, mouseY, partialTicks);
            this.recipeBookComponent.renderGhostRecipe(gui, this.leftPos, this.topPos, false, partialTicks);
        }
        gui.blit(BACKGROUND_TEXTURE, this.leftPos + 119, this.topPos + 15, 176, 22, 27, 33);
        this.renderTankTooltip(gui, mouseX, mouseY);
        this.renderTemperatureTooltip(gui, mouseX, mouseY);
        this.renderTooltip(gui, mouseX, mouseY);
        this.recipeBookComponent.renderTooltip(gui, this.leftPos, this.topPos, mouseX, mouseY);
    }

    private static final Map<Fluid, Component> FLUID_CONTAINER_COMPONENTS = new HashMap<>();

    // Called on /reload.
    public static void clearFluidContainerComponents() {
        FLUID_CONTAINER_COMPONENTS.clear();
    }


    private void renderTankTooltip(GuiGraphics gui, int mouseX, int mouseY) {

            if ( isHovering(120, 19, 24, 28, mouseX, mouseY) && !this.menu.kegTank.isEmpty() && (!(recipeBookComponent.getGhostRecipe() instanceof KegFermentingRecipe fermentingRecipe) || fermentingRecipe.getFluidIngredient() == null || fermentingRecipe.getFluidIngredient().getFluid().isSame(menu.kegTank.getFluid().getFluid()) || fermentingRecipe.getFluidIngredientTag() != null && fermentingRecipe.getFluidIngredientTag().contains(menu.kegTank.getFluid().getFluid()))) {
            Component containerComponent = (BnCTextUtils.getTranslation("container.keg.served_in", FLUID_CONTAINER_COMPONENTS.computeIfAbsent(menu.kegTank.getFluid().getFluid(), fluid -> {
                MutableComponent component = MutableComponent.create(ComponentContents.EMPTY).withStyle(ChatFormatting.GRAY);
                int amountAdded = 0;
                for (KegPouringRecipe recipe : Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING.get()).stream().filter(pouringRecipe -> pouringRecipe.getRawFluid().isSame(fluid)).sorted(Comparator.comparing(recipe -> recipe.getContainer().getItem().getDescription().getString())).toList()) {
                    if (amountAdded > 0)
                        component.append(", ");
                    component.append(recipe.getContainer().getItem().getDescription().plainCopy().withStyle(ChatFormatting.GRAY));
                    ++amountAdded;
                }
                return component;
            }))).withStyle(ChatFormatting.GRAY);
            Component component = MutableComponent.create(this.menu.kegTank.getFluid().getDisplayName().getContents())
                    .append(" (%s/%s mB)".formatted(this.menu.kegTank.getFluidAmount(), this.menu.kegTank.getCapacity()));
            gui.renderComponentTooltip(this.font, List.of(component, containerComponent), mouseX, mouseY);
        }
    }

    private void renderTemperatureTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        if ( this.isHovering(35, 54, 42, 5, mouseX, mouseY) && (!(recipeBookComponent.getGhostRecipe() instanceof KegFermentingRecipe fermentingRecipe) || KegBlockEntity.isValidTemp(menu.getKegTemperature(), fermentingRecipe.getTemperature()))) {
            List<Component> tooltip = new ArrayList<>();
            MutableComponent key = switch (menu.getKegTemperature()) {
                case 1 -> BnCTextUtils.getTranslation("container.keg.cold");
                case 2 -> BnCTextUtils.getTranslation("container.keg.chilly");
                case 4 -> BnCTextUtils.getTranslation("container.keg.warm");
                case 5 -> BnCTextUtils.getTranslation("container.keg.hot");
                default -> BnCTextUtils.getTranslation("container.keg.normal");
            };
            tooltip.add(key);
            gui.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderLabels(gui, mouseX, mouseY);
        gui.drawString(this.font, this.playerInventoryTitle, 8, (this.imageHeight - 96 + 2), 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTicks, int mouseX, int mouseY) {
        // Render UI background
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (this.minecraft == null)
            return;

        gui.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Render progress arrow
        int l = this.menu.getFermentProgressionScaled();
        gui.blit(BACKGROUND_TEXTURE, this.leftPos + PROGRESS_ARROW.x(), this.topPos + PROGRESS_ARROW.y(), 176, 4, l + 1, PROGRESS_ARROW.height());


        if (menu.isFermenting()) {
            int bubScale = (int) (((this.menu.getProgression() / 80)) * LEFT_BUBBLE.height()) % (LEFT_BUBBLE.height() + 1);
            // render bubbles
            gui.blit(BACKGROUND_TEXTURE, this.leftPos + LEFT_BUBBLE.x(), this.topPos + LEFT_BUBBLE.y() - bubScale, 176, 79 - bubScale, LEFT_BUBBLE.width(), bubScale + 1);
            gui.blit(BACKGROUND_TEXTURE, this.leftPos + RIGHT_BUBBLE.x(), this.topPos + RIGHT_BUBBLE.y() - bubScale, 186, 79 - bubScale, RIGHT_BUBBLE.width(), bubScale + 1);
        }

        int temp = this.menu.getKegTemperature();
        if (temp == 1) {
            gui.blit(BACKGROUND_TEXTURE, this.leftPos + COLD_BAR.x(), this.topPos + COLD_BAR.y(), 176, 0, COLD_BAR.width(), COLD_BAR.height());
        }
        if (temp < 3) {
            gui.blit(BACKGROUND_TEXTURE, this.leftPos + CHILLY_BAR.x(), this.topPos + CHILLY_BAR.y(), 184, 0, CHILLY_BAR.width(), CHILLY_BAR.height());
        }
        if (temp > 3) {
            gui.blit(BACKGROUND_TEXTURE, this.leftPos + WARM_BAR.x(), this.topPos + WARM_BAR.y(), 201, 0, WARM_BAR.width(), WARM_BAR.height());
        }
        if (temp == 5) {
            gui.blit(BACKGROUND_TEXTURE, this.leftPos + HOT_BAR.x(), this.topPos + HOT_BAR.y(), 210, 0, HOT_BAR.width(), HOT_BAR.height());
        }

        // Render temperature bars

        FluidStack fluidStack = this.menu.kegTank.getFluid();
        if (!fluidStack.isEmpty() && (!(recipeBookComponent.getGhostRecipe() instanceof KegFermentingRecipe fermentingRecipe) || fermentingRecipe.getFluidIngredient() == null && menu.kegTank.isEmpty() || fermentingRecipe.getFluidIngredient() != null && fermentingRecipe.getFluidIngredient().getFluid().isSame(fluidStack.getRawFluid()) || fermentingRecipe.getFluidIngredientTag() != null && fermentingRecipe.getFluidIngredientTag().contains(menu.kegTank.getFluid().getFluid()))) {

            // Fluid
            if (BnCConfiguration.RENDER_FLUID_IN_KEG.get()) {
                IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
                ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
                if (stillTexture != null) {
                    TextureAtlasSprite sprite =
                            this.minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
                    int tintColor = fluidTypeExtensions.getTintColor(fluidStack);

                    float alpha = ((tintColor >> 24) & 0xFF) / 255f;
                    float red = ((tintColor >> 16) & 0xFF) / 255f;
                    float green = ((tintColor >> 8) & 0xFF) / 255f;
                    float blue = (tintColor & 0xFF) / 255f;

                    float capacity = Math.min(this.menu.kegTank.getCapacity(), this.menu.kegTank.getFluidAmount()) / (float) this.menu.kegTank.getCapacity();
                    if (capacity > 0.57) {
                        int y1 = this.topPos + 19 + (int) (12 * (1 - ((capacity - 0.57F) / .43F)));
                        int y2 = this.topPos + 19 + 12;
                        float topCapacity = (capacity - 0.57F) / 0.43F;
                        float vDistance = sprite.getV1() - sprite.getV0();
                        float v0 = sprite.getV0() + (0.25F * vDistance) + (0.75F * vDistance * (1 - topCapacity));
                        gui.innerBlit(sprite.atlasLocation(), this.leftPos + 120, this.leftPos + 120 + 16, y1, y2, 0, sprite.getU0(), sprite.getU1(), v0, sprite.getV1(), red, green, blue, alpha);
                        gui.innerBlit(sprite.atlasLocation(), this.leftPos + 120 + 16, this.leftPos + 120 + 16 + 8, y1, y2, 0, sprite.getU0(), sprite.getU0() + 0.5F * (sprite.getU1() - sprite.getU0()), v0, sprite.getV1(), red, green, blue, alpha);

                    }
                    int y1 = this.topPos + 31 + (int) (16 * (1 - Math.min(1, (capacity / .57F))));
                    int y2 = this.topPos + 31 + 16;
                    float vDistance = sprite.getV1() - sprite.getV0();
                    float v0 = sprite.getV0() + (vDistance * (1 - Math.min(1, (capacity / .57F))));
                    gui.innerBlit(sprite.atlasLocation(), this.leftPos + 120, this.leftPos + 120 + 16, y1, y2, 0, sprite.getU0(), sprite.getU1(), v0, sprite.getV1(), red, green, blue, alpha);
                    gui.innerBlit(sprite.atlasLocation(), this.leftPos + 120 + 16, this.leftPos + 120 + 16 + 8, y1, y2, 0, sprite.getU0(), sprite.getU0() + 0.5F * (sprite.getU1() - sprite.getU0()), v0, sprite.getV1(), red, green, blue, alpha);

                }
            }

            ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(Minecraft.getInstance().level.registryAccess(), fluidStack).copy();
            Optional<KegPouringRecipe> pouringRecipe = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING.get()).stream().sorted(Comparator.comparing(KegPouringRecipe::isStrict)).filter(kegPouringRecipe ->
                    ItemStack.isSameItemSameTags(itemDisplay, kegPouringRecipe.getResultItem(minecraft.level.registryAccess()))
            ).findFirst();
            int pourCount = pouringRecipe.map(kegPouringRecipe -> Math.min(this.menu.kegTank.getCapacity(), this.menu.kegTank.getFluidAmount()) / kegPouringRecipe.getAmount()).orElse(1);
            itemDisplay.setCount(pourCount);
            if (!itemDisplay.isEmpty()) {
                gui.renderItem(itemDisplay, this.leftPos + 124, this.topPos + 23);
                gui.renderItemDecorations(minecraft.font, itemDisplay, this.leftPos + 124, this.topPos + 23);
            }
        }
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
        if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, buttonId)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() || super.mouseClicked(mouseX, mouseY, buttonId);
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int x, int y, int buttonIdx) {
        boolean flag = mouseX < (double)x || mouseY < (double)y || mouseX >= (double)(x + this.imageWidth) || mouseY >= (double)(y + this.imageHeight);
        return flag && this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, buttonIdx);
    }

    @Override
    protected void slotClicked(Slot slot, int mouseX, int mouseY, ClickType clickType) {
        super.slotClicked(slot, mouseX, mouseY, clickType);
        this.recipeBookComponent.slotClicked(slot);
    }

    @Override
    public void recipesUpdated() {
        recipeBookComponent.recipesUpdated();
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return recipeBookComponent;
    }
}
