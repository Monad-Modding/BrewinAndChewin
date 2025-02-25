package umpaz.brewinandchewin.integration.emi.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;

import java.util.List;

public interface KegEmiRecipe extends EmiRecipe {
    List<EmiIngredient> getItemInputs();
    EmiIngredient getFluidInput();
    EmiIngredient getFluidItemInput();
}
