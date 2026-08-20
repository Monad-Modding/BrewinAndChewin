package umpaz.brewinandchewin.data.recipe;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.common.registry.BnCFluids;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.data.builder.KegPouringRecipeBuilder;

public class KegPouringRecipes {

    public static void register(RecipeOutput consumer) {
        cookMiscellaneous(consumer);
    }

    private static void cookMiscellaneous(RecipeOutput consumer) {
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.RED_WINE, 250, BnCItems.RED_WINE)
                .withContainer(Items.GLASS_BOTTLE)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.WHITE_WINE, 250, BnCItems.WHITE_WINE)
                .withContainer(Items.GLASS_BOTTLE)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.CURRANT_WINE, 250, BnCItems.CURRANT_WINE)
                .withContainer(Items.GLASS_BOTTLE)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.VERRUCA_WINE, 250, BnCItems.VERRUCA_WINE)
                .withContainer(Items.GLASS_BOTTLE)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.TWISTED_WINE, 250, BnCItems.TWISTED_WINE)
                .withContainer(Items.GLASS_BOTTLE)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.RICE_WINE, 250, BnCItems.RICE_WINE)
                .withContainer(Items.GLASS_BOTTLE)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.BEER, 250, BnCItems.BEER)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.VODKA, 250, BnCItems.VODKA)
                .withContainer(Items.GLASS_BOTTLE)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.MEAD, 250, BnCItems.MEAD)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.EGG_GROG, 250, BnCItems.EGG_GROG)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.STRONGROOT_ALE, 250, BnCItems.STRONGROOT_ALE)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.SACCHARINE_RUM, 250, BnCItems.SACCHARINE_RUM)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.PALE_JANE, 250, BnCItems.PALE_JANE)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.SALTY_FOLLY, 250, BnCItems.SALTY_FOLLY)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.STEEL_TOE_STOUT, 250, BnCItems.STEEL_TOE_STOUT)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.GLITTERING_GRENADINE, 250, BnCItems.GLITTERING_GRENADINE)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.BLOODY_MARY, 250, BnCItems.BLOODY_MARY)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.RED_RUM, 250, BnCItems.RED_RUM)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.WITHERING_DROSS, 250, BnCItems.WITHERING_DROSS)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.DREAD_NOG, 250, BnCItems.DREAD_NOG)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.KOMBUCHA, 250, BnCItems.KOMBUCHA)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);

        /* Separated into loader impl.
        KegPouringRecipeBuilder.kegPouringRecipe(NeoForgeMod.MILK.get(), 250, ModItems.MILK_BOTTLE.get())
                .setFluidUnit(FluidUnit.MILLIBUCKETS)
                .build(consumer);
         */
        KegPouringRecipeBuilder.kegPouringRecipe(Fluids.WATER, 250, Items.POTION.getDefaultInstance(), true)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .withContainer(Items.GLASS_BOTTLE)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.HONEY, 250, Items.HONEY_BOTTLE)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(Fluids.WATER, 1000, Items.WATER_BUCKET)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .build(consumer);
        /* Separated into loader impl.
        KegPouringRecipeBuilder.kegPouringRecipe(NeoForgeMod.MILK.get(), 1000, Items.MILK_BUCKET)
                .setFluidUnit(FluidUnit.MILLIBUCKETS)
                .build(consumer);
         */

        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.FLAXEN_CHEESE, 1000, BnCItems.UNRIPE_FLAXEN_CHEESE_WHEEL, false)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .withContainer(Items.HONEYCOMB)
                .build(consumer);
        KegPouringRecipeBuilder.kegPouringRecipe(BnCFluids.SCARLET_CHEESE, 1000, BnCItems.UNRIPE_SCARLET_CHEESE_WHEEL, false)
                .setFluidUnit(FluidUnit.MILLIBUCKET)
                .withContainer(Items.HONEYCOMB)
                .build(consumer);
    }
}
