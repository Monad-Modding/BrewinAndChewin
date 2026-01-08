package umpaz.brewinandchewin.integration.emi.recipe;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;
import umpaz.brewinandchewin.integration.emi.BnCRecipeCategories;
import umpaz.brewinandchewin.integration.emi.widget.BnCFluidWidget;

import java.util.*;

public class FermentingEmiRecipe implements KegEmiRecipe {
    public static final ResourceLocation BACKGROUND = BrewinAndChewin.asResource("textures/gui/emi/fermenting.png");

    private final ResourceLocation id;
    private final List<EmiIngredient> itemInputs;
    @Nullable
    private final EmiIngredient itemFluidInput;
    @Nullable
    private final EmiIngredient fluidInput;
    private final EmiStack output;
    private final int temperature;
    private final int cookTime;
    private final float experience;

    private List<EmiIngredient> inputs;

    private static final Random RANDOM = new Random();

    public FermentingEmiRecipe(ResourceLocation id, List<EmiIngredient> itemInputs,
                               @Nullable EmiIngredient itemFluidInput,
                               @Nullable EmiIngredient fluidInput, EmiStack output,
                               int temperature,
                               int cookTime, float experience) {
        this.id = id;
        this.itemInputs = itemInputs;
        this.itemFluidInput = itemFluidInput;
        this.output = output;
        this.fluidInput = fluidInput;
        this.temperature = temperature;
        this.cookTime = cookTime;
        this.experience = experience;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return BnCRecipeCategories.FERMENTING;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    public int getTemperature() {
        return temperature;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        if (inputs == null) {
            List<EmiIngredient> ingredients = new ArrayList<>(itemInputs);
            if (fluidInput != null)
                ingredients.add(fluidInput);
            inputs = List.copyOf(ingredients);
        }
        return inputs;
    }

    public List<EmiIngredient> getItemInputs() {
        return itemInputs;
    }

    @Nullable
    public EmiIngredient getFluidInput() {
        return fluidInput;
    }

    @Nullable
    public EmiIngredient getFluidItemInput() {
        return itemFluidInput;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        if (itemFluidInput != null)
            return List.of(itemFluidInput);
        return List.of();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth() {
        return 136;
    }

    @Override
    public int getDisplayHeight() {
        return 49;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BACKGROUND, 0, 0, 138, 49, 10, 11);

        int borderSlotSize = 18;
        for (int row = 0; row < 2; ++row) {
            for (int column = 0; column < 2; ++column) {
                int inputIndex = row * 2 + column;
                if (inputIndex < itemInputs.size()) {
                    addSlot(widgets, itemInputs.get(inputIndex), (column * borderSlotSize) + 30, (row * borderSlotSize) + 2);
                }
            }
        }

        if (fluidInput != null) {
            widgets.add(new BnCFluidWidget(fluidInput, RANDOM.nextInt(), 1, 3));
        }

        if (output.getKeyOfType(Fluid.class) != null) {
            widgets.add(new BnCFluidWidget(output, RANDOM.nextInt(), 101, 3)).recipeContext(this);
        } else
            addSlot(widgets, output, 106, 6).recipeContext(this);

        // Arrow
        widgets.addAnimatedTexture(BACKGROUND, 69, 12, 23, 16, 171, 4, 1000 * 10, true, false, false);

        if (temperature <= 2) {
            // Chilly
            widgets.addTexture(BACKGROUND, 35, 41, 9, 3, 178, 0);
        }
        if (temperature <= 1) {
            // Cold
            widgets.addTexture(BACKGROUND, 27, 41, 8, 3, 170, 0);
        }
        if (temperature >= 4) {
            // Warm
            widgets.addTexture(BACKGROUND, 52, 41, 9, 3, 195, 0);
        }
        if (temperature >= 5) {
            // Hot
            widgets.addTexture(BACKGROUND, 61, 41, 8, 3, 204, 0);
        }

        // Time Icon
        widgets.addTexture(BACKGROUND, 72, 4, 8, 11, 170, 21);
        // Experience Icon
        if (experience > 0) {
            widgets.addTexture(BACKGROUND, 71, 23, 9, 9, 170, 32);
        }

        widgets.addTooltip(this::getTooltips, 0, 0, widgets.getWidth(), widgets.getHeight());
    }

    private SlotWidget addSlot(WidgetHolder widgets, EmiIngredient ingredient, int x, int y) {
        return widgets.add(new SlotWidget(ingredient, x, y) {
            @Override
            public void drawBackground(GuiGraphics draw, int mouseX, int mouseY, float delta) {}
        });
    }
    public static boolean isCursorInsideBounds(double x, double y, double width, double height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public List<ClientTooltipComponent> getTooltips(double mouseX, double mouseY) {
        List<ClientTooltipComponent> tooltip = new ArrayList<>();
        if (isCursorInsideBounds(68, 2, 22, 28, mouseX, mouseY)) {
            if (cookTime > 0) {
                tooltip.add(ClientTooltipComponent.create(Component.translatable("emi.cooking.time", cookTime / 20F).getVisualOrderText()));
            }
            if (experience > 0) {
                tooltip.add(ClientTooltipComponent.create(Component.translatable("emi.cooking.experience", experience).getVisualOrderText()));
            }
        } else if (isCursorInsideBounds(26, 41, 44, 5, mouseX, mouseY)) {
            MutableComponent key = switch (temperature) {
                case 1 -> BnCTextUtils.getTranslation("container.keg.cold");
                case 2 -> BnCTextUtils.getTranslation("container.keg.chilly");
                case 3 -> BnCTextUtils.getTranslation("container.keg.normal");
                case 4 -> BnCTextUtils.getTranslation("container.keg.warm");
                case 5 -> BnCTextUtils.getTranslation("container.keg.hot");
                default -> null;
            };
            if (key != null)
                tooltip.add(ClientTooltipComponent.create(key.getVisualOrderText()));
        }
        return tooltip;
    }
}
