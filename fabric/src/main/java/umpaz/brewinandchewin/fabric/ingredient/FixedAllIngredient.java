package umpaz.brewinandchewin.fabric.ingredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A copy of the `fabric:all` custom ingredient with matching stacks fixed to display multiple ingredients' stacks.
 */
public class FixedAllIngredient implements CustomIngredient {
    protected final List<Ingredient> ingredients;

    public FixedAllIngredient(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public boolean test(ItemStack stack) {
        for (Ingredient ingredient : ingredients) {
            if (!ingredient.test(stack)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public List<ItemStack> getMatchingStacks() {
        List<ItemStack> previewStacks = new ArrayList<>(Arrays.asList(ingredients.getFirst().getItems()));

        for (int i = 1; i < ingredients.size(); ++i) {
            Ingredient ing = ingredients.get(i);
            // The fix.
            previewStacks.addAll(Arrays.stream(ing.getItems()).toList());
        }

        return previewStacks;
    }

    @Override
    public boolean requiresTesting() {
        for (Ingredient ingredient : ingredients) {
            if (ingredient.requiresTesting()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        throw new UnsupportedOperationException("FixedAllIngredient is not registered, so no serializer is necessary.");
    }
}
