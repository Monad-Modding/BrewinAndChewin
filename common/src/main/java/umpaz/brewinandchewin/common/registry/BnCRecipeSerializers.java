package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;

public class BnCRecipeSerializers {
    public static final RecipeSerializer<KegFermentingRecipe> FERMENTING = new RecipeSerializer<>(
            KegFermentingRecipe.Serializer.CODEC,
            KegFermentingRecipe.Serializer.STREAM_CODEC
    );
    public static final RecipeSerializer<KegPouringRecipe> KEG_POURING = new RecipeSerializer<>(
            KegPouringRecipe.Serializer.CODEC,
            KegPouringRecipe.Serializer.STREAM_CODEC
    );
    public static final RecipeSerializer<?> CREATE_POTION_POURING = null;

    public static void registerAll() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, BrewinAndChewin.asResource("fermenting"), FERMENTING);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, BrewinAndChewin.asResource("keg_pouring"), KEG_POURING);
    }
}
