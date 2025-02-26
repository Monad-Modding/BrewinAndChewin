package umpaz.brewinandchewin.integration.emi.handler;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.network.BnCNetworkHandler;
import umpaz.brewinandchewin.common.network.serverbound.EMIFillFermentingRecipeServerboundPacket;
import umpaz.brewinandchewin.common.network.serverbound.EMIFillPouringRecipeServerboundPacket;
import umpaz.brewinandchewin.integration.emi.BnCRecipeCategories;
import umpaz.brewinandchewin.integration.emi.recipe.FermentingEmiRecipe;
import umpaz.brewinandchewin.integration.emi.recipe.KegEmiRecipe;
import umpaz.brewinandchewin.integration.emi.recipe.PouringEmiRecipe;
import umpaz.brewinandchewin.integration.emi.widget.BnCFluidWidget;

import java.util.*;
import java.util.function.IntFunction;

/**
 * Code here has been modified from EMI internals.
 * <br>
 * EMI is licensed under the MIT license.
 * <a href="https://github.com/emilyploszaj/emi/blob/1.21/LICENSE">You may read the license here.</a>
 */
public class KegEmiRecipeHandler implements StandardRecipeHandler<KegMenu> {
    private static final Component WARMER_TEMPERATURE = Component.translatable("brewinandchewin.emi.warmer_temperature");
    private static final Component COOLER_TEMPERATURE = Component.translatable("brewinandchewin.emi.cooler_temperature");
    private static final Component CANT_EMPTY = Component.translatable("brewinandchewin.emi.cant_empty");
    private static final Component INCORRECT_FLUID = Component.translatable("brewinandchewin.emi.incorrect_fluid");

    public List<Slot> getInputSources(KegMenu menu) {
        List<Slot> list = Lists.newArrayList();

        for (int i = 0; i < 4; ++i) {
            list.add(menu.getSlot(i));
        }

        int invStart = 6;

        for (int i = invStart; i < invStart + 36; ++i) {
            list.add(menu.getSlot(i));
        }

        return list;
    }

    public List<Slot> getCraftingSlots(KegMenu menu) {
        List<Slot> list = Lists.newArrayList();

        for (int i = 0; i < 6; ++i) {
            list.add(menu.getSlot(i));
        }

        return list;
    }

    public Slot getOutputSlot(KegMenu menu) {
        return menu.getSlot(KegBlockEntity.OUTPUT_SLOT);
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<KegMenu> screen) {
        return new EmiPlayerInventory(getInputSources(screen.getMenu()).stream().map(slot -> EmiStack.of(slot.getItem())).toList());
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<KegMenu> context) {
        if (recipe instanceof FermentingEmiRecipe fermentingRecipe) {
            if (!KegBlockEntity.isValidTemp(context.getScreenHandler().getKegTemperature(), fermentingRecipe.getTemperature()))
                return false;
            if (!validFluidOrCanEmpty(fermentingRecipe, fermentingRecipe.getFluidInput(), fermentingRecipe.getFluidItemInput(), context))
                return false;
        } else if (recipe instanceof PouringEmiRecipe pouringRecipe) {
            if (!hasFluidOrItem(pouringRecipe.getFluidInput(), null, context))
                return false;
        }
        if (recipe instanceof KegEmiRecipe kegRecipe)
            return hasItems(kegRecipe.getItemInputs(), context);

        return false;
    }

    private static boolean hasItems(List<EmiIngredient> ingredients, EmiCraftContext<KegMenu> context) {
        Object2LongMap<EmiStack> used = new Object2LongOpenHashMap<>();

        boolean failure = false;
        root: for (EmiIngredient ingredient : ingredients) {
            if (!ingredient.isEmpty()) {
                for (EmiStack stack : ingredient.getEmiStacks()) {
                    long desired = stack.getAmount();
                    if (context.getInventory().inventory.containsKey(stack)) {
                        EmiStack identity = context.getInventory().inventory.get(stack);
                        long alreadyUsed = used.getOrDefault(identity, 0L);
                        long available = identity.getAmount() - alreadyUsed;
                        if (available >= desired) {
                            used.put(identity, desired + alreadyUsed);
                            continue root;
                        }
                    }
                }
                failure = true;
            }
        }

        return !failure;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<KegMenu> context) {
        if (recipe instanceof FermentingEmiRecipe fermentingRecipe) {
            var stacks = BnCEMIRecipeFiller.getFermentingStacks(this, fermentingRecipe, context, context.getAmount());
            if (stacks != null) {
                Minecraft.getInstance().setScreen(context.getScreen());
                BnCNetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new EMIFillFermentingRecipeServerboundPacket(
                        context.getScreenHandler(),
                        stacks
                ));
                return true;
            }
        } else if (recipe instanceof PouringEmiRecipe pouringRecipe) {
            var stacks = BnCEMIRecipeFiller.getPouringStacks(this, pouringRecipe, context, context.getAmount());
            if (stacks != null) {
                Minecraft.getInstance().setScreen(context.getScreen());
                BnCNetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new EMIFillPouringRecipeServerboundPacket(
                        context.getScreenHandler(),
                        switch (context.getDestination()) {
                            case NONE -> 0;
                            case CURSOR -> 1;
                            case INVENTORY -> 2;
                        },
                        stacks
                ));
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<KegMenu> context) {
        List<ClientTooltipComponent> components = Lists.newArrayList();
        if (recipe instanceof FermentingEmiRecipe fermentingRecipe) {
            if (!hasItems(fermentingRecipe.getItemInputs(), context))
                components.addAll(StandardRecipeHandler.super.getTooltip(recipe, context));
            if (!validFluidOrCanEmpty(fermentingRecipe, fermentingRecipe.getFluidInput(), fermentingRecipe.getFluidItemInput(), context)) {
                if (hasFluidOrItem(fermentingRecipe.getFluidInput(), fermentingRecipe.getFluidItemInput(), context))
                    components.add(ClientTooltipComponent.create(CANT_EMPTY.getVisualOrderText()));
                else
                    components.add(ClientTooltipComponent.create(INCORRECT_FLUID.getVisualOrderText()));
            }

            if (!KegBlockEntity.isValidTemp(context.getScreenHandler().getKegTemperature(), fermentingRecipe.getTemperature())) {
                if (context.getScreenHandler().getKegTemperature() < fermentingRecipe.getTemperature())
                    components.add(ClientTooltipComponent.create(WARMER_TEMPERATURE.getVisualOrderText()));
                else
                    components.add(ClientTooltipComponent.create(COOLER_TEMPERATURE.getVisualOrderText()));
            }
        } else if (recipe instanceof PouringEmiRecipe pouringRecipe) {
            if (!hasItems(pouringRecipe.getItemInputs(), context))
                components.addAll(StandardRecipeHandler.super.getTooltip(recipe, context));
            if (!hasFluidOrItem(pouringRecipe.getFluidInput(), null, context))
                components.addAll(StandardRecipeHandler.super.getTooltip(recipe, context));
        }

        return components;
    }

    @Override
    public void render(EmiRecipe recipe, EmiCraftContext<KegMenu> context, List<Widget> widgets, GuiGraphics draw) {
        renderMissing(recipe, context, widgets, draw);
    }

    private static void renderMissing(EmiRecipe recipe, EmiCraftContext<KegMenu> context, List<Widget> widgets, GuiGraphics draw) {
        RenderSystem.enableDepthTest();
        Map<EmiIngredient, Boolean> availableForCrafting = getAvailable(recipe, context);

        for(Widget w : widgets) {
            if (w instanceof SlotWidget sw) {
                EmiIngredient stack = sw.getStack();
                Bounds bounds = sw.getBounds();
                if (sw instanceof BnCFluidWidget && recipe instanceof KegEmiRecipe kegRecipe) {
                    if (sw.getRecipe() == null && !validFluidOrCanEmpty(kegRecipe, kegRecipe.getFluidInput(), kegRecipe.getFluidItemInput(), context))
                        draw.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(), 1157562368);
                } else if (sw.getRecipe() == null && availableForCrafting.containsKey(stack) && !stack.isEmpty() && !availableForCrafting.get(stack)) {
                    draw.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(), 1157562368);
                }
            }
        }

        if (recipe instanceof FermentingEmiRecipe fermentingRecipe) {
            if (!KegBlockEntity.isValidTemp(context.getScreenHandler().getKegTemperature(), fermentingRecipe.getTemperature()))
                draw.fill(26, 41, 70, 45, 1157562368);
        }
    }

    private static Map<EmiIngredient, Boolean> getAvailable(EmiRecipe recipe, EmiCraftContext<KegMenu> context) {
        Map<EmiIngredient, Boolean> availableForCrafting = new IdentityHashMap<>();
        if (recipe instanceof KegEmiRecipe kegRecipe) {
            List<Boolean> list = getCraftAvailability(kegRecipe, context);
            List<EmiIngredient> inputs = recipe.getInputs();
            if (list.size() != inputs.size()) {
                return Map.of();
            } else {
                for(int i = 0; i < list.size(); ++i) {
                    availableForCrafting.put(inputs.get(i), list.get(i));
                }

                return availableForCrafting;
            }
        }
        return Collections.emptyMap();
    }


    private static boolean validFluidOrCanEmpty(KegEmiRecipe recipe, @Nullable EmiIngredient fluidIngredient, @Nullable EmiIngredient fluidItemIngredient, EmiCraftContext<KegMenu> context) {
        boolean success = false;
        Object2LongMap<EmiStack> used = new Object2LongOpenHashMap<>();

        EmiIngredient emptyingIngredient = getEmptyingIngredient(recipe, context);

        if (emptyingIngredient == null)
            return hasFluidOrItem(fluidIngredient, fluidItemIngredient, context);

        for (EmiStack stack : emptyingIngredient.getEmiStacks()) {
            long desired = stack.getAmount();
            if (context.getInventory().inventory.containsKey(stack)) {
                EmiStack identity = context.getInventory().inventory.get(stack);
                long alreadyUsed = used.getOrDefault(identity, 0L);
                long available = identity.getAmount() - alreadyUsed;
                if (available >= desired) {
                    used.put(identity, desired + alreadyUsed);
                    success = true;
                    break;
                }
            }
        }

        return success && hasFluidOrItem(fluidIngredient, fluidItemIngredient, context);
    }

    private static boolean hasFluidOrItem(@Nullable EmiIngredient fluidIngredient, @Nullable EmiIngredient fluidItemIngredient, EmiCraftContext<KegMenu> context) {
        FluidStack stack = context.getScreenHandler().kegTank.getFluid();
        return (fluidIngredient == null && stack.isEmpty() || fluidIngredient != null && fluidIngredient.getEmiStacks().stream().anyMatch(emiStack -> emiStack.isEqual(EmiStack.of(stack.getFluid(), stack.getTag(), stack.getAmount())))) ||
                (fluidItemIngredient != null && hasItems(List.of(fluidItemIngredient), context));
    }


    @Nullable
    public static EmiIngredient getEmptyingIngredient(KegEmiRecipe kegRecipe, EmiCraftContext<KegMenu> context) {
        if (context.getScreenHandler().kegTank.isEmpty() || kegRecipe.getFluidInput() == null || kegRecipe.getFluidInput().getEmiStacks().stream().anyMatch(emiStack -> {
            FluidStack stack = context.getScreenHandler().kegTank.getFluid();
            EmiStack tankEmiStack = EmiStack.of(
                    stack.getFluid(),
                    stack.getTag(),
                    stack.getAmount() / kegRecipe.getFluidInput().getAmount()
            );
            return emiStack.isEqual(tankEmiStack);
        }))
            return null;

        FluidStack stack = context.getScreenHandler().kegTank.getFluid();
        List<EmiIngredient> ingredients = new ArrayList<>(EmiApi.getRecipeManager().getRecipes(BnCRecipeCategories.POURING).stream()
                .filter(recipe -> {
                    if (!(recipe instanceof PouringEmiRecipe pouringRecipe))
                        return false;
                    EmiStack tankEmiStack = EmiStack.of(
                            stack.getFluid(),
                            stack.getTag(),
                            stack.getAmount() / pouringRecipe.getFluidInput().getAmount()
                    );
                    return pouringRecipe.getFluidInput().getEmiStacks().get(0).isEqual(tankEmiStack);
                }).map(recipe -> {
                    PouringEmiRecipe pouringRecipe = (PouringEmiRecipe) recipe;
                    return ((PouringEmiRecipe)recipe).getItemInputs().get(0).copy().setAmount(stack.getAmount() / pouringRecipe.getFluidInput().getAmount());
                }).toList());

        if (ingredients.isEmpty())
            return null;

        return EmiIngredient.of(ingredients);
    }

    private static List<Boolean> getCraftAvailability(KegEmiRecipe recipe, EmiCraftContext<KegMenu> context) {
        Object2LongMap<EmiStack> used = new Object2LongOpenHashMap<>();
        List<Boolean> states = Lists.newArrayList();

        root: for (EmiIngredient ingredient : recipe.getItemInputs()) {
            for (EmiStack stack : ingredient.getEmiStacks()) {
                long desired = stack.getAmount();
                if (context.getInventory().inventory.containsKey(stack)) {
                    EmiStack identity = context.getInventory().inventory.get(stack);
                    long alreadyUsed = used.getOrDefault(identity, 0L);
                    long available = identity.getAmount() - alreadyUsed;
                    if (available >= desired) {
                        used.put(identity, desired + alreadyUsed);
                        states.add(true);
                        continue root;
                    }
                }
            }

            states.add(false);
        }


        if (recipe.getFluidItemInput() != null && context.getScreenHandler().kegTank.isEmpty()) {
            boolean success = false;
            for (EmiStack stack : recipe.getFluidItemInput().getEmiStacks()) {
                long desired = stack.getAmount();
                if (context.getInventory().inventory.containsKey(stack)) {
                    EmiStack identity = context.getInventory().inventory.get(stack);
                    long alreadyUsed = used.getOrDefault(identity, 0L);
                    long available = identity.getAmount() - alreadyUsed;
                    if (available >= desired) {
                        used.put(identity, desired + alreadyUsed);
                        success = true;
                        break;
                    }
                }
            }
            states.add(success);
        } else if (recipe.getFluidInput() != null) {
            boolean success = false;
            for (EmiStack stack : recipe.getFluidInput().getEmiStacks()) {
                long desired = stack.getAmount();
                FluidStack tankStack = context.getScreenHandler().kegTank.getFluidInTank(0);
                EmiStack tankEmiStack = EmiStack.of(tankStack.getFluid(), tankStack.getTag(), tankStack.getAmount());
                if (tankStack.getAmount() >= desired && stack.isEqual(tankEmiStack)) {
                    success = true;
                    break;
                }
            }
            states.add(success);
        }

        return states;
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return (recipe.getCategory() == BnCRecipeCategories.FERMENTING || recipe.getCategory() == BnCRecipeCategories.POURING) && recipe.supportsRecipeTree();
    }

    public enum InputType {
        ITEM,
        FILL,
        EMPTY;

        public static final IntFunction<InputType> BY_ID = ByIdMap.continuous(InputType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    }
}
