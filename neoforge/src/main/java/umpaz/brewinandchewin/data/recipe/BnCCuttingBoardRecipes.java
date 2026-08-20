package umpaz.brewinandchewin.data.recipe;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.Ingredient;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.data.builder.BnCCuttingRecipeBuilder;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.CommonTags;

public class BnCCuttingBoardRecipes {
    public static void register(RecipeOutput consumer) {
        // Knife
        cuttingRecipes(consumer);

    }

    private static void cuttingRecipes(RecipeOutput consumer) {
        BnCCuttingRecipeBuilder.cuttingRecipe(Ingredient.of(BnCItems.FLAXEN_CHEESE_WHEEL), Ingredient.of(CommonTags.Items.TOOLS_KNIFE), BnCItems.FLAXEN_CHEESE_WEDGE, 8)
                .build(consumer);
        BnCCuttingRecipeBuilder.cuttingRecipe(Ingredient.of(BnCItems.SCARLET_CHEESE_WHEEL), Ingredient.of(CommonTags.Items.TOOLS_KNIFE), BnCItems.SCARLET_CHEESE_WEDGE, 8)
                .build(consumer);
        BnCCuttingRecipeBuilder.cuttingRecipe(Ingredient.of(BnCItems.QUICHE), Ingredient.of(CommonTags.Items.TOOLS_KNIFE), BnCItems.QUICHE_SLICE, 4)
                .build(consumer);
        BnCCuttingRecipeBuilder.cuttingRecipe(Ingredient.of(BnCItems.PIZZA), Ingredient.of(CommonTags.Items.TOOLS_KNIFE), BnCItems.PIZZA_SLICE, 4)
                .build(consumer);
        BnCCuttingRecipeBuilder.cuttingRecipe(Ingredient.of(BnCItems.CHOCOLATE_CAKE), Ingredient.of(CommonTags.Items.TOOLS_KNIFE), BnCItems.SLICE_OF_CHOCOLATE_CAKE, 6)
                .build(consumer);
        BnCCuttingRecipeBuilder.cuttingRecipe(Ingredient.of(BnCItems.GLOW_BERRY_MERINGUE_PIE), Ingredient.of(CommonTags.Items.TOOLS_KNIFE), BnCItems.SLICE_OF_GLOW_BERRY_MERINGUE_PIE, 4)
                .build(consumer);
        BnCCuttingRecipeBuilder.cuttingRecipe(Ingredient.of(BnCItems.PUMPKIN_ROLL), Ingredient.of(CommonTags.Items.TOOLS_KNIFE), BnCItems.SLICE_OF_PUMPKIN_ROLL, 6)
                .build(consumer);
        BnCCuttingRecipeBuilder.cuttingRecipe(Ingredient.of(BnCItems.CORN), Ingredient.of(CommonTags.Items.TOOLS_KNIFE), BnCItems.CORN_KERNELS, 1)
                .addResult(ModItems.STRAW.get())
                .addResultWithChance(BnCItems.CORN_KERNELS, 0.5F)
                .build(consumer);
        BnCCuttingRecipeBuilder.cuttingRecipe(Ingredient.of(BnCItems.CORN_KERNELS), Ingredient.of(CommonTags.Items.TOOLS_KNIFE), BnCItems.CORNMEAL, 1)
                .build(consumer);
    }
}
