package umpaz.brewinandchewin.integration.jei;

import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * look. this is only to show #{KegPouringRecipe} and @{KegFermentingRecipe
 */
public class KegFermentingPouringRecipe extends KegFermentingRecipe{

   private final ItemStack catalyst;
   private ItemStack output;

   private final int catalystAmount;

   KegFermentingPouringRecipe( KegFermentingRecipe fermentingRecipe, @Nullable KegPouringRecipe pouringRecipe ) {
      super(fermentingRecipe.getId(), fermentingRecipe.getIngredients(), fermentingRecipe.getRecipeBookTab(), fermentingRecipe.getFluidIngredient(), fermentingRecipe.getFluidIngredientTag(), fermentingRecipe.getResultFluid(), fermentingRecipe.getResultItem(), fermentingRecipe.getAmount(), fermentingRecipe.getExperience(), fermentingRecipe.getFermentTime(), fermentingRecipe.getTemperature());
      if (fermentingRecipe.getResultItem() != null) {
         this.output = new ItemStack(fermentingRecipe.getResultItem(), fermentingRecipe.getAmount());
      }
      else if (pouringRecipe != null) {
         this.output = pouringRecipe.getOutput();
      }

      if (pouringRecipe != null) {
         this.catalyst = pouringRecipe.getContainer();
         this.catalystAmount = pouringRecipe.getAmount();
      }
      else {
         this.catalyst = null;
         this.catalystAmount = 0;
      }

   }

   public ItemStack getCatalyst(){
      return this.catalyst;
   }

   public int getCatalystAmount() {
      return this.catalystAmount;
   }


   public ItemStack getOutput(){
      return this.output;
   }
}
