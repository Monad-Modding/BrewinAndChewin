package umpaz.brewinandchewin.data.recipe;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import umpaz.brewinandchewin.client.recipebook.FermentingBookCategory;
import umpaz.brewinandchewin.common.registry.BnCFluids;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.tag.BnCTags;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.data.builder.KegFermentingRecipeBuilder;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.CommonTags;

public class KegFermentingRecipes {
   public static final int FAST_FERMENTING = 4800;      // 4 minutes
   public static final int NORMAL_FERMENTING = 9600;    // 8 minutes

   public static final float MEDIUM_EXP = 1.0F;
   public static final float LARGE_EXP = 2.0F;

   public static void register(RecipeOutput output, HolderLookup.Provider provider) {
      fermentingDrinks(output, provider);
      fermentingMeals(output, provider);
   }

   private static void fermentingDrinks(RecipeOutput output, HolderLookup.Provider provider) {
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.DRINKS, BnCFluids.BEER, 1000, NORMAL_FERMENTING, MEDIUM_EXP)
              .addFluidIngredient(Tags.Fluids.WATER, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Items.WHEAT)
              .addIngredient(Items.WHEAT_SEEDS)
              .addIngredient(Items.BROWN_MUSHROOM)
              .unlockedByItems("has_tankard", BnCItems.TANKARD)
              .unlockedByItems("has_wheat", Items.WHEAT)
              .build(output);
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.DRINKS, BnCFluids.VODKA, 1000, NORMAL_FERMENTING, MEDIUM_EXP)
              .addFluidIngredient(Tags.Fluids.WATER, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Tags.Items.CROPS_POTATO)
              .addIngredient(Items.WHEAT)
              .addIngredient(Items.WHEAT_SEEDS)
              .unlockedByItems("has_tankard", BnCItems.TANKARD)
              .unlockedByItems("has_potato", Items.POTATO)
              .build(output);
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.DRINKS, BnCFluids.MEAD, 1000, NORMAL_FERMENTING, MEDIUM_EXP)
              .addFluidIngredient(Tags.Fluids.HONEY, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Items.WHEAT)
              .addIngredient(Items.WHEAT_SEEDS)
              .addIngredient(Items.SWEET_BERRIES)
              .unlockedByItems("has_tankard", BnCItems.TANKARD)
              .build(output);
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.DRINKS, BnCFluids.EGG_GROG, 1000, NORMAL_FERMENTING, MEDIUM_EXP)
              .addFluidIngredient(Tags.Fluids.MILK, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Tags.Items.EGGS)
              .addIngredient(CommonTags.Items.CROPS_CABBAGE)
              .addIngredient(Items.SUGAR)
              .unlockedByItems("has_tankard", BnCItems.TANKARD)
              .build(output);
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.DRINKS, BnCFluids.STRONGROOT_ALE, 1000, FAST_FERMENTING, MEDIUM_EXP)
              .addFluidIngredient(BnCTags.Fluids.BEER, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Tags.Items.CROPS_BEETROOT)
              .addIngredient(Tags.Items.CROPS_POTATO)
              .addIngredient(Items.BROWN_MUSHROOM)
              .addIngredient(BnCItems.JERKY)
              .unlockedByItems("has_beer", BnCItems.BEER)
              .build(output);
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.DRINKS, BnCFluids.RICE_WINE, 1000, NORMAL_FERMENTING, MEDIUM_EXP)
              .addFluidIngredient(Tags.Fluids.WATER, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(CommonTags.Items.CROPS_RICE)
              .addIngredient(Items.BROWN_MUSHROOM)
              .unlockedByItems("has_tankard", BnCItems.TANKARD)
              .build(output);
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.DRINKS, BnCFluids.GLITTERING_GRENADINE, 1000, NORMAL_FERMENTING, MEDIUM_EXP, 2)
              .addFluidIngredient(Tags.Fluids.WATER, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Items.GLOW_BERRIES)
              .addIngredient(Items.GLOWSTONE_DUST)
              .addIngredient(Items.GLOW_INK_SAC)
              .unlockedByItems("has_tankard", BnCItems.TANKARD)
              .build(output);
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.DRINKS, BnCFluids.STEEL_TOE_STOUT, 1000, FAST_FERMENTING, MEDIUM_EXP, 1)
              .addFluidIngredient(BnCTags.Fluids.STRONGROOT_ALE, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Items.IRON_INGOT)
              .addIngredient(Items.CRIMSON_FUNGUS)
              .addIngredient(Items.NETHER_WART)
              .addIngredient(Items.WHEAT)
              .unlockedByItems("has_strongroot_ale", BnCItems.STRONGROOT_ALE)
              .build(output);
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.DRINKS, BnCFluids.DREAD_NOG, 1000, FAST_FERMENTING, MEDIUM_EXP, 1)
              .addFluidIngredient(BnCTags.Fluids.EGG_GROG, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Items.OMINOUS_BOTTLE)
              .addIngredient(Items.TURTLE_EGG)
              .addIngredient(Items.FERMENTED_SPIDER_EYE)
              .unlockedByItems("has_egg_grog", BnCItems.EGG_GROG)
              .build(output);
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.DRINKS, BnCFluids.SACCHARINE_RUM, 1000, FAST_FERMENTING, MEDIUM_EXP, 4)
              .addFluidIngredient(BnCTags.Fluids.MEAD, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Items.SWEET_BERRIES)
              .addIngredient(Items.SUGAR_CANE)
              .addIngredient(Items.MELON)
              .unlockedByItems("has_mead", BnCItems.MEAD)
              .build(output);
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.DRINKS, BnCFluids.PALE_JANE, 1000, FAST_FERMENTING, MEDIUM_EXP, 4)
              .addFluidIngredient(BnCTags.Fluids.RICE_WINE, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Items.HONEY_BOTTLE)
              .addIngredient(ModItems.TREE_BARK.get())
              .addIngredient(Items.LILY_OF_THE_VALLEY)
              .addIngredient(Items.SUGAR)
              .unlockedByItems("has_rice_wine", BnCItems.RICE_WINE)
              .build(output);
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.DRINKS, BnCFluids.SALTY_FOLLY, 1000, FAST_FERMENTING, MEDIUM_EXP, 2)
              .addFluidIngredient(BnCTags.Fluids.VODKA, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Items.SEA_PICKLE)
              .addIngredient(Items.DRIED_KELP)
              .addIngredient(Items.SEAGRASS)
              .unlockedByItems("has_vodka", BnCItems.VODKA)
              .build(output);
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.DRINKS, BnCFluids.BLOODY_MARY, 1000, FAST_FERMENTING, MEDIUM_EXP, 4)
              .addFluidIngredient(BnCTags.Fluids.VODKA, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(CommonTags.Items.CROPS_TOMATO)
              .addIngredient(CommonTags.Items.CROPS_CABBAGE)
              .addIngredient(Items.SWEET_BERRIES)
              .unlockedByItems("has_vodka", BnCItems.VODKA)
              .build(output);
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.DRINKS, BnCFluids.RED_RUM, 1000, FAST_FERMENTING, MEDIUM_EXP, 5)
              .addFluidIngredient(BnCTags.Fluids.BLOODY_MARY, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Items.CRIMSON_FUNGUS)
              .addIngredient(Items.NETHER_WART)
              .addIngredient(Items.FERMENTED_SPIDER_EYE)
              .addIngredient(Items.SHROOMLIGHT)
              .unlockedByItems("has_bloody_mary", BnCItems.BLOODY_MARY)
              .build(output);
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.DRINKS, BnCFluids.WITHERING_DROSS, 1000, NORMAL_FERMENTING, LARGE_EXP, 5)
              .addFluidIngredient(BnCTags.Fluids.SALTY_FOLLY, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Items.WITHER_ROSE)
              .addIngredient(Items.INK_SAC)
              .addIngredient(Items.NETHER_WART)
              .addIngredient(Items.BONE)
              .unlockedByItems("has_salty_folly", BnCItems.SALTY_FOLLY)
              .build(output);
   }

   private static void fermentingMeals(RecipeOutput output, HolderLookup.Provider provider) {
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.MEALS, BnCFluids.FLAXEN_CHEESE, 1000, NORMAL_FERMENTING, MEDIUM_EXP, 4)
              .addFluidIngredient(Tags.Fluids.MILK, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Items.BROWN_MUSHROOM)
              .addIngredient(Items.PUMPKIN_SEEDS)
              .addIngredient(Items.SUGAR)
              .unlockedByItems("has_brown_mushroom", Items.BROWN_MUSHROOM)
              .build(output);
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.MEALS, BnCFluids.SCARLET_CHEESE, 1000, NORMAL_FERMENTING, MEDIUM_EXP, 5)
              .addFluidIngredient(Tags.Fluids.MILK, 1000, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Items.CRIMSON_FUNGUS)
              .addIngredient(Items.NETHER_WART)
              .addIngredient(Items.SUGAR)
              .unlockedByItems("has_crimson_fungus", Items.CRIMSON_FUNGUS)
              .build(output);
      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.MEALS, BnCItems.JERKY, 3, NORMAL_FERMENTING, MEDIUM_EXP, 4)
              .addIngredient(BnCTags.Items.FOOD_JERKY_MEAT)
              .addIngredient(BnCTags.Items.FOOD_JERKY_MEAT)
              .addIngredient(BnCTags.Items.FOOD_JERKY_MEAT)
              .unlockedBy("has_raw_meat", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(BnCTags.Items.FOOD_JERKY_MEAT)))
              .build(output);


      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.MEALS, BnCItems.KIMCHI, 2, NORMAL_FERMENTING, MEDIUM_EXP, 4)
              .addIngredient(CommonTags.Items.CROPS_CABBAGE)
              .addIngredient(Tags.Items.FOODS_VEGETABLE)
              .addIngredient(Items.KELP)
              .unlockedByItems("has_kelp", Items.KELP)
              .build(output);

      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.MEALS, BnCItems.KIPPERS, 2, NORMAL_FERMENTING, MEDIUM_EXP, 4)
              .addIngredient(CommonTags.Items.FOODS_SAFE_RAW_FISH)
              .addIngredient(CommonTags.Items.FOODS_SAFE_RAW_FISH)
              .addIngredient(Items.DRIED_KELP)
              .unlockedBy("has_fish", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(CommonTags.Items.FOODS_SAFE_RAW_FISH).build()))
              .build(output);

      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.MEALS, BnCItems.PICKLED_PICKLES, 2, NORMAL_FERMENTING, MEDIUM_EXP, 2)
              .addFluidIngredient(Tags.Fluids.HONEY, 250, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Items.SEA_PICKLE)
              .addIngredient(Items.SEA_PICKLE)
              .addIngredient(Items.GLOW_BERRIES)
              .unlockedByItems("has_sea_pickle", Items.SEA_PICKLE)
              .build(output);

      KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.MEALS, BnCItems.COCOA_FUDGE, 1, NORMAL_FERMENTING, MEDIUM_EXP, 2)
              .addFluidIngredient(Tags.Fluids.MILK, 500, FluidUnit.MILLIBUCKET)
              .setFluidUnit(FluidUnit.MILLIBUCKET)
              .addIngredient(Items.SUGAR)
              .addIngredient(Items.COCOA_BEANS)
              .addIngredient(Items.COCOA_BEANS)
              .unlockedByItems("has_cocoa_beans", Items.COCOA_BEANS)
              .build(output);
   }

}
