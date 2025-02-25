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
        registry.addWorkstation(BnCRecipeCategories.POURING, BnCRecipeWorkstations.KEG);
        registry.addRecipeHandler(BnCMenuTypes.KEG.get(), new KegEmiRecipeHandler());

        for (KegFermentingRecipe recipe : registry.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.FERMENTING.get())) {
            if (recipe.getResultFluid() != null) {
                registry.addRecipe(new FermentingEmiRecipe(recipe.getId(), recipe.getIngredients().stream().map(EmiIngredient::of).toList(), getFluidItemIngredients(registry.getRecipeManager(), recipe),
                            getFluidIngredient(recipe),
                            EmiStack.of(recipe.getResultFluid(), recipe.getAmount()),
                            recipe.getTemperature(), recipe.getFermentTime(), recipe.getExperience()));
            } else {
                registry.addRecipe(new FermentingEmiRecipe(recipe.getId(), recipe.getIngredients().stream().map(EmiIngredient::of).toList(),
                        getFluidItemIngredients(registry.getRecipeManager(), recipe), getFluidIngredient(recipe),
                        EmiStack.of(recipe.getResultItem(Minecraft.getInstance().level.registryAccess())),
                        recipe.getTemperature(), recipe.getFermentTime(), recipe.getExperience()));
            }
        }

        for (KegPouringRecipe recipe : registry.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING.get()).stream().filter(pouringRecipe -> !pouringRecipe.hasSpecialFluid()).toList()) {
            registry.addRecipe(new PouringEmiRecipe(recipe.getId(), EmiStack.of(recipe.getRawFluid(), recipe.getAmount()),
                    EmiStack.of(recipe.getContainer()), EmiStack.of(recipe.getOutput())));
        }

        registry.addRecipe(new CheeseEmiRecipe(BrewinAndChewin.asResource("/cheese/flaxen"), EmiStack.of(BnCItems.UNRIPE_FLAXEN_CHEESE_WHEEL.get()), EmiStack.of(BnCItems.FLAXEN_CHEESE_WHEEL.get())));
        registry.addRecipe(new CheeseEmiRecipe(BrewinAndChewin.asResource("/cheese/scarlet"), EmiStack.of(BnCItems.UNRIPE_SCARLET_CHEESE_WHEEL.get()), EmiStack.of(BnCItems.SCARLET_CHEESE_WHEEL.get())));
    }

    private EmiIngredient getFluidIngredient(KegFermentingRecipe recipe) {
        if (recipe.getFluidIngredient() == null)
            return null;
        return EmiStack.of(recipe.getFluidIngredient().getFluid(), recipe.getFluidIngredient().getTag(), recipe.getFluidIngredient().getAmount());
    }

    private EmiIngredient getFluidItemIngredients(RecipeManager recipes, KegFermentingRecipe recipe) {
        if (recipe.getFluidIngredient() == null)
            return null;
        int fluidAmount = recipe.getFluidIngredient().getAmount();
        return EmiIngredient.of(recipes.getAllRecipesFor(BnCRecipeTypes.KEG_POURING.get()).stream().filter(recipe1 -> recipe.getFluidIngredient().getFluid().isSame(recipe1.getRawFluid())).map(holder -> {
            ItemStack stack = holder.getOutput();
            stack = stack.copyWithCount(fluidAmount / holder.getAmount());
            return (EmiIngredient)EmiStack.of(stack);
        }).toList());
    }
}
