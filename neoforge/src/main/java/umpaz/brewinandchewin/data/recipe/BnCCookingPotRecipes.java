package umpaz.brewinandchewin.data.recipe;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.tag.BnCTags;
import umpaz.brewinandchewin.data.builder.BnCCookingPotRecipeBuilder;
import vectorwing.farmersdelight.client.recipebook.CookingPotRecipeBookTab;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.CommonTags;

public class BnCCookingPotRecipes {
    public static final int FAST_COOKING = 100;      // 5 seconds
    public static final int NORMAL_COOKING = 200;    // 10 seconds
    public static final int SLOW_COOKING = 400;      // 20 seconds

    public static final float SMALL_EXP = 0.35F;
    public static final float MEDIUM_EXP = 1.0F;
    public static final float LARGE_EXP = 2.0F;

    public static void register(RecipeOutput consumer) {
        cook(consumer);
    }

    private static void cook(RecipeOutput consumer) {
        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.CHEESY_PASTA, 1, NORMAL_COOKING, MEDIUM_EXP, Items.BOWL)
                .addIngredient(BnCItems.FLAXEN_CHEESE_WEDGE)
                .addIngredient(CommonTags.Items.FOODS_PASTA)
                .addIngredient(CommonTags.Items.FOODS_TOMATO)
                .addIngredient(CommonTags.Items.FOODS_SAFE_RAW_FISH)
                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
                .unlockedByItems("has_cheese", BnCItems.FLAXEN_CHEESE_WEDGE)
                .build(consumer);
        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.CREAMY_ONION_SOUP, 1, NORMAL_COOKING, MEDIUM_EXP, Items.BOWL)
                .addIngredient(BnCTags.Items.FOOD_CHEESE_WEDGE)
                .addIngredient(CommonTags.Items.FOODS_ONION)
                .addIngredient(Tags.Items.FOODS_VEGETABLE)
                .addIngredient(Tags.Items.FOODS_BREAD)
                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
                .unlockedBy("has_cheese", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(BnCTags.Items.FOOD_CHEESE_WEDGE).build()))
                .build(consumer);
        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.FIERY_FONDUE_POT, 1, SLOW_COOKING, LARGE_EXP, Items.CAULDRON)
                .addIngredient(ModItems.TOMATO_SAUCE.get())
                .addIngredient(Tags.Items.CROPS_POTATO)
                .addIngredient(Tags.Items.DRINKS_MILK)
                .addIngredient(BnCItems.SCARLET_CHEESE_WHEEL)
                .addIngredient(ModItems.HAM.get())
                .addIngredient(Tags.Items.FOODS_BREAD)
                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
                .unlockedByItems("has_cheese", BnCItems.SCARLET_CHEESE_WHEEL)
                .build(consumer);
        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.HORROR_LASAGNA, 1, NORMAL_COOKING, MEDIUM_EXP, Items.BOWL)
                .addIngredient(BnCItems.SCARLET_CHEESE_WEDGE)
                .addIngredient(Tags.Items.CROPS_BEETROOT)
                .addIngredient(ModItems.TOMATO_SAUCE.get())
                .addIngredient(CommonTags.Items.FOODS_PASTA)
                .addIngredient(BnCTags.Items.FOOD_CHEESE_WEDGE)
                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
                .unlockedByItems("has_cheese", BnCItems.SCARLET_CHEESE_WEDGE)
                .build(consumer);
        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.SCARLET_PIEROGI, 1, NORMAL_COOKING, MEDIUM_EXP, Items.BOWL)
                .addIngredient(BnCItems.SCARLET_CHEESE_WEDGE)
                .addIngredient(Tags.Items.CROPS_POTATO)
                .addIngredient(CommonTags.Items.FOODS_DOUGH)
                .addIngredient(Items.NETHER_WART)
                .addIngredient(CommonTags.Items.CROPS_CABBAGE)
                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
                .unlockedByItems("has_cheese", BnCItems.SCARLET_CHEESE_WEDGE)
                .build(consumer);
        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.VEGETABLE_OMELET, 1, NORMAL_COOKING, MEDIUM_EXP, Items.BOWL)
                .addIngredient(BnCTags.Items.FOOD_CHEESE_WEDGE)
                .addIngredient(Tags.Items.EGGS)
                .addIngredient(Tags.Items.EGGS)
                .addIngredient(CommonTags.Items.FOODS_ONION)
                .addIngredient(Tags.Items.CROPS_CARROT)
                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
                .unlockedBy("has_cheese", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(BnCTags.Items.FOOD_CHEESE_WEDGE).build()))
                .build(consumer);


        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.SWEET_BERRY_JAM, 1, NORMAL_COOKING, MEDIUM_EXP, Items.GLASS_BOTTLE)
                .addIngredient(Items.SWEET_BERRIES)
                .addIngredient(Items.SWEET_BERRIES)
                .addIngredient(Items.SWEET_BERRIES)
                .addIngredient(Items.SUGAR)
                .setRecipeBookTab(CookingPotRecipeBookTab.MISC)
                .unlockedByItems("has_sweet_berries", Items.SWEET_BERRIES)
                .build(consumer);
        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.GLOW_BERRY_MARMALADE, 1, NORMAL_COOKING, MEDIUM_EXP, Items.GLASS_BOTTLE)
                .addIngredient(Items.GLOW_BERRIES)
                .addIngredient(Items.GLOW_BERRIES)
                .addIngredient(Items.GLOW_BERRIES)
                .addIngredient(Items.SUGAR)
                .setRecipeBookTab(CookingPotRecipeBookTab.MISC)
                .unlockedByItems("has_glow_berries", Items.GLOW_BERRIES)
                .build(consumer);
        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.APPLE_JELLY, 1, NORMAL_COOKING, MEDIUM_EXP, Items.GLASS_BOTTLE)
                .addIngredient(Items.APPLE)
                .addIngredient(Items.APPLE)
                .addIngredient(Items.APPLE)
                .addIngredient(Items.SUGAR)
                .unlockedByItems("has_apple", Items.APPLE)
                .setRecipeBookTab(CookingPotRecipeBookTab.MISC)
                .build(consumer);
    }
}
