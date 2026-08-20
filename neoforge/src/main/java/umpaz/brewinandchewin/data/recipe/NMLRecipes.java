package umpaz.brewinandchewin.data.recipe;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.recipebook.FermentingBookCategory;
import umpaz.brewinandchewin.common.compat.nomansland.NMLIntegration;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import net.minecraft.world.item.crafting.Ingredient;
import umpaz.brewinandchewin.data.builder.BnCCookingPotRecipeBuilder;
import umpaz.brewinandchewin.data.builder.KegFermentingRecipeBuilder;
import vectorwing.farmersdelight.client.recipebook.CookingPotRecipeBookTab;
import vectorwing.farmersdelight.common.registry.ModItems;

public class NMLRecipes {
    public static final int COLD_TIER = 1;
    public static final int BOTTLE = 250;

    public static void register(RecipeOutput output) {
        Item walnuts = item(NMLIntegration.WALNUTS);
        Item mapleSyrup = item(NMLIntegration.MAPLE_SYRUP_BOTTLE);
        Item thistle = item(NMLIntegration.THISTLE);
        if (walnuts == null || mapleSyrup == null || thistle == null) {
            BrewinAndChewin.LOG.warn("{} is not on the datagen classpath! NML recipes won't be created.", NMLIntegration.MOD_ID);
            return;
        }

        RecipeOutput conditional = output.withConditions(new ModLoadedCondition(NMLIntegration.MOD_ID));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, item(NMLIntegration.RICE_PUDDING_ITEM))
                .requires(ModItems.COOKED_RICE.get())
                .requires(BnCItems.SWEET_BERRY_JAM)
                .requires(walnuts)
                .requires(Tags.Items.DRINKS_MILK)
                .unlockedBy("has_walnuts", InventoryChangeTrigger.TriggerInstance.hasItems(walnuts))
                .save(conditional, BrewinAndChewin.asResource("rice_pudding"));

        KegFermentingRecipeBuilder.kegFermentingRecipe(FermentingBookCategory.MEALS, item(NMLIntegration.MAPLE_FUDGE_ITEM), 2, KegFermentingRecipes.MEDIUM_EXP, COLD_TIER)
                .addFluidIngredient(Tags.Fluids.MILK, BOTTLE, FluidUnit.MILLIBUCKET)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .addIngredient(Items.SUGAR)
                .addIngredient(mapleSyrup)
                .unlockedByItems("has_maple_syrup", mapleSyrup)
                .build(conditional, BrewinAndChewin.asResource("fermenting/maple_fudge_from_milk"));

        BnCCookingPotRecipeBuilder.cookingPotRecipe(BnCItems.RENNET, 1, BnCCookingPotRecipes.NORMAL_COOKING, BnCCookingPotRecipes.SMALL_EXP)
                .addIngredient(Ingredient.of(thistle), 6)
                .setRecipeBookTab(CookingPotRecipeBookTab.MISC)
                .unlockedByItems("has_thistle", thistle)
                .build(conditional, BrewinAndChewin.asResource("cooking/rennet_from_thistle"));
    }

    private static Item item(ResourceLocation id) {
        Item found = BuiltInRegistries.ITEM.get(id);
        return found == Items.AIR ? null : found;
    }
}
