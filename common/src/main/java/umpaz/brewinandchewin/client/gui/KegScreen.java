package umpaz.brewinandchewin.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.material.Fluid;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.BrewinAndChewinClient;
import umpaz.brewinandchewin.client.utility.BnCRectangle;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KegScreen extends AbstractContainerScreen<KegMenu> implements RecipeUpdateListener
{
    public static final Identifier BACKGROUND_TEXTURE = BrewinAndChewin.asResource("textures/gui/keg.png");
    private static final BnCRectangle PROGRESS_ARROW = new BnCRectangle(80, 25, 0, 18);
    public static final BnCRectangle COLD_BAR = new BnCRectangle(35, 55, 8, 4);
    public static final BnCRectangle CHILLY_BAR = new BnCRectangle(43, 55, 9, 4);
    public static final BnCRectangle WARM_BAR = new BnCRectangle(60, 55, 9, 4);
    public static final BnCRectangle HOT_BAR = new BnCRectangle(69, 55, 8, 4);
    private static final BnCRectangle LEFT_BUBBLE = new BnCRectangle(109, 44, 9, 24);
    private static final BnCRectangle RIGHT_BUBBLE = new BnCRectangle(147, 44, 9, 24);

    private final KegRecipeBookComponent recipeBookComponent = new KegRecipeBookComponent(this.menu);
    private boolean widthTooNarrow;

    public KegScreen(KegMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn, 176, 166);
        this.leftPos = 0;
        this.topPos = 0;
        this.titleLabelX = 28;
    }

    @Override
    public void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.titleLabelX = 38;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        if (BnCConfiguration.common().recipeBook().enabled()) {
            this.addRenderableWidget(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, button -> {
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
    public void extractRenderState(GuiGraphicsExtractor gui, final int mouseX, final int mouseY, float partialTicks) {
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.extractBackground(gui, mouseX, mouseY, partialTicks);
            this.recipeBookComponent.extractRenderState(gui, mouseX, mouseY, partialTicks);
        } else {
            super.extractRenderState(gui, mouseX, mouseY, partialTicks);
            this.recipeBookComponent.extractRenderState(gui, mouseX, mouseY, partialTicks);
            this.recipeBookComponent.extractGhostRecipe(gui, false);
            this.recipeBookComponent.renderGhostOverlay(gui, this.leftPos, this.topPos);
        }
        gui.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos + 119, this.topPos + 15, 176.0F, 22.0F, 27, 33, 256, 256);
        this.renderTankTooltip(gui, mouseX, mouseY);
        this.renderTemperatureTooltip(gui, mouseX, mouseY);
        this.extractTooltip(gui, mouseX, mouseY);
        this.recipeBookComponent.extractTooltip(gui, mouseX, mouseY);
        this.recipeBookComponent.extractKegGhostTooltip(gui, this.leftPos, this.topPos, mouseX, mouseY);
    }

    private static final Map<Fluid, Component> FLUID_CONTAINER_COMPONENTS = new HashMap<>();

    // Called on /reload.
    public static void clearFluidContainerComponents() {
        FLUID_CONTAINER_COMPONENTS.clear();
    }


    private void renderTankTooltip(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
        if (isHovering(120, 19, 24, 28, mouseX, mouseY) && !menu.kegTank.isEmpty()) {
            Component containerComponent = (BnCTextUtils.getTranslation("container.keg.served_in", FLUID_CONTAINER_COMPONENTS.computeIfAbsent(menu.kegTank.getAbstractedFluid().fluid(), fluid -> {
                MutableComponent component = MutableComponent.create(PlainTextContents.EMPTY).withStyle(ChatFormatting.GRAY);
                int amountAdded = 0;
                for (KegPouringRecipe recipe : Minecraft.getInstance().level.recipeAccess().getSynchronizedRecipes().getAllOfType(BnCRecipeTypes.KEG_POURING).stream().map(RecipeHolder::value).filter(pouringRecipe -> {
                    if (pouringRecipe.isStrict())
                        return pouringRecipe.getRawFluid().matches(menu.kegTank.getAbstractedFluid());
                    return pouringRecipe.getRawFluid().fluid().isSame(menu.kegTank.getAbstractedFluid().fluid());
                }).sorted(Comparator.comparing(recipe -> recipe.getContainer().getHoverName().getString())).toList()) {
                    if (amountAdded > 0)
                        component.append(", ");
                    component.append(recipe.getContainer().getHoverName().plainCopy().withStyle(ChatFormatting.GRAY));
                    ++amountAdded;
                }
                return component;
            }))).withStyle(ChatFormatting.GRAY);
            Component component = MutableComponent.create(BrewinAndChewin.getHelper().getFluidDisplayName(this.menu.kegTank.getAbstractedFluid()).getContents())
                    .append((BnCConfiguration.client().displayUnit().shortFormat(" (%s/%s") + ")").formatted(FluidUnit.convert(menu.kegTank.getAbstractedFluid().amount(), FluidUnit.getLoaderUnit(), BnCConfiguration.client().displayUnit()), FluidUnit.convert(menu.kegTank.getFluidCapacity(), FluidUnit.getLoaderUnit(), BnCConfiguration.client().displayUnit())));
            List<Component> components = new ArrayList<>(List.of(component, containerComponent));
            if (BnCConfiguration.client().oppositeFluidDisplay() == BnCConfiguration.Client.DisplaySettings.ADVANCED_TOOLTIPS && minecraft.options.advancedItemTooltips || BnCConfiguration.client().oppositeFluidDisplay() == BnCConfiguration.Client.DisplaySettings.ALWAYS) {
                FluidUnit opposite = FluidUnit.getOpposite(BnCConfiguration.client().displayUnit());
                components.add(MutableComponent.create(Component.literal((opposite.shortFormat("%s/%s")).formatted(FluidUnit.convert(menu.kegTank.getAbstractedFluid().amount(), FluidUnit.getLoaderUnit(), opposite), FluidUnit.convert(menu.kegTank.getFluidCapacity(), FluidUnit.getLoaderUnit(), opposite))).getContents()).withStyle(ChatFormatting.GRAY));
            }
            if (minecraft.options.advancedItemTooltips) {
                Identifier fluidId = menu.kegTank.getAbstractedFluid().fluid().builtInRegistryHolder().key().identifier();
                components.add(Component.literal(fluidId.toString()).withStyle(ChatFormatting.DARK_GRAY));
                if (!menu.kegTank.getAbstractedFluid().components().isEmpty()) {
                    components.add(Component.translatable("item.components", menu.kegTank.getAbstractedFluid().components().size()).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
            gui.setComponentTooltipForNextFrame(this.font, components, mouseX, mouseY);
        }
    }

    private void renderTemperatureTooltip(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
        if (this.isHovering(35, 54, 42, 5, mouseX, mouseY)) {
            List<Component> tooltip = new ArrayList<>();
            MutableComponent key = switch (menu.getKegTemperature()) {
                case 1 -> BnCTextUtils.getTranslation("container.keg.cold");
                case 2 -> BnCTextUtils.getTranslation("container.keg.chilly");
                case 4 -> BnCTextUtils.getTranslation("container.keg.warm");
                case 5 -> BnCTextUtils.getTranslation("container.keg.hot");
                default -> BnCTextUtils.getTranslation("container.keg.normal");
            };
            tooltip.add(key);
            gui.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
        super.extractLabels(gui, mouseX, mouseY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(gui, mouseX, mouseY, partialTicks);
        if (this.minecraft == null)
            return;

        gui.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        // Render progress arrow
        int l = this.menu.getFermentProgressionScaled();
        gui.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos + PROGRESS_ARROW.x(), this.topPos + PROGRESS_ARROW.y(), 176.0F, 4.0F, l + 1, PROGRESS_ARROW.height(), 256, 256);


        if (menu.isFermenting()) {
            int bubScale = (int) (((this.menu.getProgression() / 80)) * LEFT_BUBBLE.height()) % (LEFT_BUBBLE.height() + 1);
            // render bubbles
            gui.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos + LEFT_BUBBLE.x(), this.topPos + LEFT_BUBBLE.y() - bubScale, 176.0F, (float) (79 - bubScale), LEFT_BUBBLE.width(), bubScale + 1, 256, 256);
            gui.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos + RIGHT_BUBBLE.x(), this.topPos + RIGHT_BUBBLE.y() - bubScale, 186.0F, (float) (79 - bubScale), RIGHT_BUBBLE.width(), bubScale + 1, 256, 256);
        }

        int temp = this.menu.getKegTemperature();
        if (temp == 1) {
            gui.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos + COLD_BAR.x(), this.topPos + COLD_BAR.y(), 176.0F, 0.0F, COLD_BAR.width(), COLD_BAR.height(), 256, 256);
        }
        if (temp < 3) {
            gui.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos + CHILLY_BAR.x(), this.topPos + CHILLY_BAR.y(), 184.0F, 0.0F, CHILLY_BAR.width(), CHILLY_BAR.height(), 256, 256);
        }
        if (temp > 3) {
            gui.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos + WARM_BAR.x(), this.topPos + WARM_BAR.y(), 201.0F, 0.0F, WARM_BAR.width(), WARM_BAR.height(), 256, 256);
        }
        if (temp == 5) {
            gui.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos + HOT_BAR.x(), this.topPos + HOT_BAR.y(), 210.0F, 0.0F, HOT_BAR.width(), HOT_BAR.height(), 256, 256);
        }

        // Render temperature bars

        AbstractedFluidStack fluidStack = this.menu.kegTank.getAbstractedFluid();
        if (!fluidStack.isEmpty()) {
            if (BnCConfiguration.client().renderFluidInKeg())
                BrewinAndChewinClient.getHelper().renderFluidInKeg(fluidStack, gui, leftPos + 120, topPos + 19, 1.0F);

            ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(Minecraft.getInstance().level.registryAccess(), fluidStack).copy();
            Optional<KegPouringRecipe> pouringRecipe = Minecraft.getInstance().level.recipeAccess().getSynchronizedRecipes().getAllOfType(BnCRecipeTypes.KEG_POURING).stream().map(RecipeHolder::value).sorted(Comparator.comparing(KegPouringRecipe::isStrict)).filter(kegPouringRecipe -> {
                if (kegPouringRecipe.isStrict())
                    return ItemStack.isSameItemSameComponents(itemDisplay, kegPouringRecipe.getResultItem(minecraft.level.registryAccess()));
                return ItemStack.isSameItem(itemDisplay, kegPouringRecipe.getResultItem(minecraft.level.registryAccess()));
            }).findFirst();
            int pourCount = pouringRecipe.map(kegPouringRecipe -> (int)(Math.min(this.menu.kegTank.getFluidCapacity(), this.menu.kegTank.getAbstractedFluid().amount()) / kegPouringRecipe.getLoaderAmount())).orElse(1);
            itemDisplay.setCount(pourCount);
            if (!itemDisplay.isEmpty()) {
                gui.item(itemDisplay, this.leftPos + 124, this.topPos + 23);
                gui.itemDecorations(minecraft.font, itemDisplay, this.leftPos + 124, this.topPos + 23);
            }
        }
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.recipeBookComponent.mouseClicked(event, doubleClick)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() || super.mouseClicked(event, doubleClick);
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int x, int y) {
        boolean flag = mouseX < (double)x || mouseY < (double)y || mouseX >= (double)(x + this.imageWidth) || mouseY >= (double)(y + this.imageHeight);
        return flag && this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void slotClicked(Slot slot, int mouseX, int mouseY, ContainerInput clickType) {
        super.slotClicked(slot, mouseX, mouseY, clickType);
        this.recipeBookComponent.slotClicked(slot);
    }

    @Override
    public void recipesUpdated() {
        recipeBookComponent.recipesUpdated();
    }

    @Override
    public void fillGhostRecipe(RecipeDisplay display) {
        recipeBookComponent.fillGhostRecipe(display);
    }

    public RecipeBookComponent getRecipeBookComponent() {
        return recipeBookComponent;
    }
}
