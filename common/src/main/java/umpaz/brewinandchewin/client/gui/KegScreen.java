package umpaz.brewinandchewin.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
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
    public static final ResourceLocation BACKGROUND_TEXTURE = BrewinAndChewin.asResource("textures/gui/keg.png");
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
    public void render(GuiGraphics gui, final int mouseX, final int mouseY, float partialTicks) {
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBackground(gui, mouseX, mouseY, partialTicks);
            this.recipeBookComponent.render(gui, mouseX, mouseY, partialTicks);
        } else {
            super.render(gui, mouseX, mouseY, partialTicks);
            this.recipeBookComponent.render(gui, mouseX, mouseY, partialTicks);
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
        if (isHovering(120, 19, 24, 28, mouseX, mouseY) && !menu.kegTank.isEmpty() && (recipeBookComponent.getGhostRecipe() == null || !(recipeBookComponent.getGhostRecipe().value() instanceof KegFermentingRecipe fermentingRecipe) || fermentingRecipe.getResult().left().isPresent() && fermentingRecipe.getResult().left().get().matches(menu.kegTank.getAbstractedFluid()))) {
            Component containerComponent = (BnCTextUtils.getTranslation("container.keg.served_in", FLUID_CONTAINER_COMPONENTS.computeIfAbsent(menu.kegTank.getAbstractedFluid().fluid(), fluid -> {
                MutableComponent component = MutableComponent.create(PlainTextContents.EMPTY).withStyle(ChatFormatting.GRAY);
                int amountAdded = 0;
                for (KegPouringRecipe recipe : Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().map(RecipeHolder::value).filter(pouringRecipe -> {
                    if (pouringRecipe.isStrict())
                        return pouringRecipe.getRawFluid().matches(menu.kegTank.getAbstractedFluid());
                    return pouringRecipe.getRawFluid().fluid().isSame(menu.kegTank.getAbstractedFluid().fluid());
                }).sorted(Comparator.comparing(recipe -> recipe.getContainer().getItem().getDescription().getString())).toList()) {
                    if (amountAdded > 0)
                        component.append(", ");
                    component.append(recipe.getContainer().getItem().getDescription().plainCopy().withStyle(ChatFormatting.GRAY));
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
                ResourceLocation fluidId = menu.kegTank.getAbstractedFluid().fluid().builtInRegistryHolder().key().location();
                components.add(Component.literal(fluidId.toString()).withStyle(ChatFormatting.DARK_GRAY));
                if (!menu.kegTank.getAbstractedFluid().components().isEmpty()) {
                    components.add(Component.translatable("item.components", menu.kegTank.getAbstractedFluid().components().size()).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
            gui.renderComponentTooltip(this.font, components, mouseX, mouseY);
        }
    }

    private void renderTemperatureTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        if (this.isHovering(35, 54, 42, 5, mouseX, mouseY) && (recipeBookComponent.getGhostRecipe() == null || !(recipeBookComponent.getGhostRecipe().value() instanceof KegFermentingRecipe fermentingRecipe) || KegBlockEntity.isValidTemp(menu.getKegTemperature(), fermentingRecipe.getTemperature()))) {
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

        AbstractedFluidStack fluidStack = this.menu.kegTank.getAbstractedFluid();
        if (!fluidStack.isEmpty() && (recipeBookComponent.getGhostRecipe() == null || !(recipeBookComponent.getGhostRecipe().value() instanceof KegFermentingRecipe fermentingRecipe) || fermentingRecipe.getFluidIngredient().isEmpty() && menu.kegTank.isEmpty() || fermentingRecipe.getFluidIngredient().isPresent() && fermentingRecipe.getFluidIngredient().get().ingredient().matches(fluidStack))) {
            if (BnCConfiguration.client().renderFluidInKeg())
                BrewinAndChewinClient.getHelper().renderFluidInKeg(fluidStack, gui, leftPos + 120, topPos + 19, 1.0F);

            ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(Minecraft.getInstance().level.registryAccess(), fluidStack).copy();
            Optional<KegPouringRecipe> pouringRecipe = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().map(RecipeHolder::value).sorted(Comparator.comparing(KegPouringRecipe::isStrict)).filter(kegPouringRecipe -> {
                if (kegPouringRecipe.isStrict())
                    return ItemStack.isSameItemSameComponents(itemDisplay, kegPouringRecipe.getResultItem(minecraft.level.registryAccess()));
                return ItemStack.isSameItem(itemDisplay, kegPouringRecipe.getResultItem(minecraft.level.registryAccess()));
            }).findFirst();
            int pourCount = pouringRecipe.map(kegPouringRecipe -> (int)(Math.min(this.menu.kegTank.getFluidCapacity(), this.menu.kegTank.getAbstractedFluid().amount()) / kegPouringRecipe.getLoaderAmount())).orElse(1);
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
