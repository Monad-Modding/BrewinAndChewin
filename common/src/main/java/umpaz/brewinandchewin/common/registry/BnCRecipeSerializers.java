package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.crafting.CreatePotionPouringRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;

public class BnCRecipeSerializers {
    public static final RecipeSerializer<?> FERMENTING = new KegFermentingRecipe.Serializer();
    public static final RecipeSerializer<?> KEG_POURING = new KegPouringRecipe.Serializer();
    public static final RecipeSerializer<?> CREATE_POTION_POURING = createCreatePotionPouringRecipe();

    public static void registerAll() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, BrewinAndChewin.asResource("fermenting"), FERMENTING);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, BrewinAndChewin.asResource("keg_pouring"), KEG_POURING);
        if (CREATE_POTION_POURING != null)
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, BrewinAndChewin.asResource("create_potion_pouring"), CREATE_POTION_POURING);
    }

    private static RecipeSerializer<?> createCreatePotionPouringRecipe() {
        if (BrewinAndChewin.getHelper().isModLoaded("create"))
            return new CreatePotionPouringRecipe.Serializer();
        return null;
    }
}
