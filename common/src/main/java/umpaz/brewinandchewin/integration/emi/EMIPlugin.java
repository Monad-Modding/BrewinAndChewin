package umpaz.brewinandchewin.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCMenuTypes;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.integration.emi.handler.KegEmiRecipeHandler;
import umpaz.brewinandchewin.integration.emi.recipe.CheeseEmiRecipe;
import umpaz.brewinandchewin.integration.emi.recipe.FermentingEmiRecipe;
import umpaz.brewinandchewin.integration.emi.recipe.PouringEmiRecipe;

@EmiEntrypoint
public class EMIPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(BnCRecipeCategories.FERMENTING);
        registry.addCategory(BnCRecipeCategories.POURING);
        registry.addCategory(BnCRecipeCategories.AGING);

        registry.addWorkstation(BnCRecipeCategories.FERMENTING, BnCRecipeWorkstations.KEG);
        registry.addRecipeHandler(BnCMenuTypes.KEG, new KegEmiRecipeHandler());

        for (RecipeHolder<KegFermentingRecipe> recipe : registry.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.FERMENTING)) {
            if (recipe.value().getResult().left().isPresent()) {
                AbstractedFluidStack stack = recipe.value().getResult().left().get();
                registry.addRecipe(new FermentingEmiRecipe(recipe.id(), recipe.value().getIngredients().stream().map(EmiIngredient::of).toList(), getFluidItemIngredients(registry.getRecipeManager(), recipe),
                            getFluidIngredient(recipe),
                            EmiStack.of(stack.fluid(), stack.components() instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY, stack.unit().convertToLoader(stack.amount())),
                            recipe.value().getTemperature(), recipe.value().getFermentTime(), recipe.value().getExperience()));
            } else {
                registry.addRecipe(new FermentingEmiRecipe(recipe.id(), recipe.value().getIngredients().stream().map(EmiIngredient::of).toList(),
                        getFluidItemIngredients(registry.getRecipeManager(), recipe), getFluidIngredient(recipe),
                        EmiStack.of(recipe.value().getResultItem(Minecraft.getInstance().level.registryAccess())),
                        recipe.value().getTemperature(), recipe.value().getFermentTime(), recipe.value().getExperience()));
            }
        }

        for (RecipeHolder<KegPouringRecipe> recipe : registry.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().filter(pouringRecipe -> !pouringRecipe.value().hasSpecialFluid()).toList()) {
            AbstractedFluidStack stack = recipe.value().getRawFluid();
            registry.addRecipe(new PouringEmiRecipe(recipe.id(), EmiStack.of(stack.fluid(), stack.components() instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY, recipe.value().getUnit().convertToLoader(stack.amount())),
                    EmiStack.of(recipe.value().getContainer()), EmiStack.of(recipe.value().getOutput())));
        }

        registry.addRecipe(new CheeseEmiRecipe(BrewinAndChewin.asResource("/cheese/flaxen"), EmiStack.of(BnCItems.UNRIPE_FLAXEN_CHEESE_WHEEL), EmiStack.of(BnCItems.FLAXEN_CHEESE_WHEEL)));
        registry.addRecipe(new CheeseEmiRecipe(BrewinAndChewin.asResource("/cheese/scarlet"), EmiStack.of(BnCItems.UNRIPE_SCARLET_CHEESE_WHEEL), EmiStack.of(BnCItems.SCARLET_CHEESE_WHEEL)));
    }

    private EmiIngredient getFluidIngredient(RecipeHolder<KegFermentingRecipe> recipe) {
        if (recipe.value().getFluidIngredient().isEmpty())
            return null;
        long amount = recipe.value().getFluidIngredient().orElseThrow().loaderAmount();
        return EmiIngredient.of(recipe.value().getFluidIngredient().orElseThrow().ingredient().displayStacks().stream().map(stack -> EmiStack.of(stack.fluid(), stack.components() instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY, amount)).toList());
    }

    private EmiIngredient getFluidItemIngredients(RecipeManager recipes, RecipeHolder<KegFermentingRecipe> recipe) {
        if (recipe.value().getFluidIngredient().isEmpty())
            return null;
        int fluidAmount = (int)recipe.value().getFluidIngredient().orElseThrow().getUnit().convert(recipe.value().getFluidIngredient().get().amount(), FluidUnit.LITER);
        return EmiIngredient.of(recipes.getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().filter(holder -> recipe.value().getFluidIngredient().get().ingredient().matches(holder.value().getRawFluid())).map(holder -> {
            ItemStack stack = holder.value().getOutput();
            stack = stack.copyWithCount((int) (fluidAmount / holder.value().getUnit().convert(holder.value().getRawFluid().amount(), FluidUnit.LITER)));
            return (EmiIngredient)EmiStack.of(stack);
        }).toList());
    }
}
