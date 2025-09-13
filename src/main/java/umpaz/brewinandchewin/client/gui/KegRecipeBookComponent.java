package umpaz.brewinandchewin.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.tags.ITag;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.KegStackedContents;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.mixin.client.GhostRecipeAccessor;
import umpaz.brewinandchewin.common.mixin.client.RecipeBookComponentAccessor;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class KegRecipeBookComponent extends RecipeBookComponent {
    private final RecipeManager recipeManager;

    protected static final ResourceLocation RECIPE_BOOK_BUTTONS = BrewinAndChewin.asResource("textures/gui/recipe_book_buttons.png");
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
        this.filterButton.initTextureValues(0, 0, 28, 18, RECIPE_BOOK_BUTTONS);
    }

    public void hide() {
        this.setVisible(false);
    }

    @Nullable
    public Recipe<?> getGhostRecipe() {
        return ghostRecipe.getRecipe();
    }

    @Nonnull
    protected Component getRecipeFilterName() {
        return FILTER_NAME;
    }

    @Override
    public void renderTooltip(GuiGraphics gui, int renderX, int renderY, int mouseX, int mouseY) {
        super.renderTooltip(gui, renderX, renderY, mouseX, mouseY);
        if (this.isVisible() && this.menu instanceof KegMenu kegMenu) {
            Recipe<?> recipe = this.ghostRecipe.getRecipe();
            if (recipe instanceof KegFermentingRecipe fermentingRecipe) {
                if (!KegBlockEntity.isValidTemp(kegMenu.getKegTemperature(), fermentingRecipe.getTemperature()))
                    renderTemperatureTooltip(gui, renderX, renderY, mouseX, mouseY);
                FluidStack fluidStack = fermentingRecipe.getFluidIngredient();
                ITag<Fluid> fluidTag = fermentingRecipe.getFluidIngredientTag();
                if (fluidStack == null)
                    return;
                Fluid tankFluid = kegMenu.kegTank.getFluid().getFluid();
                if (!tankFluid.isSame(fluidStack.getRawFluid()) && (fluidTag == null || !fluidTag.contains(tankFluid)))
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
            Recipe<?> recipe = this.ghostRecipe.getRecipe();
            if (recipe instanceof KegFermentingRecipe fermentingRecipe) {
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

                FluidStack fluidStack = fermentingRecipe.getFluidIngredient();
                ITag<Fluid> fluidTag = fermentingRecipe.getFluidIngredientTag();
                // Fluid
                if (fluidStack == null && !kegMenu.kegTank.isEmpty() || fluidStack != null &&
                        !kegMenu.kegTank.getFluid().getFluid().isSame(fluidStack.getRawFluid()) && (fluidTag == null || !fluidTag.contains(kegMenu.kegTank.getFluid().getFluid()))) {
                    if (fluidStack != null && BnCConfiguration.RENDER_FLUID_IN_KEG.get()) {
                        if (fluidTag != null) {
                            fluidStack = new FluidStack(
                                    fluidTag.stream().toList().get(Mth.floor(((GhostRecipeAccessor)ghostRecipe).brewinandchewin$getTime() / 30.0F) % fluidTag.size()),
                                    fluidStack.getAmount(),
                                    fluidStack.getTag()
                            );
                        }

                        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
                        ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
                        if (stillTexture != null) {
                            TextureAtlasSprite sprite =
                                    this.minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
                            int tintColor = fluidTypeExtensions.getTintColor(fluidStack);

                            float alpha = ((tintColor >> 24) & 0xFF) / 255f / 3f;
                            float red = ((tintColor >> 16) & 0xFF) / 255f;
                            float green = ((tintColor >> 8) & 0xFF) / 255f;
                            float blue = (tintColor & 0xFF) / 255f;

                            float capacity = (float) fermentingRecipe.getFluidIngredient().getAmount() / kegMenu.kegTank.getCapacity();
                            if (capacity > 0.57F) {
                                int y1 = topPos + 19 + (int) (12 * (1 - ((capacity - 0.57F) / .43F)));
                                int y2 = topPos + 19 + 12;
                                float topCapacity = (capacity - 0.57F) / 0.43F;
                                float vDistance = sprite.getV1() - sprite.getV0();
                                float v0 = sprite.getV0() + (0.25F * vDistance) + (0.75F * vDistance * (1 - topCapacity));
                                gui.innerBlit(sprite.atlasLocation(), leftPos + 120, leftPos + 120 + 16, y1, y2, 0, sprite.getU0(), sprite.getU1(), v0, sprite.getV1(), red, green, blue, alpha);
                                gui.innerBlit(sprite.atlasLocation(), leftPos + 120 + 16, leftPos + 120 + 16 + 8, y1, y2, 0, sprite.getU0(), sprite.getU0() + 0.5F * (sprite.getU1() - sprite.getU0()), v0, sprite.getV1(), red, green, blue, alpha);
                            }

                            int y1 = topPos + 31 + (int) (16 * (1 - Math.min(1, (capacity / .57F))));
                            int y2 = topPos + 31 + 16;
                            float vDistance = sprite.getV1() - sprite.getV0();
                            float v0 = sprite.getV0() + (vDistance * (1 - Math.min(1, (capacity / .57F))));
                            gui.innerBlit(sprite.atlasLocation(), leftPos + 120, leftPos + 120 + 16, y1, y2, 0, sprite.getU0(), sprite.getU1(), v0, sprite.getV1(), red, green, blue, alpha);
                            gui.innerBlit(sprite.atlasLocation(), leftPos + 120 + 16, leftPos + 120 + 16 + 8, y1, y2, 0, sprite.getU0(), sprite.getU0() + 0.5F * (sprite.getU1() - sprite.getU0()), v0, sprite.getV1(), red, green, blue, alpha);
                        }
                    }
                    gui.fill(leftPos + 120, topPos + 19, leftPos + 120 + 16 + 8, topPos + 31 + 16, 822018048);

                    if (fluidStack != null) {
                        ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(Minecraft.getInstance().level.registryAccess(), fluidStack).copy();
                        int pourCount = Math.min(fermentingRecipe.getFluidIngredient().getAmount(), kegMenu.kegTank.getCapacity()) / 250;
                        itemDisplay.setCount(pourCount);
                        if (!itemDisplay.isEmpty()) {
                            int itemX = leftPos + 124;
                            int itemY = topPos + 23;
                            gui.renderItem(itemDisplay, itemX, itemY);
                            gui.fill(RenderType.guiGhostRecipeOverlay(), itemX, itemY, itemX + 16, itemY + 16, 822083583);
                            gui.renderItemDecorations(minecraft.font, itemDisplay, itemX, itemY);
                        }
                    }
                }
            }
        }
    }

    private void renderTankTooltip(GuiGraphics gui, int renderX, int renderY, int mouseX, int mouseY, FluidStack stack) {
        if (isHovering(108, 19, 24, 28, mouseX - renderX, mouseY - renderY) && menu instanceof KegMenu kegMenu && !kegMenu.kegTank.isEmpty() ) {
            gui.renderTooltip(minecraft.font, stack.getDisplayName(), mouseX, mouseY);
        }
    }

    private void renderTemperatureTooltip(GuiGraphics gui, int renderX, int renderY, int mouseX, int mouseY) {
        if ( this.isHovering(34, 54, 43, 5, mouseX - renderX, mouseY - renderY) && menu instanceof KegMenu kegMenu && getGhostRecipe() instanceof KegFermentingRecipe fermentingRecipe && !KegBlockEntity.isValidTemp(kegMenu.getKegTemperature(), fermentingRecipe.getTemperature())) {
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
    public void setupGhostRecipe(Recipe<?> recipe, List<Slot> slots) {
        ItemStack resultStack = recipe.getResultItem(this.minecraft.level.registryAccess()).copy();
        this.ghostRecipe.setRecipe(recipe);
        if (slots.get(5).getItem().isEmpty()) {
            this.ghostRecipe.addIngredient(Ingredient.of(resultStack), slots.get(5).x, slots.get(5).y);
        }

        if (recipe instanceof KegFermentingRecipe fermentingRecipe && fermentingRecipe.getResultFluid() != null) {
            Optional<KegPouringRecipe> pouringRecipe = recipeManager.getAllRecipesFor(BnCRecipeTypes.KEG_POURING.get()).stream().filter(kegPouringRecipe -> kegPouringRecipe.getRawFluid().isSame(fermentingRecipe.getResultFluid())).findFirst();
            pouringRecipe.ifPresent(kegPouringRecipe -> this.ghostRecipe.addIngredient(Ingredient.of(kegPouringRecipe.getContainer()), slots.get(4).x, slots.get(4).y));
        }

        this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe, recipe.getIngredients().iterator(), 0);
    }
}