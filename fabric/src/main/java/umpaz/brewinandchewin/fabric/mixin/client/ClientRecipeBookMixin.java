package umpaz.brewinandchewin.fabric.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.brewinandchewin.client.recipebook.BnCRecipeBookCategories;
import umpaz.brewinandchewin.client.recipebook.FermentingBookCategory;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookMixin {
    @Inject(method = "setupCollections", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;"))
    private void brewinandchewin$setupAggregateCategories(Iterable<Recipe<?>> iterable, RegistryAccess registryAccess, CallbackInfo ci, @Local(ordinal = 1) Map<RecipeBookCategories, List<RecipeCollection>> aggregateCategories) {
        aggregateCategories.put(BnCRecipeBookCategories.FERMENTING_SEARCH, Stream.of(BnCRecipeBookCategories.FERMENTING_MEALS, BnCRecipeBookCategories.FERMENTING_DRINKS)
                .flatMap(categories -> aggregateCategories.getOrDefault(categories, List.of()).stream())
                .toList()
        );
    }

    @Inject(method = "getCategory", at = @At(value = "INVOKE", target = "Lcom/mojang/logging/LogUtils;defer(Ljava/util/function/Supplier;)Ljava/lang/Object;", ordinal = 0), cancellable = true)
    private static void brewinandchewin$getCustomRecipeCategory(Recipe<?> recipe, CallbackInfoReturnable<RecipeBookCategories> cir) {
        if (recipe instanceof KegFermentingRecipe fermentingRecipe) {
            FermentingBookCategory tab = fermentingRecipe.getRecipeBookCategory();
            if (tab != null) {
                cir.setReturnValue(switch (tab) {
                    case MEALS -> BnCRecipeBookCategories.FERMENTING_MEALS;
                    case DRINKS -> BnCRecipeBookCategories.FERMENTING_DRINKS;
                });
            }
        }
    }
}
