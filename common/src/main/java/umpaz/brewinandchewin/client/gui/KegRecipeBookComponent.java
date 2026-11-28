package umpaz.brewinandchewin.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.BrewinAndChewinClient;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.crafting.FluidIngredientWithAmount;
import umpaz.brewinandchewin.common.crafting.display.KegFermentingRecipeDisplay;
import umpaz.brewinandchewin.common.mixin.client.GhostSlotsInvoker;
import umpaz.brewinandchewin.common.mixin.client.RecipeBookComponentAccessor;
import umpaz.brewinandchewin.common.registry.BnCCommonRecipeBookCategories;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;
import umpaz.brewinandchewin.common.utility.FluidUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KegRecipeBookComponent extends RecipeBookComponent<KegMenu> {
    private static final WidgetSprites FILTER_SPRITES = new WidgetSprites(
            BrewinAndChewin.asResource("recipe_book/keg_filter_enabled"),
            BrewinAndChewin.asResource("recipe_book/keg_filter_disabled"),
            BrewinAndChewin.asResource("recipe_book/keg_filter_enabled_highlighted"),
            BrewinAndChewin.asResource("recipe_book/keg_filter_disabled_highlighted")
    );
    private static final Component FILTER_NAME = Component.translatable("brewinandchewin.container.recipe_book.fermentable");

    private static final List<TabInfo> TABS = List.of(
            new TabInfo(SearchRecipeBookCategory.CRAFTING),
            new TabInfo(BnCItems.UNRIPE_FLAXEN_CHEESE_WHEEL, BnCCommonRecipeBookCategories.FERMENTING_MEALS),
            new TabInfo(BnCItems.BEER, BnCCommonRecipeBookCategories.FERMENTING_DRINKS)
    );

    @Nullable
    private KegFermentingRecipeDisplay currentDisplay;
    @Nullable
    private RecipeDisplayId lastTrackedRecipe;
    private int ghostTime;

    public KegRecipeBookComponent(KegMenu menu) {
        super(menu, TABS);
    }

    @Override
    protected WidgetSprites getFilterButtonTextures() {
        return FILTER_SPRITES;
    }

    public void hide() {
        this.setVisible(false);
        this.currentDisplay = null;
        this.lastTrackedRecipe = null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        boolean handled = super.mouseClicked(event, isDoubleClick);
        if (handled) {
            RecipeBookComponentAccessor accessor = (RecipeBookComponentAccessor) this;
            RecipeDisplayId lastId = accessor.brewinandchewin$getLastRecipe();
            RecipeCollection lastCollection = accessor.brewinandchewin$getLastRecipeCollection();
            if (lastId != null && lastCollection != null && !lastId.equals(this.lastTrackedRecipe)) {
                this.lastTrackedRecipe = lastId;
                for (RecipeDisplayEntry entry : lastCollection.getRecipes()) {
                    if (entry.id().equals(lastId) && entry.display() instanceof KegFermentingRecipeDisplay fermentingDisplay) {
                        this.currentDisplay = fermentingDisplay;
                        this.ghostTime = 0;
                        break;
                    }
                }
            }
        }
        return handled;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.currentDisplay != null) {
            this.ghostTime++;
        }
    }

    @Override
    protected boolean isCraftingSlot(Slot slot) {
        return slot.index <= 3;
    }

    @Override
    protected void selectMatchingRecipes(RecipeCollection collection, StackedItemContents stackedContents) {
        collection.selectRecipes(stackedContents, display -> display instanceof KegFermentingRecipeDisplay);
    }

    @Override
    protected @NotNull Component getRecipeFilterName() {
        return FILTER_NAME;
    }

    @Override
    public void slotClicked(@Nullable Slot slot) {
        super.slotClicked(slot);
        if (slot != null && slot.index <= 3) {
            boolean allEmpty = true;
            for (int i = 0; i < 4; i++) {
                if (!this.menu.getSlot(i).getItem().isEmpty()) {
                    allEmpty = false;
                    break;
                }
            }
            if (allEmpty) {
                this.currentDisplay = null;
                this.lastTrackedRecipe = null;
            }
        }
    }

    @Override
    protected void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay display, ContextMap context) {
        GhostSlotsInvoker invoker = (GhostSlotsInvoker) ghostSlots;
        invoker.brewinandchewin$setResult(this.menu.getSlot(5), context, display.result());
        if (display instanceof KegFermentingRecipeDisplay fermentingDisplay) {
            this.currentDisplay = fermentingDisplay;
            this.ghostTime = 0;
            List<Slot> inputSlots = this.menu.slots.subList(0, 4);
            for (int i = 0; i < Math.min(fermentingDisplay.ingredients().size(), inputSlots.size()); i++) {
                invoker.brewinandchewin$setInput(inputSlots.get(i), context, fermentingDisplay.ingredients().get(i));
            }
        } else {
            this.currentDisplay = null;
        }
    }

    public void renderGhostOverlay(GuiGraphicsExtractor gui, int leftPos, int topPos) {
        if (!this.isVisible() || this.currentDisplay == null || this.minecraft == null || this.minecraft.level == null)
            return;

        if (!KegBlockEntity.isValidTemp(this.menu.getKegTemperature(), this.currentDisplay.temperature())) {
            int temp = this.currentDisplay.temperature();
            int minX = leftPos + 52;
            int maxX = minX + 8;

            if (temp < 3) {
                gui.blit(RenderPipelines.GUI_TEXTURED, KegScreen.BACKGROUND_TEXTURE, leftPos + KegScreen.CHILLY_BAR.x(), topPos + KegScreen.CHILLY_BAR.y(), 184.0F, 0.0F, KegScreen.CHILLY_BAR.width(), KegScreen.CHILLY_BAR.height(), 256, 256);
                minX = leftPos + KegScreen.CHILLY_BAR.x();
            }
            if (temp == 1) {
                gui.blit(RenderPipelines.GUI_TEXTURED, KegScreen.BACKGROUND_TEXTURE, leftPos + KegScreen.COLD_BAR.x(), topPos + KegScreen.COLD_BAR.y(), 176.0F, 0.0F, KegScreen.COLD_BAR.width(), KegScreen.COLD_BAR.height(), 256, 256);
                minX = leftPos + KegScreen.COLD_BAR.x();
            }
            if (temp > 3) {
                gui.blit(RenderPipelines.GUI_TEXTURED, KegScreen.BACKGROUND_TEXTURE, leftPos + KegScreen.WARM_BAR.x(), topPos + KegScreen.WARM_BAR.y(), 201.0F, 0.0F, KegScreen.WARM_BAR.width(), KegScreen.WARM_BAR.height(), 256, 256);
                maxX = leftPos + KegScreen.WARM_BAR.x() + KegScreen.WARM_BAR.width();
            }
            if (temp == 5) {
                gui.blit(RenderPipelines.GUI_TEXTURED, KegScreen.BACKGROUND_TEXTURE, leftPos + KegScreen.HOT_BAR.x(), topPos + KegScreen.HOT_BAR.y(), 210.0F, 0.0F, KegScreen.HOT_BAR.width(), KegScreen.HOT_BAR.height(), 256, 256);
                maxX = leftPos + KegScreen.HOT_BAR.x() + KegScreen.HOT_BAR.width();
            }
            gui.fill(minX, topPos + 55, maxX, topPos + 59, 0x60FF0000);
        }

        Optional<FluidIngredientWithAmount> fluidIngredient = this.currentDisplay.fluidIngredient();
        if (fluidIngredient.isPresent() && !fluidIngredient.get().ingredient().displayStacks().isEmpty()) {
            List<AbstractedFluidStack> ingredients = fluidIngredient.get().ingredient().displayStacks();
            AbstractedFluidStack fluidStack = ingredients.get((this.ghostTime / 30) % ingredients.size());
            fluidStack = new AbstractedFluidStack(fluidStack.fluid(), fluidIngredient.get().amount(), fluidStack.components(), fluidIngredient.get().unit().orElse(FluidUnit.getLoaderUnit()), fluidStack.loaderSpecific());
            if (this.menu.kegTank.isEmpty() || !this.menu.kegTank.getAbstractedFluid().fluid().isSame(fluidStack.fluid())) {
                if (BnCConfiguration.client().renderFluidInKeg()) {
                    BrewinAndChewinClient.getHelper().renderFluidInKeg(fluidStack, gui, leftPos + 120, topPos + 19, 0.6F);
                    gui.fill(leftPos + 120, topPos + 19, leftPos + 120 + 24, topPos + 31 + 16, 822083583);
                }
                gui.fill(leftPos + 120, topPos + 19, leftPos + 120 + 24, topPos + 31 + 16, 0x60FF0000);

                ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(this.minecraft.level.registryAccess(), fluidStack).copy();
                int pourCount = (int) (Math.min(fluidIngredient.get().loaderAmount(), this.menu.kegTank.getFluidCapacity()) / FluidUnit.convert(250L, FluidUnit.MILLIBUCKET, FluidUnit.getLoaderUnit()));
                itemDisplay.setCount(Math.max(1, pourCount));
                if (!itemDisplay.isEmpty()) {
                    int itemX = leftPos + 124;
                    int itemY = topPos + 23;
                    gui.item(itemDisplay, itemX, itemY);
                    gui.fill(itemX, itemY, itemX + 16, itemY + 16, 822083583);
                    gui.itemDecorations(this.minecraft.font, itemDisplay, itemX, itemY);
                }
            }
        } else if (fluidIngredient.isEmpty() && !this.menu.kegTank.isEmpty()) {
            gui.fill(leftPos + 120, topPos + 19, leftPos + 120 + 24, topPos + 31 + 16, 0x60FF0000);
        }
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor gui, int mouseX, int mouseY, @Nullable Slot hoveredSlot) {
        super.extractTooltip(gui, mouseX, mouseY, hoveredSlot);
    }

    public void extractKegGhostTooltip(GuiGraphicsExtractor gui, int renderX, int renderY, int mouseX, int mouseY) {
        if (!this.isVisible() || this.currentDisplay == null || this.minecraft == null)
            return;

        if (!KegBlockEntity.isValidTemp(this.menu.getKegTemperature(), this.currentDisplay.temperature())) {
            int relX = mouseX - renderX;
            int relY = mouseY - renderY;
            if (relX >= 34 && relX < 77 && relY >= 54 && relY < 59) {
                MutableComponent key = switch (this.currentDisplay.temperature()) {
                    case 1 -> BnCTextUtils.getTranslation("container.keg.cold");
                    case 2 -> BnCTextUtils.getTranslation("container.keg.chilly");
                    case 4 -> BnCTextUtils.getTranslation("container.keg.warm");
                    case 5 -> BnCTextUtils.getTranslation("container.keg.hot");
                    default -> BnCTextUtils.getTranslation("container.keg.normal");
                };
                gui.setComponentTooltipForNextFrame(this.minecraft.font, List.of(Component.translatable("brewinandchewin.container.keg.temperature_requirement", key)), mouseX, mouseY);
            }
        }

        Optional<FluidIngredientWithAmount> ingredient = this.currentDisplay.fluidIngredient();
        int relX = mouseX - renderX;
        int relY = mouseY - renderY;
        if (relX >= 120 && relX < 144 && relY >= 19 && relY < 47) {
            if (ingredient.isPresent() && !ingredient.get().ingredient().displayStacks().isEmpty()) {
                List<AbstractedFluidStack> ingredients = ingredient.get().ingredient().displayStacks();
                AbstractedFluidStack fluidStack = ingredients.get((this.ghostTime / 30) % ingredients.size());
                fluidStack = new AbstractedFluidStack(fluidStack.fluid(), ingredient.get().amount(), fluidStack.components(), ingredient.get().unit().orElse(FluidUnit.getLoaderUnit()), fluidStack.loaderSpecific());
                if (this.menu.kegTank.isEmpty() || !this.menu.kegTank.getAbstractedFluid().fluid().isSame(fluidStack.fluid())) {
                    FluidUnit unit = BnCConfiguration.client().displayUnit();
                    Component component = MutableComponent.create(BrewinAndChewin.getHelper().getFluidDisplayName(fluidStack).getContents())
                            .append((unit.shortFormat(" (%s/%s") + ")").formatted(FluidUnit.convert(fluidStack.amount(), fluidStack.unit(), unit), FluidUnit.convert(this.menu.kegTank.getFluidCapacity(), FluidUnit.getLoaderUnit(), unit)));
                    List<Component> components = new ArrayList<>(List.of(component));
                    if (this.minecraft.options.advancedItemTooltips) {
                        Identifier fluidId = fluidStack.fluid().builtInRegistryHolder().key().identifier();
                        components.add(Component.literal(fluidId.toString()).withStyle(ChatFormatting.DARK_GRAY));
                    }
                    gui.setComponentTooltipForNextFrame(this.minecraft.font, components, mouseX, mouseY);
                }
            } else if (ingredient.isEmpty() && !this.menu.kegTank.isEmpty()) {
                gui.setComponentTooltipForNextFrame(this.minecraft.font, List.of(Component.literal("No fluid required (empty tank)").withStyle(ChatFormatting.RED)), mouseX, mouseY);
            }
        }
    }

    public void extractTooltip(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
        this.extractTooltip(gui, mouseX, mouseY, null);
    }
}
