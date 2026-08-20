package umpaz.brewinandchewin.data.recipe;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.crafting.Ingredient;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCItems;

public class BnCCookingRecipes {
    public static final int SMELTING_TIME = 200;
    public static final int SMOKING_TIME = 100;
    public static final int CAMPFIRE_TIME = 600;

    public static final float SMALL_EXP = 0.35F;

    public static void register(RecipeOutput consumer) {
        foodSmelting(consumer, BnCItems.RAW_CROISSANT, BnCItems.CROISSANT, "croissant");
        foodSmelting(consumer, BnCItems.RAW_SAUSAGE, BnCItems.COOKED_SAUSAGE, "cooked_sausage");
        foodSmelting(consumer, BnCItems.CORN, BnCItems.COOKED_CORN, "cooked_corn");
        foodSmelting(consumer, BnCItems.CORN_KERNELS, BnCItems.POPPED_CORN, "popped_corn");

        ovenOnly(consumer, BnCItems.CORN_DOUGH, BnCItems.CORN_BREAD, "corn_bread");
        ovenOnly(consumer, BnCItems.RAW_MUFFIN, BnCItems.CORN_MUFFIN, "corn_muffin");

        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(BnCItems.CORN_DOUGH), RecipeCategory.FOOD, BnCItems.TORTILLA, SMALL_EXP, CAMPFIRE_TIME)
                .unlockedBy("has_corn_dough", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.CORN_DOUGH))
                .save(consumer, BrewinAndChewin.asResource("tortilla_from_campfire_cooking"));
    }

    private static void ovenOnly(RecipeOutput consumer, ItemLike input, ItemLike result, String name) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.FOOD, result, SMALL_EXP, SMELTING_TIME)
                .unlockedBy("has_" + name, InventoryChangeTrigger.TriggerInstance.hasItems(input))
                .save(consumer, BrewinAndChewin.asResource(name + "_from_smelting"));
        SimpleCookingRecipeBuilder.smoking(Ingredient.of(input), RecipeCategory.FOOD, result, SMALL_EXP, SMOKING_TIME)
                .unlockedBy("has_" + name, InventoryChangeTrigger.TriggerInstance.hasItems(input))
                .save(consumer, BrewinAndChewin.asResource(name + "_from_smoking"));
    }

    private static void foodSmelting(RecipeOutput consumer, ItemLike input, ItemLike result, String name) {
        ovenOnly(consumer, input, result, name);
        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(input), RecipeCategory.FOOD, result, SMALL_EXP, CAMPFIRE_TIME)
                .unlockedBy("has_" + name, InventoryChangeTrigger.TriggerInstance.hasItems(input))
                .save(consumer, BrewinAndChewin.asResource(name + "_from_campfire_cooking"));
    }
}
