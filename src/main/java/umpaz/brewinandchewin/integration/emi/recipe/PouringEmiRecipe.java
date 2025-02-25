package umpaz.brewinandchewin.integration.emi.recipe;

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
import umpaz.brewinandchewin.integration.emi.widget.BnCFluidWidget;

import java.util.List;
import java.util.Random;

public class PouringEmiRecipe implements KegEmiRecipe {
    public static final ResourceLocation BACKGROUND = BrewinAndChewin.asResource("textures/gui/emi/pouring.png");

    private final ResourceLocation id;
    private final EmiIngredient fluid;
    private final EmiStack container;
    private final EmiStack output;

    private static final Random RANDOM = new Random();

    public PouringEmiRecipe(ResourceLocation id, EmiIngredient fluid,
                            EmiStack container, EmiStack output) {
        this.id = id;
        this.fluid = fluid;
        this.container = container;
        this.output = output;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return BnCRecipeCategories.POURING;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(container, fluid);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth() {
        return 60;
    }

    @Override
    public int getDisplayHeight() {
        return 60;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BACKGROUND, 4, 2, 56, 56, 36, 15);

        widgets.add(new BnCFluidWidget(fluid, RANDOM.nextInt(), 31, 1));

        addSlot(widgets, container, 4, 38);
        addSlot(widgets, output, 36, 38).recipeContext(this);
    }

    private SlotWidget addSlot(WidgetHolder widgets, EmiIngredient ingredient, int x, int y) {
        return widgets.add(new SlotWidget(ingredient, x, y) {
            @Override
            public void drawBackground(GuiGraphics draw, int mouseX, int mouseY, float delta) {}
        });
    }

    @Override
    public List<EmiIngredient> getItemInputs() {
        return List.of(container);
    }

    @Override
    public EmiIngredient getFluidInput() {
        return fluid;
    }

    @Override
    public EmiIngredient getFluidItemInput() {
        return null;
    }
}
