package umpaz.brewinandchewin.integration.emi.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.integration.emi.BnCRecipeCategories;

import java.util.List;

public class CheeseEmiRecipe implements EmiRecipe {
    private static final ResourceLocation BACKGROUND = BrewinAndChewin.asResource("textures/gui/jei/cheese_ripening.png");

    private final ResourceLocation id;
    private final EmiStack input;
    private final EmiStack output;

    public CheeseEmiRecipe(ResourceLocation id, EmiStack input, EmiStack output) {
        this.id = id;
        this.input = input;
        this.output = output;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return BnCRecipeCategories.AGING;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth() {
        return 102;
    }

    @Override
    public int getDisplayHeight() {
        return 41;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BACKGROUND, 0, 0, 102, 41, 8, 9);

        addSlot(widgets, input, 0, 16);
        addSlot(widgets, output, 84, 16).recipeContext(this);
    }

    private SlotWidget addSlot(WidgetHolder widgets, EmiIngredient ingredient, int x, int y) {
        return widgets.add(new SlotWidget(ingredient, x, y) {
            @Override
            public void drawBackground(GuiGraphics draw, int mouseX, int mouseY, float delta) {}
        });
    }
}
