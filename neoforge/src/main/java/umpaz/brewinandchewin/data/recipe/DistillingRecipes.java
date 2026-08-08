package umpaz.brewinandchewin.data.recipe;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.tag.BnCTags;
import umpaz.brewinandchewin.data.builder.DistillingRecipeBuilder;

public class DistillingRecipes {
    public static final int FAST_DISTILLING = 200;
    public static final int NORMAL_DISTILLING = 400;
    public static final int SLOW_DISTILLING = 800;

    public static final float SMALL_EXP = 0.35F;
    public static final float MEDIUM_EXP = 1.0F;

    public static void register(RecipeOutput output) {
        DistillingRecipeBuilder.distilling(BnCTags.Items.WINES, BnCItems.BRANDY, NORMAL_DISTILLING, 2, MEDIUM_EXP)
                .unlockedByItems("has_wine", BnCItems.RED_WINE)
                .build(output);
        DistillingRecipeBuilder.distilling(BnCItems.BRANDY, BnCItems.AQUA_VITAE, SLOW_DISTILLING, 3, MEDIUM_EXP)
                .unlockedByItems("has_brandy", BnCItems.BRANDY)
                .build(output);
        DistillingRecipeBuilder.distilling(BnCItems.VODKA, BnCItems.AQUA_VITAE, SLOW_DISTILLING, 3, MEDIUM_EXP)
                .unlockedByItems("has_vodka", BnCItems.VODKA)
                .build(output, umpaz.brewinandchewin.BrewinAndChewin.asResource("distilling/aqua_vitae_from_vodka"));
        DistillingRecipeBuilder.distilling(Items.FERMENTED_SPIDER_EYE, BnCItems.SICKENING_TINCTURE, FAST_DISTILLING, 1, SMALL_EXP)
                .unlockedByItems("has_fermented_spider_eye", Items.FERMENTED_SPIDER_EYE)
                .build(output);
        DistillingRecipeBuilder.distilling(Items.HONEY_BOTTLE, BnCItems.DELICIOUS_TINCTURE, FAST_DISTILLING, 1, SMALL_EXP)
                .unlockedByItems("has_honey_bottle", Items.HONEY_BOTTLE)
                .build(output);
    }
}
