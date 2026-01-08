package umpaz.brewinandchewin.integration.jei.category;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;
import umpaz.brewinandchewin.integration.jei.BnCJEIRecipeTypes;
import umpaz.brewinandchewin.integration.jei.KegFermentingPouringRecipe;
import vectorwing.farmersdelight.common.utility.ClientRenderUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public class FermentingRecipeCategory implements IRecipeCategory<KegFermentingPouringRecipe> {
    public static final ResourceLocation UID = BrewinAndChewin.asResource("fermenting");
    protected final IModIdHelper modIdHelper;
    protected final IPlatformFluidHelper<?> fluidHelper;

    protected final IDrawableAnimated arrow;
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable cold;
    private final IDrawable chilly;
    private final IDrawable warm;
    private final IDrawable hot;
    protected final IDrawable timeIcon;
    protected final IDrawable expIcon;
    protected final IDrawable kegOverlay;
    protected final IDrawableAnimated leftBubble;
    protected final IDrawableAnimated rightBubble;

    public FermentingRecipeCategory(IGuiHelper guiHelper, IPlatformFluidHelper<?> fluidHelper, IModIdHelper modIdHelper) {
        this.modIdHelper = modIdHelper;
        this.fluidHelper = fluidHelper;
        title = BnCTextUtils.getTranslation("jei.fermenting");
        ResourceLocation backgroundImage = BrewinAndChewin.asResource("textures/gui/jei/keg.png");
        background = guiHelper.createDrawable(backgroundImage, 12, 13, 136, 56);
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BnCItems.KEG));
        arrow = guiHelper.drawableBuilder(backgroundImage, 171, 4, 23, 16)
                .buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
        cold = guiHelper.createDrawable(backgroundImage, 170, 0, 8, 3);
        chilly = guiHelper.createDrawable(backgroundImage, 178, 0, 9, 3);
        warm = guiHelper.createDrawable(backgroundImage, 195, 0, 9, 3);
        hot = guiHelper.createDrawable(backgroundImage, 204, 0, 8, 3);
        expIcon = guiHelper.createDrawable(backgroundImage, 170, 32, 9, 9);
        timeIcon = guiHelper.createDrawable(backgroundImage, 170, 21, 8, 11);
        kegOverlay = guiHelper.createDrawable(backgroundImage, 170, 45, 26, 30);
        leftBubble = guiHelper.drawableBuilder(backgroundImage, 170, 75, 9, 24)
                .buildAnimated(50, IDrawableAnimated.StartDirection.BOTTOM, false);
        rightBubble = guiHelper.drawableBuilder(backgroundImage, 180, 75, 9, 24)
                .buildAnimated(50, IDrawableAnimated.StartDirection.BOTTOM, false);
    }

    @Override
    public RecipeType<KegFermentingPouringRecipe> getRecipeType() {
        return BnCJEIRecipeTypes.FERMENTING;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, KegFermentingPouringRecipe recipe, IFocusGroup focusGroup) {
        if (recipe.getFluidIngredient().isPresent()) {
            // used to be config
            if (true) {
                IRecipeSlotBuilder recipeSlot = builder.addSlot(RecipeIngredientRole.INPUT, 0, 2)
                        .setFluidRenderer(81000L, false, 26, 30)
                        .setOverlay(kegOverlay, 0, 0);
                recipeSlot.addIngredients((IIngredientType) fluidHelper.getFluidIngredientType(), recipe.getFluidIngredient().get().ingredient().displayStacks().stream().map(abstractedFluidStack -> fluidHelper.create(abstractedFluidStack.fluid().builtInRegistryHolder().value(), recipe.getFluidIngredient().get().loaderAmount())).toList());
            } else {
                IIngredientAcceptor<?> recipeSlot = builder.addInvisibleIngredients(RecipeIngredientRole.INPUT);
                recipeSlot.addIngredients((IIngredientType) fluidHelper.getFluidIngredientType(), recipe.getFluidIngredient().get().ingredient().displayStacks().stream().map(abstractedFluidStack -> fluidHelper.create(abstractedFluidStack.fluid().builtInRegistryHolder().value(), recipe.getFluidIngredient().get().loaderAmount())).toList());
            }
            IRecipeSlotBuilder itemSlot = builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 5, 5);

            for (AbstractedFluidStack stack : recipe.getFluidIngredient().get().ingredient().displayStacks()) {
                ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(Minecraft.getInstance().level.registryAccess(), stack).copy();
                Optional<KegPouringRecipe> pouringRecipe = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().sorted(Comparator.comparing(KegPouringRecipe::isStrict)).filter(kegPouringRecipe -> {
                    if (kegPouringRecipe.isStrict())
                        return ItemStack.isSameItemSameTags(itemDisplay, kegPouringRecipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
                    return ItemStack.isSameItem(itemDisplay, kegPouringRecipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
                }).findFirst();
                int pourCount = pouringRecipe.map(kegPouringRecipe -> (int)(Math.min(81000L, recipe.getFluidIngredient().get().loaderAmount()) / kegPouringRecipe.getLoaderAmount())).orElse(1);
                itemDisplay.setCount(pourCount);
                if (!itemDisplay.isEmpty()) {
                    itemSlot.addItemStack(itemDisplay);
                }
            }
        }

        NonNullList<Ingredient> recipeIngredients = recipe.getIngredients();

        int borderSlotSize = 18;
        for (int row = 0; row < 2; ++row) {
            for (int column = 0; column < 2; ++column) {
                int inputIndex = row * 2 + column;
                if (inputIndex < recipeIngredients.size()) {
                    builder.addSlot(RecipeIngredientRole.INPUT, (column * borderSlotSize) + 29, (row * borderSlotSize) + 1)
                            .addItemStacks(Arrays.asList(recipeIngredients.get(inputIndex).getItems()));
                }
            }
        }

        if (recipe.getResult().left().isPresent()) {
            var result = recipe.getResult().left().get();
            // used to be config
            if (true) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, 100, 2)
                        .addFluidStack(result.fluid(), recipe.getUnit().convertToLoader(result.amount()))
                        .setFluidRenderer(81000L, false, 26, 30)
                        .setOverlay(kegOverlay, 0, 0);
            } else
                builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                        .addFluidStack(result.fluid(), recipe.getUnit().convertToLoader(result.amount()));

            ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(Minecraft.getInstance().level.registryAccess(), result).copy();
            Optional<KegPouringRecipe> pouringRecipe = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().sorted(Comparator.comparing(KegPouringRecipe::isStrict)).filter(kegPouringRecipe -> {
                if (kegPouringRecipe.isStrict())
                    return ItemStack.isSameItemSameTags(itemDisplay, kegPouringRecipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
                return ItemStack.isSameItem(itemDisplay, kegPouringRecipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
            }).findFirst();
            int pourCount = pouringRecipe.map(kegPouringRecipe -> (int)(Math.min(81000L, recipe.getFluidIngredient().get().loaderAmount()) / kegPouringRecipe.getLoaderAmount())).orElse(1);
            itemDisplay.setCount(pourCount);
            if (!itemDisplay.isEmpty())
                builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 105, 5)
                        .addItemStack(itemDisplay);
        }

        if (recipe.getCatalyst() != null) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 73, 39).addItemStack(recipe.getCatalyst());
        }
        if (recipe.getOutput() != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 39).addItemStack(recipe.getOutput());
        }

        builder.moveRecipeTransferButton(132, 43);
    }

    @Override
    public void draw(KegFermentingPouringRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 67, 10);
        leftBubble.draw(guiGraphics, 90, 3);
        rightBubble.draw(guiGraphics, 127, 3);

        if (recipe.getTemperature() <= 2) {
            chilly.draw(guiGraphics, 33, 39);
        }
        if (recipe.getTemperature() <= 1) {
            cold.draw(guiGraphics, 25, 39);
        }
        if (recipe.getTemperature() >= 4) {
            warm.draw(guiGraphics, 50, 39);
        }
        if (recipe.getTemperature() >= 5) {
            hot.draw(guiGraphics, 59, 39);
        }

        timeIcon.draw(guiGraphics, 70, 2);
        if (recipe.getExperience() > 0) {
            expIcon.draw(guiGraphics, 69, 21);
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, KegFermentingPouringRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (ClientRenderUtils.isCursorInsideBounds(67, 2, 22, 28, mouseX, mouseY)) {
            int cookTime = recipe.getFermentTime();
            if (cookTime > 0) {
                if (cookTime >= 1200)
                    tooltip.add(Component.translatable("gui.jei.category.smelting.time.minutes", cookTime / 1200));
                else
                    tooltip.add(Component.translatable("gui.jei.category.smelting.time.seconds", cookTime / 20));
            }
            float experience = recipe.getExperience();
            if (experience > 0) {
                tooltip.add(Component.translatable("gui.jei.category.smelting.experience", experience));
            }
        } else if (ClientRenderUtils.isCursorInsideBounds(24, 38, 44, 5, mouseX, mouseY)) {
            MutableComponent key = switch (recipe.getTemperature()) {
                case 1 -> BnCTextUtils.getTranslation("container.keg.cold");
                case 2 -> BnCTextUtils.getTranslation("container.keg.chilly");
                case 3 -> BnCTextUtils.getTranslation("container.keg.normal");
                case 4 -> BnCTextUtils.getTranslation("container.keg.warm");
                case 5 -> BnCTextUtils.getTranslation("container.keg.hot");
                default -> null;
            };
            if (key != null)
                tooltip.add(key);
        } else if (ClientRenderUtils.isCursorInsideBounds(92, 39, 10, 16, mouseX, mouseY)) {
            if (recipe.getCatalyst() != null) {
                tooltip.add(Component.literal(String.valueOf(recipe.getPouringAmount())).append(I18n.get("generic.unit.millibuckets")));
            }
        }
    }

}
