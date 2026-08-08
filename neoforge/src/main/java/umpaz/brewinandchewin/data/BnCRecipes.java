package umpaz.brewinandchewin.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import umpaz.brewinandchewin.data.recipe.BnCCookingPotRecipes;
import umpaz.brewinandchewin.data.recipe.BnCCraftingRecipes;
import umpaz.brewinandchewin.data.recipe.BnCCuttingBoardRecipes;
import umpaz.brewinandchewin.data.recipe.DistillingRecipes;
import umpaz.brewinandchewin.data.recipe.KegFermentingRecipes;
import umpaz.brewinandchewin.data.recipe.KegPouringRecipes;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.concurrent.CompletableFuture;

public class BnCRecipes extends RecipeProvider
{
    public BnCRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output, HolderLookup.Provider provider) {
        KegFermentingRecipes.register(output, provider);
        KegPouringRecipes.register(output);
        DistillingRecipes.register(output);
        BnCCookingPotRecipes.register(output);
        BnCCraftingRecipes.register(output);
        BnCCuttingBoardRecipes.register(output);
    }
}