package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;

public class BnCRecipeTypes {
    public static final RecipeType<KegFermentingRecipe> FERMENTING = registerRecipeType("fermenting");
    public static final RecipeType<KegPouringRecipe> KEG_POURING = registerRecipeType("keg_pouring");

    public static <T extends Recipe<?>> RecipeType<T> registerRecipeType(final String identifier) {
        return new RecipeType<>()
        {
            public String toString() {
                return BrewinAndChewin.MODID + ":" + identifier;
            }
        };
    }

    public static void registerAll() {
        Registry.register(BuiltInRegistries.RECIPE_TYPE, BrewinAndChewin.asResource("fermenting"), FERMENTING);
        Registry.register(BuiltInRegistries.RECIPE_TYPE, BrewinAndChewin.asResource("keg_pouring"), KEG_POURING);
    }
}
