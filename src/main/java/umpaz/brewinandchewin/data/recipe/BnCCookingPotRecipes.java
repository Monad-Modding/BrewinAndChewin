package umpaz.brewinandchewin.data.recipe;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.tag.BnCTags;
import umpaz.brewinandchewin.data.builder.BnCCookingPotRecipeBuilder;
import vectorwing.farmersdelight.client.recipebook.CookingPotRecipeBookTab;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.ForgeTags;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.function.Consumer;

public class BnCCookingPotRecipes {
    public static final int FAST_COOKING = 100;      // 5 seconds
    public static final int NORMAL_COOKING = 200;    // 10 seconds
    public static final int SLOW_COOKING = 400;      // 20 seconds

    public static final float SMALL_EXP = 0.35F;
    public static final float MEDIUM_EXP = 1.0F;
    public static final float LARGE_EXP = 2.0F;

    public static void register(Consumer<FinishedRecipe> consumer) {
        cook(consumer);
    }

    private static void cook(Consumer<FinishedRecipe> consumer) {
        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.CHEESY_PASTA.get(), 1, NORMAL_COOKING, MEDIUM_EXP, Items.BOWL)
                .addIngredient(BnCItems.FLAXEN_CHEESE_WEDGE.get())
                .addIngredient(ForgeTags.PASTA_RAW_PASTA)
                .addIngredient(ForgeTags.VEGETABLES_TOMATO)
                .addIngredient(ForgeTags.RAW_FISHES)
                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
                .unlockedByItems("has_cheese", BnCItems.FLAXEN_CHEESE_WEDGE.get())
                .build(consumer);
        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.CREAMY_ONION_SOUP.get(), 1, NORMAL_COOKING, MEDIUM_EXP, Items.BOWL)
                .addIngredient(BnCTags.CHEESE_WEDGES)
                .addIngredient(ForgeTags.VEGETABLES_ONION)
                .addIngredient(ForgeTags.VEGETABLES)
                .addIngredient(ForgeTags.BREAD)
                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
                .unlockedBy("has_cheese", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(BnCTags.CHEESE_WEDGES).build()))
                .build(consumer);
        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.FIERY_FONDUE_POT.get(), 1, SLOW_COOKING, LARGE_EXP, Items.CAULDRON)
                .addIngredient(ModItems.TOMATO_SAUCE.get())
                .addIngredient(ForgeTags.VEGETABLES_POTATO)
                .addIngredient(ForgeTags.MILK)
                .addIngredient(BnCItems.SCARLET_CHEESE_WHEEL.get())
                .addIngredient(ModItems.HAM.get())
                .addIngredient(ForgeTags.BREAD)
                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
                .unlockedByItems("has_cheese", BnCItems.SCARLET_CHEESE_WHEEL.get())
                .build(consumer);
        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.HORROR_LASAGNA.get(), 1, NORMAL_COOKING, MEDIUM_EXP, Items.BOWL)
                .addIngredient(BnCItems.SCARLET_CHEESE_WEDGE.get())
                .addIngredient(ForgeTags.VEGETABLES_BEETROOT)
                .addIngredient(ModItems.TOMATO_SAUCE.get())
                .addIngredient(ForgeTags.PASTA_RAW_PASTA)
                .addIngredient(BnCTags.HORROR_MEATS)
                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
                .unlockedByItems("has_cheese", BnCItems.SCARLET_CHEESE_WEDGE.get())
                .build(consumer);
        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.SCARLET_PIEROGI.get(), 1, NORMAL_COOKING, MEDIUM_EXP, Items.BOWL)
                .addIngredient(BnCItems.SCARLET_CHEESE_WEDGE.get())
                .addIngredient(ForgeTags.VEGETABLES_POTATO)
                .addIngredient(ForgeTags.DOUGH)
                .addIngredient(Items.NETHER_WART)
                .addIngredient(ModTags.CABBAGE_ROLL_INGREDIENTS)
                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
                .unlockedByItems("has_cheese", BnCItems.SCARLET_CHEESE_WEDGE.get())
                .build(consumer);
        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.VEGETABLE_OMELET.get(), 1, NORMAL_COOKING, MEDIUM_EXP, Items.BOWL)
                .addIngredient(BnCTags.CHEESE_WEDGES)
                .addIngredient(ForgeTags.EGGS)
                .addIngredient(ForgeTags.EGGS)
                .addIngredient(ForgeTags.VEGETABLES_ONION)
                .addIngredient(ForgeTags.VEGETABLES_CARROT)
                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
                .unlockedBy("has_cheese", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(BnCTags.CHEESE_WEDGES).build()))
                .build(consumer);


        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.SWEET_BERRY_JAM.get(), 1, NORMAL_COOKING, MEDIUM_EXP, Items.GLASS_BOTTLE)
                .addIngredient(Items.SWEET_BERRIES)
                .addIngredient(Items.SWEET_BERRIES)
                .addIngredient(Items.SWEET_BERRIES)
                .addIngredient(Items.SUGAR)
                .setRecipeBookTab(CookingPotRecipeBookTab.MISC)
                .unlockedByItems("has_sweet_berries", Items.SWEET_BERRIES)
                .build(consumer);
        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.GLOW_BERRY_MARMALADE.get(), 1, NORMAL_COOKING, MEDIUM_EXP, Items.GLASS_BOTTLE)
                .addIngredient(Items.GLOW_BERRIES)
                .addIngredient(Items.GLOW_BERRIES)
                .addIngredient(Items.GLOW_BERRIES)
                .addIngredient(Items.SUGAR)
                .setRecipeBookTab(CookingPotRecipeBookTab.MISC)
                .unlockedByItems("has_glow_berries", Items.GLOW_BERRIES)
                .build(consumer);
        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.APPLE_JELLY.get(), 1, NORMAL_COOKING, MEDIUM_EXP, Items.GLASS_BOTTLE)
                .addIngredient(Items.APPLE)
                .addIngredient(Items.APPLE)
                .addIngredient(Items.APPLE)
                .addIngredient(Items.SUGAR)
                .unlockedByItems("has_apple", Items.APPLE)
                .setRecipeBookTab(CookingPotRecipeBookTab.MISC)
                .build(consumer);
    }
}
