package umpaz.brewinandchewin.fabric.mixin.client;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.brewinandchewin.client.recipebook.BnCRecipeBookCategories;
import umpaz.brewinandchewin.common.BnCRecipeBookTypes;
import umpaz.brewinandchewin.common.registry.BnCItems;

import java.util.List;

@Mixin(RecipeBookCategories.class)
public enum RecipeBookCategoriesMixin {
    BREWINANDCHEWIN_FERMENTING_SEARCH(new ItemStack(Items.COMPASS)),
    BREWINANDCHEWIN_FERMENTING_DRINKS(new ItemStack(BnCItems.BEER)),
    BREWINANDCHEWIN_FERMENTING_MEALS(new ItemStack(BnCItems.UNRIPE_FLAXEN_CHEESE_WHEEL)),
    BREWINANDCHEWIN_FERMENTING_MISC(new ItemStack(BnCItems.KIMCHI));

    @Shadow
    private RecipeBookCategoriesMixin(ItemStack... itemIcons) {
    }

    @Inject(method = "getCategories", at = @At("HEAD"), cancellable = true)
	private static void brewinandchewin$getCustomCategories(RecipeBookType recipeBookType, CallbackInfoReturnable<List<RecipeBookCategories>> cir) {
		if (recipeBookType == BnCRecipeBookTypes.FERMENTING)
			cir.setReturnValue(List.of(BnCRecipeBookCategories.FERMENTING_SEARCH, BnCRecipeBookCategories.FERMENTING_MEALS, BnCRecipeBookCategories.FERMENTING_DRINKS));
	}
}
