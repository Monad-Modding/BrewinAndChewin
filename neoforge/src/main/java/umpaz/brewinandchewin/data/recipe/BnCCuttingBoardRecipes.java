package umpaz.brewinandchewin.data.recipe;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.Ingredient;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.data.builder.BnCCuttingRecipeBuilder;
import vectorwing.farmersdelight.common.tag.CommonTags;

public class BnCCuttingBoardRecipes {
    public static void register(RecipeOutput consumer) {
        // Knife
        cuttingRecipes(consumer);

    }

    private static void cuttingRecipes(RecipeOutput consumer) {
        BnCCuttingRecipeBuilder.cuttingRecipe(Ingredient.of(BnCItems.FLAXEN_CHEESE_WHEEL), Ingredient.of(CommonTags.Items.TOOLS_KNIFE), BnCItems.FLAXEN_CHEESE_WEDGE, 4)
                .build(consumer);
        BnCCuttingRecipeBuilder.cuttingRecipe(Ingredient.of(BnCItems.SCARLET_CHEESE_WHEEL), Ingredient.of(CommonTags.Items.TOOLS_KNIFE), BnCItems.SCARLET_CHEESE_WEDGE, 4)
                .build(consumer);
        BnCCuttingRecipeBuilder.cuttingRecipe(Ingredient.of(BnCItems.QUICHE), Ingredient.of(CommonTags.Items.TOOLS_KNIFE), BnCItems.QUICHE_SLICE, 4)
                .build(consumer);
        BnCCuttingRecipeBuilder.cuttingRecipe(Ingredient.of(BnCItems.PIZZA), Ingredient.of(CommonTags.Items.TOOLS_KNIFE), BnCItems.PIZZA_SLICE, 4)
                .build(consumer);
    }
}
