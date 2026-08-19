package umpaz.brewinandchewin.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.BrewinAndChewinClient;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.KegStackedContents;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.crafting.FluidIngredientWithAmount;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.mixin.client.GhostRecipeAccessor;
import umpaz.brewinandchewin.common.mixin.client.RecipeBookComponentAccessor;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KegRecipeBookComponent extends RecipeBookComponent {
    private final RecipeManager recipeManager;

    private static final WidgetSprites FILTER_SPRITES = new WidgetSprites(
            BrewinAndChewin.asResource("recipe_book/keg_filter_enabled"),
            BrewinAndChewin.asResource("recipe_book/keg_filter_disabled"),
            BrewinAndChewin.asResource("recipe_book/keg_filter_enabled_highlighted"),
            BrewinAndChewin.asResource("recipe_book/keg_filter_disabled_highlighted")
    );
    private static final Component FILTER_NAME = Component.translatable("brewinandchewin.container.recipe_book.fermentable");

    public KegRecipeBookComponent(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    @Override
    public void initVisuals() {
        if (menu instanceof KegMenu kegMenu)
            ((RecipeBookComponentAccessor)this).brewinandchewin$setStackedContents(new KegStackedContents(kegMenu, recipeManager));
        super.initVisuals();
    }

    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(FILTER_SPRITES);
    }

    public void hide() {
        this.setVisible(false);
    }

    @Nullable
    public RecipeHolder<?> getGhostRecipe() {
        return ghostRecipe.getRecipe();
    }

    @NotNull
    protected Component getRecipeFilterName() {
        return FILTER_NAME;
    }

    @Override
    public void renderTooltip(GuiGraphics gui, int renderX, int renderY, int mouseX, int mouseY) {
        super.renderTooltip(gui, renderX, renderY, mouseX, mouseY);
        if (this.isVisible() && this.menu instanceof KegMenu kegMenu) {
            RecipeHolder<?> recipe = this.ghostRecipe.getRecipe();
            if (recipe != null && recipe.value() instanceof KegFermentingRecipe fermentingRecipe) {
                if (!KegBlockEntity.isValidTemp(kegMenu.getKegTemperature(), fermentingRecipe.getTemperature()))
                    renderTemperatureTooltip(gui, renderX, renderY, mouseX, mouseY);
                Optional<FluidIngredientWithAmount> ingredient = fermentingRecipe.getFluidIngredient();
                if (ingredient.isEmpty() || ingredient.get().ingredient().displayStacks().isEmpty())
                    return;
                List<AbstractedFluidStack> ingredients = ingredient.get().ingredient().displayStacks();
                AbstractedFluidStack fluidStack = ingredients.get(Mth.floor(((GhostRecipeAccessor)ghostRecipe).brewinandchewin$getTime() / 30.0F) % ingredients.size());
                fluidStack = new AbstractedFluidStack(fluidStack.fluid(), ingredient.get().amount(), fluidStack.components(), ingredient.get().unit().orElse(FluidUnit.getLoaderUnit()), fluidStack.loaderSpecific());
                if (!kegMenu.kegTank.getAbstractedFluid().fluid().isSame(fluidStack.fluid()))
                    renderTankTooltip(gui, renderX, renderY, mouseX, mouseY, fluidStack);
            }
        }
    }

    @Override
    public void renderGhostRecipe(GuiGraphics gui, int leftPos, int topPos, boolean singleItem, float partialTick) {
        this.ghostRecipe.render(gui, this.minecraft, leftPos, topPos, singleItem, partialTick);
        if (ghostRecipe.getRecipe() == null)
            return;

        if (this.menu instanceof KegMenu kegMenu) {
            RecipeHolder<?> recipe = this.ghostRecipe.getRecipe();
            if (recipe.value() instanceof KegFermentingRecipe fermentingRecipe) {
                if (!KegBlockEntity.isValidTemp(kegMenu.getKegTemperature(), fermentingRecipe.getTemperature())) {
                    RenderSystem.enableBlend();
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.6F);
                    int temp = fermentingRecipe.getTemperature();
                    int minX = leftPos + 52;
                    int maxX = minX + 8;

                    if (temp < 3) {
                        gui.blit(KegScreen.BACKGROUND_TEXTURE, leftPos + KegScreen.CHILLY_BAR.x(), topPos + KegScreen.CHILLY_BAR.y(), 184, 0, KegScreen.CHILLY_BAR.width(), KegScreen.CHILLY_BAR.height());
                        minX = leftPos + KegScreen.CHILLY_BAR.x();
                    }
                    if (temp == 1) {
                        gui.blit(KegScreen.BACKGROUND_TEXTURE, leftPos + KegScreen.COLD_BAR.x(), topPos + KegScreen.COLD_BAR.y(), 176, 0, KegScreen.COLD_BAR.width(), KegScreen.COLD_BAR.height());
                        minX = leftPos + KegScreen.COLD_BAR.x();
                    }
                    if (temp > 3) {
                        gui.blit(KegScreen.BACKGROUND_TEXTURE, leftPos + KegScreen.WARM_BAR.x(), topPos + KegScreen.WARM_BAR.y(), 201, 0, KegScreen.WARM_BAR.width(), KegScreen.WARM_BAR.height());
                        maxX = leftPos + KegScreen.WARM_BAR.x() + KegScreen.WARM_BAR.width();
                    }
                    if (temp == 5) {
                        gui.blit(KegScreen.BACKGROUND_TEXTURE, leftPos + KegScreen.HOT_BAR.x(), topPos + KegScreen.HOT_BAR.y(), 210, 0, KegScreen.HOT_BAR.width(), KegScreen.HOT_BAR.height());
                        maxX = leftPos + KegScreen.HOT_BAR.x() + KegScreen.HOT_BAR.width();
                    }
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.disableBlend();
                    gui.fill(minX, topPos + 55, maxX, topPos + 58, 822018048);
                }

                Optional<FluidIngredientWithAmount> fluidIngredient = fermentingRecipe.getFluidIngredient();
                // Fluid
                if (fluidIngredient.isPresent() && !fluidIngredient.get().ingredient().displayStacks().isEmpty()) {
                    var ingredients = fluidIngredient.get().ingredient().displayStacks();
                    AbstractedFluidStack fluidStack = ingredients.get(Mth.floor(((GhostRecipeAccessor)ghostRecipe).brewinandchewin$getTime() / 30.0F) % ingredients.size());
                    fluidStack = new AbstractedFluidStack(fluidStack.fluid(), fluidIngredient.get().amount(), fluidStack.components(), fluidIngredient.get().unit().orElse(FluidUnit.getLoaderUnit()), null);
                    if (kegMenu.kegTank.isEmpty() || !kegMenu.kegTank.getAbstractedFluid().fluid().isSame(fluidStack.fluid())) {
                        if (BnCConfiguration.client().renderFluidInKeg()) {
                            BrewinAndChewinClient.getHelper().renderFluidInKeg(fluidStack, gui, leftPos + 120, topPos + 19, 0.6F);
                            gui.fill(RenderType.guiGhostRecipeOverlay(), leftPos + 120, topPos + 19, leftPos + 120 + 16 + 8, topPos + 31 + 16, 822083583);
                        }
                        gui.fill(leftPos + 120, topPos + 19, leftPos + 120 + 16 + 8, topPos + 31 + 16, 822018048);

                        ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(Minecraft.getInstance().level.registryAccess(), fluidStack).copy();
                        int pourCount = (int) (Math.min(fermentingRecipe.getFluidIngredient().get().loaderAmount(), kegMenu.kegTank.getFluidCapacity()) / FluidUnit.convert(250L, FluidUnit.MILLIBUCKET, FluidUnit.getLoaderUnit()));
                        itemDisplay.setCount(pourCount);
                        if (!itemDisplay.isEmpty()) {
                            int itemX = leftPos + 124;
                            int itemY = topPos + 23;
                            gui.renderItem(itemDisplay, itemX, itemY);
                            gui.fill(RenderType.guiGhostRecipeOverlay(), itemX, itemY, itemX + 16, itemY + 16, 822083583);
                            gui.renderItemDecorations(minecraft.font, itemDisplay, itemX, itemY);
                        }
                    }
                } else if (fluidIngredient.isEmpty() && !kegMenu.kegTank.isEmpty()) {
                    gui.fill(leftPos + 120, topPos + 19, leftPos + 120 + 16 + 8, topPos + 31 + 16, 822018048);
                }
            }
        }
    }

    private void renderTankTooltip(GuiGraphics gui, int renderX, int renderY, int mouseX, int mouseY, AbstractedFluidStack stack) {
        if (isHovering(120, 19,  24, 28, mouseX - renderX, mouseY - renderY) && menu instanceof KegMenu kegMenu && (kegMenu.kegTank.isEmpty() || !kegMenu.kegTank.getAbstractedFluid().fluid().isSame(stack.fluid()))) {
            Component component = MutableComponent.create(BrewinAndChewin.getHelper().getFluidDisplayName(stack).getContents())
                    .append((BnCConfiguration.client().displayUnit().shortFormat(" (%s/%s") + ")").formatted(FluidUnit.convert(stack.amount(), stack.unit(), BnCConfiguration.client().displayUnit()), FluidUnit.convert(kegMenu.kegTank.getFluidCapacity(), FluidUnit.getLoaderUnit(), BnCConfiguration.client().displayUnit())));
            List<Component> components = new ArrayList<>(List.of(component));
            if (BnCConfiguration.client().oppositeFluidDisplay() == BnCConfiguration.Client.DisplaySettings.ADVANCED_TOOLTIPS && minecraft.options.advancedItemTooltips || BnCConfiguration.client().oppositeFluidDisplay() == BnCConfiguration.Client.DisplaySettings.ALWAYS) {
                FluidUnit opposite = FluidUnit.getOpposite(BnCConfiguration.client().displayUnit());
                components.add(MutableComponent.create(Component.literal((opposite.shortFormat("%s/%s")).formatted(FluidUnit.convert(stack.amount(), stack.unit(), opposite), FluidUnit.convert(kegMenu.kegTank.getFluidCapacity(), FluidUnit.getLoaderUnit(), opposite))).getContents()).withStyle(ChatFormatting.GRAY));
            }
            if (minecraft.options.advancedItemTooltips) {
                ResourceLocation fluidId = stack.fluid().builtInRegistryHolder().key().location();
                components.add(Component.literal(fluidId.toString()).withStyle(ChatFormatting.DARK_GRAY));
                if (!stack.components().isEmpty()) {
                    components.add(Component.translatable("item.components", stack.components().size()).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
            gui.renderComponentTooltip(minecraft.font, components, mouseX, mouseY);
        }
    }

    private void renderTemperatureTooltip(GuiGraphics gui, int renderX, int renderY, int mouseX, int mouseY) {
        if (isHovering(34, 54, 43, 5, mouseX - renderX, mouseY - renderY) && menu instanceof KegMenu kegMenu && getGhostRecipe().value() instanceof KegFermentingRecipe fermentingRecipe && !KegBlockEntity.isValidTemp(kegMenu.getKegTemperature(), fermentingRecipe.getTemperature())) {
            MutableComponent key = switch (fermentingRecipe.getTemperature()) {
                case 1 -> BnCTextUtils.getTranslation("container.keg.cold");
                case 2 -> BnCTextUtils.getTranslation("container.keg.chilly");
                case 4 -> BnCTextUtils.getTranslation("container.keg.warm");
                case 5 -> BnCTextUtils.getTranslation("container.keg.hot");
                default -> BnCTextUtils.getTranslation("container.keg.normal");
            };
            gui.renderComponentTooltip(minecraft.font, List.of(Component.translatable("brewinandchewin.container.keg.temperature_requirement", key)), mouseX, mouseY);
        }
    }

    private boolean isHovering(int x, int y, int xWidth, int yWidth, int mouseX, int mouseY) {
        int maxX = x + xWidth;
        int maxY = y + yWidth;

        return mouseX >= x && mouseX <= maxX && mouseY >= y && mouseY <= maxY;
    }

    @Override
    public void setupGhostRecipe(RecipeHolder<?> recipe, List<Slot> slots) {
        ItemStack resultStack = recipe.value().getResultItem(this.minecraft.level.registryAccess()).copy();
        this.ghostRecipe.setRecipe(recipe);
        if (slots.get(5).getItem().isEmpty()) {
            this.ghostRecipe.addIngredient(Ingredient.of(resultStack), slots.get(5).x, slots.get(5).y);
        }

        if (recipe.value() instanceof KegFermentingRecipe fermentingRecipe && fermentingRecipe.getResult().left().isPresent()) {
            Optional<RecipeHolder<KegPouringRecipe>> pouringRecipe = recipeManager.getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().filter(kegPouringRecipe -> kegPouringRecipe.value().getRawFluid().matches(fermentingRecipe.getResult().left().get())).findFirst();
            pouringRecipe.ifPresent(kegPouringRecipe -> this.ghostRecipe.addIngredient(Ingredient.of(kegPouringRecipe.value().getContainer()), slots.get(4).x, slots.get(4).y));
        }

        this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe, recipe.value().getIngredients().iterator(), 0);
    }
}