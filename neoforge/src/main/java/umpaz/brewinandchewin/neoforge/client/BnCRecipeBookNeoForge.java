package umpaz.brewinandchewin.neoforge.client;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.neoforged.neoforge.common.util.Lazy;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.recipebook.BnCRecipeBookCategories;
import umpaz.brewinandchewin.client.recipebook.FermentingBookCategory;
import umpaz.brewinandchewin.common.BnCRecipeBookTypes;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;

import java.util.List;

@EventBusSubscriber(modid = BrewinAndChewin.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BnCRecipeBookNeoForge {
    @SubscribeEvent
    public static void registerRecipeBooks(RegisterRecipeBookCategoriesEvent event) {
        event.registerBookCategories(BnCRecipeBookTypes.FERMENTING, ImmutableList.of(BnCRecipeBookCategories.FERMENTING_SEARCH, BnCRecipeBookCategories.FERMENTING_DRINKS, BnCRecipeBookCategories.FERMENTING_MEALS, BnCRecipeBookCategories.FERMENTING_MISC));
        event.registerAggregateCategory(BnCRecipeBookCategories.FERMENTING_SEARCH, ImmutableList.of(BnCRecipeBookCategories.FERMENTING_DRINKS, BnCRecipeBookCategories.FERMENTING_MEALS, BnCRecipeBookCategories.FERMENTING_MISC));
        event.registerRecipeCategoryFinder(BnCRecipeTypes.FERMENTING, recipe ->
        {
            if (recipe.value() instanceof KegFermentingRecipe fermentingRecipe) {
                FermentingBookCategory tab = fermentingRecipe.getRecipeBookCategory();
                if (tab != null) {
                    return switch (tab) {
                        case MEALS -> BnCRecipeBookCategories.FERMENTING_MEALS;
                        case DRINKS -> BnCRecipeBookCategories.FERMENTING_DRINKS;
                        case MISC -> BnCRecipeBookCategories.FERMENTING_MISC;
                    };
                }
            }
            return null;
        });
        event.registerRecipeCategoryFinder(BnCRecipeTypes.KEG_POURING, recipe -> RecipeBookCategories.UNKNOWN);
    }

    public static Object getSearchRecipeCategoryItemStacks(int idx, Class<?> type) {
        return Lazy.of(() -> List.of(new ItemStack(Items.COMPASS)));
    }

    public static Object getDrinksRecipeCategoryItemStacks(int idx, Class<?> type) {
        return Lazy.of(() -> List.of(new ItemStack(BnCItems.BEER)));
    }
    public static Object getMealsRecipeCategoryItemStacks(int idx, Class<?> type) {
        return Lazy.of(() -> List.of(new ItemStack(BnCItems.UNRIPE_FLAXEN_CHEESE_WHEEL)));
    }

    public static Object getMiscRecipeCategoryItemStacks(int idx, Class<?> type) {
        return Lazy.of(() -> List.of(new ItemStack(BnCItems.KIMCHI)));
    }
}
