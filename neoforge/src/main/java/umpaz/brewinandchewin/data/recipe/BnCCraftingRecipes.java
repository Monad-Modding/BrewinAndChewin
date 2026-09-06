package umpaz.brewinandchewin.data.recipe;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.tag.BnCTags;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.CommonTags;
import vectorwing.farmersdelight.common.tag.ModTags;

public class BnCCraftingRecipes {

    public static void register(RecipeOutput consumer) {
        recipes(consumer);

    }

    private static void recipes(RecipeOutput consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, BnCItems.TRELLIS, 4)
                .pattern("sss")
                .pattern("s s")
                .pattern("sss")
                .define('s', Items.STICK)
                .unlockedBy("has_stick", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STICK))
                .save(consumer, BrewinAndChewin.asResource("trellis"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BnCItems.RICH_CHOCOLATE_CAKE)
                .pattern("msm")
                .pattern("ccc")
                .pattern("www")
                .define('m', Tags.Items.DRINKS_MILK)
                .define('s', Items.SWEET_BERRIES)
                .define('c', BnCItems.COCOA_FUDGE)
                .define('w', Items.WHEAT)
                .unlockedBy("has_cocoa_fudge", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.COCOA_FUDGE))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BnCItems.RICH_CHOCOLATE_CAKE)
                .pattern("sss")
                .pattern("sss")
                .define('s', BnCItems.SLICE_OF_RICH_CHOCOLATE_CAKE)
                .unlockedBy("has_slice", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.SLICE_OF_RICH_CHOCOLATE_CAKE))
                .save(consumer, BrewinAndChewin.asResource("rich_chocolate_cake_from_slices"));
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BnCItems.PUMPKIN_ROLL)
                .pattern("ppp")
                .pattern("msm")
                .pattern("ddd")
                .define('p', ModItems.PUMPKIN_SLICE.get())
                .define('m', Tags.Items.DRINKS_MILK)
                .define('s', Items.SUGAR)
                .define('d', ModItems.WHEAT_DOUGH.get())
                .unlockedBy("has_pumpkin", InventoryChangeTrigger.TriggerInstance.hasItems(Items.PUMPKIN))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BnCItems.PUMPKIN_ROLL)
                .pattern("sss")
                .pattern("sss")
                .define('s', BnCItems.SLICE_OF_PUMPKIN_ROLL)
                .unlockedBy("has_slice", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.SLICE_OF_PUMPKIN_ROLL))
                .save(consumer, BrewinAndChewin.asResource("pumpkin_roll_from_slices"));
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BnCItems.GLOW_BERRY_MERINGUE_PIE)
                .pattern("ggg")
                .pattern("sss")
                .pattern("epe")
                .define('g', Items.GLOW_BERRIES)
                .define('s', Items.SUGAR)
                .define('e', Tags.Items.EGGS)
                .define('p', ModItems.PIE_CRUST.get())
                .unlockedBy("has_glow_berries", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GLOW_BERRIES))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BnCItems.GLOW_BERRY_MERINGUE_PIE)
                .pattern("ss")
                .pattern("ss")
                .define('s', BnCItems.SLICE_OF_GLOW_BERRY_MERINGUE_PIE)
                .unlockedBy("has_slice", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.SLICE_OF_GLOW_BERRY_MERINGUE_PIE))
                .save(consumer, BrewinAndChewin.asResource("glow_berry_meringue_pie_from_slices"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BnCItems.GLOW_BROWNIE, 2)
                .requires(ModItems.WHEAT_DOUGH.get())
                .requires(BnCItems.COCOA_FUDGE)
                .requires(BnCItems.GLOW_BERRY_MARMALADE)
                .unlockedBy("has_marmalade", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.GLOW_BERRY_MARMALADE))
                .save(consumer, BrewinAndChewin.asResource("glow_brownie"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BnCItems.APPLE_TURNOVER, 2)
                .requires(ModItems.WHEAT_DOUGH.get())
                .requires(BnCItems.APPLE_JELLY)
                .requires(Items.SUGAR)
                .unlockedBy("has_jelly", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.APPLE_JELLY))
                .save(consumer, BrewinAndChewin.asResource("apple_turnover"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BnCItems.JAM_SANDWICH, 2)
                .requires(Items.BREAD)
                .requires(BnCTags.Items.JAMS)
                .requires(Items.BREAD)
                .unlockedBy("has_jam", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.SWEET_BERRY_JAM))
                .save(consumer, BrewinAndChewin.asResource("jam_sandwich"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BnCItems.RAW_CROISSANT, 5)
                .pattern("ddd")
                .pattern("ded")
                .define('d', ModItems.WHEAT_DOUGH.get())
                .define('e', Tags.Items.EGGS)
                .unlockedBy("has_dough", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.WHEAT_DOUGH.get()))
                .save(consumer, BrewinAndChewin.asResource("raw_croissant"));
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BnCItems.RAW_MUFFIN, 5)
                .pattern("ddd")
                .pattern("ded")
                .define('d', BnCItems.CORN_DOUGH)
                .define('e', Tags.Items.EGGS)
                .unlockedBy("has_corn_dough", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.CORN_DOUGH))
                .save(consumer, BrewinAndChewin.asResource("raw_muffin"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BnCItems.CORN_KERNELS)
                .requires(BnCItems.CORN)
                .unlockedBy("has_corn", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.CORN))
                .save(consumer, BrewinAndChewin.asResource("corn_kernels"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BnCItems.CORN_DOUGH)
                .requires(BnCItems.CORNMEAL)
                .requires(Items.WATER_BUCKET)
                .unlockedBy("has_cornmeal", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.CORNMEAL))
                .save(consumer, BrewinAndChewin.asResource("corn_dough_from_water"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BnCItems.CORN_DOUGH)
                .requires(BnCItems.CORNMEAL)
                .requires(Tags.Items.EGGS)
                .unlockedBy("has_cornmeal", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.CORNMEAL))
                .save(consumer, BrewinAndChewin.asResource("corn_dough_from_egg"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BnCItems.HAM_AND_CHEESE_SANDWICH, 2)
                .requires(Items.BREAD)
                .requires(ModItems.SMOKED_HAM.get())
                .requires(BnCItems.FLAXEN_CHEESE_WEDGE)
                .requires(Items.BREAD)
                .unlockedBy("has_cheese", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.FLAXEN_CHEESE_WEDGE))
                .save(consumer, BrewinAndChewin.asResource("ham_and_cheese_sandwich"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BnCItems.HAM_AND_CHEESE_SANDWICH)
                .requires(Items.BREAD)
                .requires(Items.COOKED_PORKCHOP)
                .requires(Items.COOKED_PORKCHOP)
                .requires(BnCTags.Items.FOOD_CHEESE_WEDGE)
                .unlockedBy("has_cheese", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.FLAXEN_CHEESE_WEDGE))
                .save(consumer, BrewinAndChewin.asResource("ham_and_cheese_sandwich_from_pork"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BnCItems.PIZZA)
                .pattern(" f ")
                .pattern("ptp")
                .pattern("www")
                .define('w', Items.WHEAT)
                .define('t', ModItems.TOMATO_SAUCE.get())
                .define('p', BnCTags.Items.FOOD_PIZZA_TOPPING)
                .define('f', BnCTags.Items.FOOD_CHEESE_WEDGE)
                .unlockedBy("has_cheese", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.FLAXEN_CHEESE_WEDGE))
                .save(consumer, BrewinAndChewin.asResource("pizza"));
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BnCItems.QUICHE)
                .pattern("blb")
                .pattern("mcm")
                .pattern("eCe")
                .define('b', ModItems.COOKED_BACON.get())
                .define('l', CommonTags.Items.CROPS_CABBAGE)
                .define('m', Tags.Items.DRINKS_MILK)
                .define('c', BnCTags.Items.FOOD_CHEESE_WEDGE)
                .define('e', Tags.Items.EGGS)
                .define('C', ModItems.PIE_CRUST.get())
                .unlockedBy("has_crust", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.PIE_CRUST.get()))
                .save(consumer, BrewinAndChewin.asResource("quiche_from_bacon"));
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BnCItems.QUICHE)
                .pattern("blb")
                .pattern("mcm")
                .pattern("eCe")
                .define('b', Items.BROWN_MUSHROOM)
                .define('l', ModItems.CABBAGE_LEAF.get())
                .define('m', Tags.Items.DRINKS_MILK)
                .define('c', BnCTags.Items.FOOD_CHEESE_WEDGE)
                .define('e', Tags.Items.EGGS)
                .define('C', ModItems.PIE_CRUST.get())
                .unlockedBy("has_crust", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.PIE_CRUST.get()))
                .save(consumer, BrewinAndChewin.asResource("quiche_from_mushroom"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, BnCItems.COASTER, 4)
                .pattern("cc")
                .define('c', ModItems.CANVAS.get())
                .unlockedBy("has_canvas", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.CANVAS.get()))
                .save(consumer, BrewinAndChewin.asResource("item_coaster"));

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BnCItems.PIZZA)
                .pattern("pp")
                .pattern("pp")
                .define('p', BnCItems.PIZZA_SLICE)
                .unlockedBy("has_slice", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.PIZZA_SLICE))
                .save(consumer, BrewinAndChewin.asResource("pizza_from_slices"));
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BnCItems.FLAXEN_CHEESE_WHEEL)
                .pattern("ppp")
                .pattern("p p")
                .pattern("ppp")
                .define('p', BnCItems.FLAXEN_CHEESE_WEDGE)
                .unlockedBy("has_cheese", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.FLAXEN_CHEESE_WEDGE))
                .save(consumer, BrewinAndChewin.asResource("flaxen_cheese_wheel_from_wedges"));
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BnCItems.SCARLET_CHEESE_WHEEL)
                .pattern("ppp")
                .pattern("p p")
                .pattern("ppp")
                .define('p', BnCItems.SCARLET_CHEESE_WEDGE)
                .unlockedBy("has_cheese", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.SCARLET_CHEESE_WEDGE))
                .save(consumer, BrewinAndChewin.asResource("scarlet_cheese_wheel_from_wedges"));
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BnCItems.QUICHE)
                .pattern("pp")
                .pattern("pp")
                .define('p', BnCItems.QUICHE_SLICE)
                .unlockedBy("has_slice", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.QUICHE_SLICE))
                .save(consumer, BrewinAndChewin.asResource("quiche_from_slices"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, BnCItems.KEG)
                .pattern("ipi")
                .pattern("ihi")
                .pattern("ppp")
                .define('i', Items.IRON_INGOT)
                .define('h', Items.HONEYCOMB)
                .define('p', ItemTags.PLANKS)
                .unlockedBy("has_honeycomb", InventoryChangeTrigger.TriggerInstance.hasItems(Items.HONEYCOMB))
                .save(consumer, BrewinAndChewin.asResource("keg"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BnCItems.TANKARD, 4)
                .pattern("p p")
                .pattern("i i")
                .pattern("ppp")
                .define('i', Items.IRON_NUGGET)
                .define('p', ItemTags.PLANKS)
                .unlockedBy("has_nugget", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_NUGGET))
                .save(consumer, BrewinAndChewin.asResource("tankard"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, BnCItems.HEATING_CASK, 1)
                .pattern("sss")
                .pattern("cCc")
                .pattern("pmp")
                .define('p', ItemTags.PLANKS)
                .define('s', ItemTags.WOODEN_SLABS)
                .define('c', Items.COAL_BLOCK)
                .define('C', Items.BLAZE_POWDER)
                .define('m', Items.MAGMA_BLOCK)
                .unlockedBy("has_powder", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BLAZE_POWDER))
                .save(consumer, BrewinAndChewin.asResource("heating_cask"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, BnCItems.ICE_CRATE, 1)
                .pattern("pSp")
                .pattern("sis")
                .pattern("psp")
                .define('i', Items.PACKED_ICE)
                .define('S', Items.STRING)
                .define('p', ItemTags.PLANKS)
                .define('s', ItemTags.WOODEN_SLABS)
                .unlockedBy("has_ice", InventoryChangeTrigger.TriggerInstance.hasItems(Items.PACKED_ICE))
                .save(consumer, BrewinAndChewin.asResource("ice_crate"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, BnCItems.AGING_CASK, 1)
                .pattern("ipi")
                .pattern("php")
                .pattern("ipi")
                .define('i', Items.IRON_INGOT)
                .define('p', ItemTags.PLANKS)
                .define('h', Items.HONEYCOMB)
                .unlockedBy("has_honeycomb", InventoryChangeTrigger.TriggerInstance.hasItems(Items.HONEYCOMB))
                .save(consumer, BrewinAndChewin.asResource("aging_cask"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, BnCItems.BOTTLE_RACK, 1)
                .pattern("psp")
                .pattern("shs")
                .pattern("psp")
                .define('p', ItemTags.PLANKS)
                .define('s', ItemTags.WOODEN_SLABS)
                .define('h', Items.HONEYCOMB)
                .unlockedBy("has_honeycomb", InventoryChangeTrigger.TriggerInstance.hasItems(Items.HONEYCOMB))
                .save(consumer, BrewinAndChewin.asResource("bottle_rack"));


        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BnCItems.LABEL, 3)
                .requires(Items.PAPER)
                .requires(Items.PAPER)
                .requires(Items.PAPER)
                .requires(Items.HONEYCOMB)
                .unlockedBy("has_honeycomb", InventoryChangeTrigger.TriggerInstance.hasItems(Items.HONEYCOMB))
                .save(consumer, BrewinAndChewin.asResource("label"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BnCItems.RED_GRAPE_SEEDS, 1)
                .requires(BnCItems.RED_GRAPES)
                .unlockedBy("has_red_grapes", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.RED_GRAPES))
                .save(consumer, BrewinAndChewin.asResource("red_grape_seeds"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BnCItems.WHITE_GRAPE_SEEDS, 1)
                .requires(BnCItems.WHITE_GRAPES)
                .unlockedBy("has_white_grapes", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.WHITE_GRAPES))
                .save(consumer, BrewinAndChewin.asResource("white_grape_seeds"));
    }
}
