package umpaz.brewinandchewin.fabric.ingredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.stream.Stream;

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
    public Stream<Holder<Item>> items() {
        return ingredients.stream().flatMap(Ingredient::items);
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
