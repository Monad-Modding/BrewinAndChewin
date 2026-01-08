package umpaz.brewinandchewin.integration.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.crafting.RecipeManager;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;


import java.util.*;

public class BnCJEIRecipes {

    private final RecipeManager recipeManager;

    public BnCJEIRecipes() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;

        if (level != null) {
            this.recipeManager = level.getRecipeManager();
        } else {
            throw new NullPointerException("minecraft world must not be null.");
        }
    }

    public List<KegFermentingPouringRecipe> getKegRecipes() {

        List<KegFermentingRecipe> ferms = recipeManager.getAllRecipesFor(BnCRecipeTypes.FERMENTING);
        List<KegPouringRecipe> pours = recipeManager.getAllRecipesFor(BnCRecipeTypes.KEG_POURING);

        List<KegFermentingPouringRecipe> kegRecipes = new ArrayList<>();

        // add all of ferms
        for (KegFermentingRecipe fermentingRecipe : ferms) {
            if (fermentingRecipe.getResult().left().isPresent()) {
                for (KegPouringRecipe pouringRecipe : pours) {
                    if (pouringRecipe.getRawFluid().matches(fermentingRecipe.getResult().left().get())) {
                        kegRecipes.add(new KegFermentingPouringRecipe(fermentingRecipe.getId(), fermentingRecipe, pouringRecipe, Minecraft.getInstance().level.registryAccess()));
                    }
                }
            }
            else {
                kegRecipes.add(new KegFermentingPouringRecipe(fermentingRecipe.getId(), fermentingRecipe, null, Minecraft.getInstance().level.registryAccess()));
            }
        }


        return kegRecipes;
    }


   public List<CheeseAgingRecipe> getCheeseRecipes() {
      List<CheeseAgingRecipe> cheese = new ArrayList<>();


      cheese.add(new CheeseAgingRecipe(BnCBlocks.UNRIPE_FLAXEN_CHEESE_WHEEL.asItem(), BnCBlocks.FLAXEN_CHEESE_WHEEL.asItem()));
      cheese.add(new CheeseAgingRecipe(BnCBlocks.UNRIPE_SCARLET_CHEESE_WHEEL.asItem(), BnCBlocks.SCARLET_CHEESE_WHEEL.asItem()));

      // find every instance of Unripe Cheese Wheel block, and call the supplier :)
      return cheese;
   }
}
