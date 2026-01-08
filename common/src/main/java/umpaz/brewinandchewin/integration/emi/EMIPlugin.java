package umpaz.brewinandchewin.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
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

        for (KegFermentingRecipe recipe : registry.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.FERMENTING)) {
            if (recipe.getResult().left().isPresent()) {
                AbstractedFluidStack stack = recipe.getResult().left().get();
                registry.addRecipe(new FermentingEmiRecipe(recipe.getId(), recipe.getIngredients().stream().map(EmiIngredient::of).toList(), getFluidItemIngredients(registry.getRecipeManager(), recipe),
                            getFluidIngredient(recipe),
                            EmiStack.of(stack.fluid(), stack.unit().convertToLoader(stack.amount())),
                            recipe.getTemperature(), recipe.getFermentTime(), recipe.getExperience()));
            } else {
                registry.addRecipe(new FermentingEmiRecipe(recipe.getId(), recipe.getIngredients().stream().map(EmiIngredient::of).toList(),
                        getFluidItemIngredients(registry.getRecipeManager(), recipe), getFluidIngredient(recipe),
                        EmiStack.of(recipe.getResultItem(Minecraft.getInstance().level.registryAccess())),
                        recipe.getTemperature(), recipe.getFermentTime(), recipe.getExperience()));
            }
        }

        for (KegPouringRecipe recipe : registry.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().filter(pouringRecipe -> !pouringRecipe.hasSpecialFluid()).toList()) {
            AbstractedFluidStack stack = recipe.getRawFluid();
            registry.addRecipe(new PouringEmiRecipe(recipe.getId(), EmiStack.of(stack.fluid(), recipe.getUnit().convertToLoader(stack.amount())), // , stack.components() instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY
                    EmiStack.of(recipe.getContainer()), EmiStack.of(recipe.getOutput())));
        }

        registry.addRecipe(new CheeseEmiRecipe(BrewinAndChewin.asResource("/cheese/flaxen"), EmiStack.of(BnCItems.UNRIPE_FLAXEN_CHEESE_WHEEL), EmiStack.of(BnCItems.FLAXEN_CHEESE_WHEEL)));
        registry.addRecipe(new CheeseEmiRecipe(BrewinAndChewin.asResource("/cheese/scarlet"), EmiStack.of(BnCItems.UNRIPE_SCARLET_CHEESE_WHEEL), EmiStack.of(BnCItems.SCARLET_CHEESE_WHEEL)));
    }

    private EmiIngredient getFluidIngredient(KegFermentingRecipe recipe) {
        if (recipe.getFluidIngredient().isEmpty())
            return null;
        return EmiIngredient.of(recipe.getFluidIngredient().orElseThrow().ingredient().displayStacks().stream().map(stack -> EmiStack.of(stack.fluid(), stack.unit().convertToLoader(stack.amount()))).toList());
    }

    private EmiIngredient getFluidItemIngredients(RecipeManager recipes, KegFermentingRecipe recipe) {
        if (recipe.getFluidIngredient().isEmpty())
            return null;
        int fluidAmount = (int)recipe.getFluidIngredient().orElseThrow().getUnit().convert(recipe.getFluidIngredient().get().amount(), FluidUnit.LITER);
        return EmiIngredient.of(recipes.getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().filter(holder -> recipe.getFluidIngredient().get().ingredient().matches(holder.getRawFluid())).map(holder -> {
            ItemStack stack = holder.getOutput();
            stack = stack.copyWithCount((int) (fluidAmount / holder.getUnit().convert(holder.getRawFluid().amount(), FluidUnit.LITER)));
            return (EmiIngredient)EmiStack.of(stack);
        }).toList());
    }
}
