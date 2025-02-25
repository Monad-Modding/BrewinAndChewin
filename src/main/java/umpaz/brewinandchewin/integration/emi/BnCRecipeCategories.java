package umpaz.brewinandchewin.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.resources.ResourceLocation;
import umpaz.brewinandchewin.BrewinAndChewin;

public class BnCRecipeCategories {
    private static final ResourceLocation SIMPLIFIED_TEXTURES = BrewinAndChewin.asResource("textures/gui/emi/simplified.png");

    public static final EmiRecipeCategory FERMENTING = new EmiRecipeCategory(BrewinAndChewin.asResource("fermenting"), BnCRecipeWorkstations.KEG, simplifiedRenderer(0, 0));
    public static final EmiRecipeCategory POURING = new EmiRecipeCategory(BrewinAndChewin.asResource("pouring"), BnCRecipeWorkstations.KEG, simplifiedRenderer(16, 0));
    public static final EmiRecipeCategory AGING = new EmiRecipeCategory(BrewinAndChewin.asResource("aging"), BnCRecipeWorkstations.AGING, simplifiedRenderer(32, 0));

    private static EmiRenderable simplifiedRenderer(int u, int v) {
        return (draw, x, y, delta) ->
                draw.blit(SIMPLIFIED_TEXTURES, x, y, u, v, 16, 16, 48, 16);
    }
}
