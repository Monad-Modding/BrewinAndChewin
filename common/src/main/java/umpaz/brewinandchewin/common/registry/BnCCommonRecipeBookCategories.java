package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import umpaz.brewinandchewin.BrewinAndChewin;

public class BnCCommonRecipeBookCategories {
    public static final RecipeBookCategory FERMENTING_MEALS = new RecipeBookCategory();
    public static final RecipeBookCategory FERMENTING_DRINKS = new RecipeBookCategory();
    public static final RecipeBookCategory FERMENTING_MISC = new RecipeBookCategory();
    public static final RecipeBookCategory KEG_POURING = new RecipeBookCategory();

    public static void registerAll() {
        Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, BrewinAndChewin.asResource("fermenting_meals"), FERMENTING_MEALS);
        Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, BrewinAndChewin.asResource("fermenting_drinks"), FERMENTING_DRINKS);
        Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, BrewinAndChewin.asResource("fermenting_misc"), FERMENTING_MISC);
        Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, BrewinAndChewin.asResource("keg_pouring"), KEG_POURING);
    }
}
