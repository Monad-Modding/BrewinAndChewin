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
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, BnCItems.HAM_AND_CHEESE_SANDWICH, 2)
                .requires(Items.BREAD)
                .requires(ModItems.SMOKED_HAM.get())
                .requires(BnCItems.FLAXEN_CHEESE_WEDGE)
                .requires(Items.BREAD)
                .unlockedBy("has_cheese", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.FLAXEN_CHEESE_WEDGE))
                .save(consumer, BrewinAndChewin.asResource("ham_and_cheese_sandwich"));

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
                .define('e', Items.EGG)
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
                .define('e', Items.EGG)
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
                .pattern("pp")
                .pattern("pp")
                .define('p', BnCItems.FLAXEN_CHEESE_WEDGE)
                .unlockedBy("has_cheese", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.FLAXEN_CHEESE_WEDGE))
                .save(consumer, BrewinAndChewin.asResource("flaxen_cheese_wheel_from_wedges"));
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, BnCItems.SCARLET_CHEESE_WHEEL)
                .pattern("pp")
                .pattern("pp")
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

    }
}
