package umpaz.brewinandchewin.client.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;

import java.util.Comparator;
import java.util.Optional;

public class BnCClientRecipeUtils {
    public static ItemStack getPouredItemFromFluid(AbstractedFluidStack fluid) {
        if (fluid.isEmpty() || Minecraft.getInstance().level == null)
            return ItemStack.EMPTY;
        RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
        ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(registryAccess, fluid);

        if (!itemDisplay.isEmpty())
            return itemDisplay;

        Optional<KegPouringRecipe> recipe = Minecraft.getInstance().level.recipeAccess().getSynchronizedRecipes().getAllOfType(BnCRecipeTypes.KEG_POURING).stream().map(RecipeHolder::value).sorted(Comparator.comparing(KegPouringRecipe::isStrict)).filter(kegPouringRecipe -> kegPouringRecipe.getRawFluid().matches(fluid)).findFirst();
        return recipe.map(KegPouringRecipe::getOutput).orElse(ItemStack.EMPTY);
    }
}
