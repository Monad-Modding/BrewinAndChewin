/**
 * The MIT License (MIT)
 * <br>
 * Copyright (c) 2022 Emi
 * <br>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <br>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <br>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package umpaz.brewinandchewin.integration.emi.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.integration.emi.BnCRecipeCategories;
import umpaz.brewinandchewin.integration.emi.recipe.FermentingEmiRecipe;
import umpaz.brewinandchewin.integration.emi.recipe.KegEmiRecipe;
import umpaz.brewinandchewin.integration.emi.recipe.PouringEmiRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Code here has been modified from EMI internals.
 */
public class BnCEMIRecipeFiller {
    public static @Nullable Map<KegEmiRecipeHandler.InputType, List<ItemStack>> getFermentingStacks(KegEmiRecipeHandler handler, FermentingEmiRecipe recipe, EmiCraftContext<KegMenu> context, int amount) {
        try {
            KegMenu menu = context.getScreen().getMenu();
            if (handler != null) {
                Map<KegEmiRecipeHandler.InputType, List<ItemStack>> desired = Maps.newHashMap();
                List<Slot> slots = handler.getInputSources(menu);
                List<Slot> craftingSlots = handler.getCraftingSlots(menu);
                List<EmiIngredient> itemIngredients = recipe.getItemInputs();
                EmiIngredient fluidIngredient = recipe.getFluidInput();
                EmiIngredient fluidItemIngredient = recipe.getFluidItemInput();
                EmiIngredient emptyingIngredient = KegEmiRecipeHandler.getEmptyingIngredient(recipe, context);
                if (!handleItemInputs(
                        handler, recipe, context.getScreen(), amount,
                        desired, itemIngredients, slots, craftingSlots
                ))
                    return null;
                if (fluidItemIngredient != null && !handleFillingInputs(
                        handler, recipe, context, amount,
                        desired, fluidIngredient, fluidItemIngredient, slots
                ))
                    return null;
                if (emptyingIngredient != null && !context.getScreen().getMenu().kegTank.isEmpty() && !handleEmptyingInputs(
                        handler, recipe, context, amount,
                        desired, emptyingIngredient, slots
                ))
                    return null;
                return desired;
            }
        } catch (Exception ex) {
            BrewinAndChewin.LOG.error("Failed to get fermenting recipe stacks. ", ex);
        }
        return null;
    }

    public static @Nullable List<ItemStack> getPouringStacks(KegEmiRecipeHandler handler, PouringEmiRecipe recipe, EmiCraftContext<KegMenu> context, int amount) {
        try {
            KegMenu menu = context.getScreen().getMenu();
            if (handler != null) {
                Map<KegEmiRecipeHandler.InputType, List<ItemStack>> desired = Maps.newHashMap();
                List<Slot> slots = handler.getInputSources(menu);
                EmiIngredient emptyingIngredient = recipe.getItemInputs().get(0);
                if (emptyingIngredient != null && !context.getScreen().getMenu().kegTank.isEmpty() && !handleEmptyingInputs(
                        handler, recipe, context, amount,
                        desired, emptyingIngredient, slots
                ))
                    return null;
                return desired.get(KegEmiRecipeHandler.InputType.EMPTY);
            }
        } catch (Exception ex) {
            BrewinAndChewin.LOG.error("Failed to get fermenting recipe stacks. ", ex);
        }
        return null;
    }

    private static boolean handleItemInputs(
            KegEmiRecipeHandler handler, EmiRecipe recipe, AbstractContainerScreen<KegMenu> screen, int amount,
            Map<KegEmiRecipeHandler.InputType, List<ItemStack>> desired, List<EmiIngredient> ingredients, List<Slot> slots, List<Slot> craftingSlots) {
        Object2IntMap<EmiStack> weightDivider = new Object2IntOpenHashMap<>();
        List<DiscoveredItem> discovered = Lists.newArrayList();

        for (int i = 0; i < ingredients.size(); i++) {
            List<DiscoveredItem> d = Lists.newArrayList();
            EmiIngredient ingredient = ingredients.get(i);
            List<EmiStack> emiStacks = ingredient.getEmiStacks();
            if (ingredient.isEmpty()) {
                discovered.add(null);
                continue;
            }
            for (EmiStack stack : emiStacks) {
                slotLoop:
                for (Slot s : slots) {
                    ItemStack ss = s.getItem();
                    if (EmiStack.of(s.getItem()).isEqual(stack)) {
                        for (DiscoveredItem di : d) {
                            if (ItemStack.isSameItemSameTags(ss, di.stack)) {
                                di.amount += ss.getCount();
                                continue slotLoop;
                            }
                        }
                        d.add(new DiscoveredItem(stack, ss, ss.getCount(), (int) ingredient.getAmount(), ss.getMaxStackSize()));
                    }
                }
            }
            DiscoveredItem biggest = null;
            for (DiscoveredItem di : d) {
                if (biggest == null) {
                    biggest = di;
                } else {
                    int a = di.amount / (weightDivider.getOrDefault(di.ingredient, 0) + di.consumed);
                    int ba = biggest.amount / (weightDivider.getOrDefault(biggest.ingredient, 0) + biggest.consumed);
                    if (ba < a) {
                        biggest = di;
                    }
                }
            }
            if (biggest == null || i >= craftingSlots.size()) {
                return false;
            }
            Slot slot = craftingSlots.get(i);
            if (slot == null) {
                return false;
            }
            weightDivider.put(biggest.ingredient, weightDivider.getOrDefault(biggest.ingredient, 0) + biggest.consumed);
            biggest.max = Math.min(biggest.max, slot.getMaxStackSize());
            discovered.add(biggest);
        }
        if (discovered.isEmpty()) {
            return false;
        }

        List<DiscoveredItem> unique = Lists.newArrayList();
        outer:
        for (DiscoveredItem di : discovered) {
            if (di == null) {
                continue;
            }
            for (DiscoveredItem ui : unique) {
                if (ItemStack.isSameItemSameTags(di.stack, ui.stack)) {
                    ui.consumed += di.consumed;
                    continue outer;
                }
            }
            unique.add(new DiscoveredItem(di.ingredient, di.stack, di.amount, di.consumed, di.max));
        }
        int maxAmount = Integer.MAX_VALUE;
        for (DiscoveredItem ui : unique) {
            if (!ui.catalyst()) {
                maxAmount = Math.min(maxAmount, ui.amount / ui.consumed);
                maxAmount = Math.min(maxAmount, ui.max);
            }
        }
        maxAmount = Math.min(maxAmount, amount + batchesAlreadyPresent(recipe, handler, screen));

        if (maxAmount == 0)
            return false;

        for (DiscoveredItem di : discovered) {
            if (di != null) {
                ItemStack is = di.stack.copy();
                int a = di.catalyst() ? di.consumed : di.consumed * maxAmount;
                is.setCount(a);
                desired.computeIfAbsent(KegEmiRecipeHandler.InputType.ITEM, inputType -> new ArrayList<>()).add(is);
            } else {
                desired.computeIfAbsent(KegEmiRecipeHandler.InputType.ITEM, inputType -> new ArrayList<>()).add(ItemStack.EMPTY);
            }
        }
        return true;
    }

    private static boolean handleFillingInputs(
            KegEmiRecipeHandler handler, FermentingEmiRecipe recipe, EmiCraftContext<KegMenu> context, int amount,
            Map<KegEmiRecipeHandler.InputType, List<ItemStack>> desired, EmiIngredient fluidIngredient, EmiIngredient itemIngredient, List<Slot> slots) {
        if (recipe.getFluidInput() == null || fluidIngredient.getEmiStacks().stream().anyMatch(emiStack -> context.getScreen().getMenu().kegTank.getAbstractedFluid().matches(new AbstractedFluidStack((Fluid) emiStack.getKey(), (int)emiStack.getAmount(), null))) && context.getScreen().getMenu().kegTank.getFluidCapacity() == Math.min(fluidIngredient.getAmount() * context.getAmount(), context.getScreen().getMenu().kegTank.getFluidCapacity()))
            return true;
        EmiIngredient emiFluidIngredient = recipe.getFluidInput();
        EmiStack emiFluidStack = recipe.getFluidInput().getEmiStacks().get(0);
        AbstractedFluidStack fluidStack = new AbstractedFluidStack((Fluid)emiFluidStack.getKey(), (int) emiFluidStack.getAmount(), null, FluidUnit.getLoaderUnit());


        List<EmiStack> discoveredQuickRef = Lists.newArrayList();

        List<DiscoveredItem> d = Lists.newArrayList();
        List<EmiStack> emiStacks = itemIngredient.getEmiStacks();

        for (EmiStack stack : emiStacks) {
            slotLoop: for (Slot s : slots) {
                ItemStack ss = s.getItem();
                if (EmiStack.of(s.getItem()).isEqual(stack)) {
                    for (DiscoveredItem di : d) {
                        if (ItemStack.isSameItemSameTags(ss, di.stack) || di.amount >= di.consumed) {
                            di.amount += ss.getCount();
                            continue slotLoop;
                        }
                    }
                    Optional<PouringEmiRecipe> potentialPouring = EmiApi.getRecipeManager().getRecipes(BnCRecipeCategories.POURING).stream().filter(r -> {
                        if (!(r instanceof PouringEmiRecipe pouring))
                            return false;
                        if (recipe.getFluidInput() == null)
                            return false;
                        return emiFluidIngredient.getEmiStacks().stream().anyMatch(es -> pouring.getFluidInput().getEmiStacks().get(0).isEqual(es)) && r.getOutputs().get(0).isEqual(stack);
                    }).map(recipe1 -> (PouringEmiRecipe) recipe1).findFirst();
                    if (potentialPouring.isPresent()) {
                        AbstractedFluidStack tankStack = context.getScreen().getMenu().kegTank.getAbstractedFluid();
                        EmiStack tankEmiStack = EmiStack.of(tankStack.fluid(), tankStack.componentPatch(), tankStack.amount());
                        int consumed = (int) ((context.getScreen().getMenu().kegTank.getFluidCapacity() - (potentialPouring.get().getFluidInput().getEmiStacks().stream().anyMatch(emiStack -> emiStack.isEqual(tankEmiStack)) ? context.getScreen().getMenu().kegTank.getAbstractedFluid().amount() : 0)) / potentialPouring.get().getFluidInput().getAmount());
                        if (consumed < 1)
                            continue;
                        d.add(new DiscoveredItem(stack, ss, ss.getCount(), consumed, (int) stack.getAmount()));
                        discoveredQuickRef.add(stack);
                    }
                }
            }
        }

        for (EmiStack inventoryStack : context.getInventory().inventory.values()) {
            if (discoveredQuickRef.contains(inventoryStack))
                continue;
            ItemStack itemStack = inventoryStack.getItemStack();
            AbstractedFluidTank tank = BrewinAndChewin.getHelper().getFluidContainerFromItem(itemStack);
            if (tank == null)
                continue;
            int target = (int) tank.getAbstractedFluid().amount();
            if (tank.getAbstractedFluid().isEmpty() && tank.isFluidValid(0, fluidStack) && target > 0) {
                AbstractedFluidStack tankStack = context.getScreen().getMenu().kegTank.getAbstractedFluid();
                int consumed = (int) ((context.getScreen().getMenu().kegTank.getFluidCapacity() - (tankStack.matches(tank.getAbstractedFluid()) ? context.getScreen().getMenu().kegTank.getAbstractedFluid().amount() : 0)) / target);
                if (consumed < 1)
                    continue;
                d.add(new DiscoveredItem(inventoryStack, itemStack, itemStack.getCount(), consumed, (int) ((context.getScreen().getMenu().kegTank.getFluidCapacity()) / target)));
            }
        }
        return handleFluidInternally(handler, recipe, context, amount, KegEmiRecipeHandler.InputType.FILL, desired, d);
    }

    private static boolean handleEmptyingInputs(
            KegEmiRecipeHandler handler, KegEmiRecipe recipe, EmiCraftContext<KegMenu> context, int amount,
            Map<KegEmiRecipeHandler.InputType, List<ItemStack>> desired, EmiIngredient ingredient, List<Slot> slots) {
        if (context.getScreen().getMenu().kegTank.isEmpty())
            return true;
        AbstractedFluidStack fluidStack = context.getScreen().getMenu().kegTank.getAbstractedFluid();
        EmiStack emiFluidStack = EmiStack.of(fluidStack.fluid(), fluidStack.componentPatch(), fluidStack.amount());

        List<EmiStack> discoveredQuickRef = Lists.newArrayList();

        List<DiscoveredItem> d = Lists.newArrayList();
        List<EmiStack> emiStacks = ingredient.getEmiStacks();

        for (EmiStack stack : emiStacks) {
            slotLoop: for (Slot s : slots) {
                ItemStack ss = s.getItem();
                if (EmiStack.of(s.getItem()).isEqual(stack)) {
                    for (DiscoveredItem di : d) {
                        if (ItemStack.isSameItemSameTags(ss, di.stack) || di.amount >= di.consumed) {
                            di.amount += ss.getCount();
                            continue slotLoop;
                        }
                    }
                    Optional<PouringEmiRecipe> potentialPouring = recipe instanceof PouringEmiRecipe pouring ? Optional.of(pouring) : EmiApi.getRecipeManager().getRecipes(BnCRecipeCategories.POURING).stream().filter(r -> {
                        if (!(r instanceof PouringEmiRecipe pouring))
                            return false;
                        return pouring.getFluidInput().getEmiStacks().get(0).isEqual(emiFluidStack) && r.getInputs().get(0).getEmiStacks().get(0).isEqual(stack);
                    }).map(recipe1 -> (PouringEmiRecipe) recipe1).findFirst();
                    if (potentialPouring.isPresent()) {
                        int consumed = (int) (Math.min(context.getScreen().getMenu().kegTank.getAbstractedFluid().amount() * context.getAmount(), context.getScreen().getMenu().kegTank.getFluidCapacity()) / potentialPouring.get().getFluidInput().getAmount());
                        if (recipe instanceof PouringEmiRecipe && consumed > context.getAmount())
                            consumed = context.getAmount();
                        if (consumed < 1)
                            continue;
                        d.add(new DiscoveredItem(stack, ss, ss.getCount(), consumed, (int) stack.getAmount()));
                        discoveredQuickRef.add(stack);
                    }
                }
            }
        }

        for (EmiStack inventoryStack : context.getInventory().inventory.values()) {
            if (discoveredQuickRef.contains(inventoryStack))
                continue;
            ItemStack itemStack = inventoryStack.getItemStack();
            AbstractedFluidTank tank = BrewinAndChewin.getHelper().getFluidContainerFromItem(itemStack);
            if (tank == null)
                continue;
            long target = tank.getAbstractedFluid().amount();
            if (tank.getAbstractedFluid().isEmpty() && tank.isFluidValid(0, fluidStack) && target > 0) {
                int consumed = (int) (recipe instanceof PouringEmiRecipe ? context.getScreen().getMenu().kegTank.getAbstractedFluid().amount() : (context.getScreen().getMenu().kegTank.getFluidCapacity() - context.getScreen().getMenu().kegTank.getAbstractedFluid().amount()) / target);
                if (consumed < 1)
                    continue;
                d.add(new DiscoveredItem(inventoryStack, itemStack, itemStack.getCount(), consumed * amount, (int) ((context.getScreen().getMenu().kegTank.getFluidCapacity()) / target)));
            }
        }
        return handleFluidInternally(handler, recipe, context, amount, KegEmiRecipeHandler.InputType.EMPTY, desired, d);
    }

    private static boolean handleFluidInternally(KegEmiRecipeHandler handler, KegEmiRecipe recipe, EmiCraftContext<KegMenu> context, int amount, KegEmiRecipeHandler.InputType inputType,
                                                 Map<KegEmiRecipeHandler.InputType, List<ItemStack>> desired, List<DiscoveredItem> d) {
        List<DiscoveredItem> discovered = Lists.newArrayList();

        DiscoveredItem biggest = null;
        for (DiscoveredItem di : d) {
            if (biggest == null) {
                biggest = di;
            } else {
                long a = di.amount / di.consumed;
                long ba = biggest.amount / biggest.consumed;
                if (ba < a) { // Use the lesser value here.
                    biggest = di;
                }
            }
        }
        if (biggest == null) {
            return false;
        }
        discovered.add(biggest);

        List<DiscoveredItem> unique = Lists.newArrayList();
        outer: for (DiscoveredItem di : discovered) {
            if (di == null) {
                continue;
            }
            for (DiscoveredItem ui : unique) {
                if (ItemStack.isSameItemSameTags(di.stack, ui.stack)) {
                    ui.consumed += di.consumed;
                    continue outer;
                }
            }
            unique.add(new DiscoveredItem(di.ingredient, di.stack, di.amount, di.consumed, di.max));
        }
        int maxAmount = Integer.MAX_VALUE;
        for (DiscoveredItem ui : unique) {
            if (!ui.catalyst()) {
                maxAmount = Math.min(maxAmount, ui.amount / ui.consumed);
                maxAmount = Math.min(maxAmount, ui.max);
            }
        }
        maxAmount = Math.min(maxAmount, amount + batchesAlreadyPresent(recipe, handler, context.getScreen()));

        if (maxAmount == 0)
            return false;

        for (DiscoveredItem di : discovered) {
            if (di != null) {
                ItemStack is = di.stack.copy();
                int a = di.catalyst() ? di.consumed : di.consumed * maxAmount;
                is.setCount(a);
                desired.computeIfAbsent(inputType, ip -> new ArrayList<>()).add(is);
            } else {
                desired.computeIfAbsent(inputType, ip -> new ArrayList<>()).add(ItemStack.EMPTY);
            }
        }
        return true;
    }

    private static int batchesAlreadyPresent(EmiRecipe recipe, KegEmiRecipeHandler handler, AbstractContainerScreen<KegMenu> screen) {
        if (recipe instanceof FermentingEmiRecipe)
            return 0;
        List<ItemStack> stacks = Lists.newArrayList();
        Slot output = handler.getOutputSlot(screen.getMenu());
        if (output != null && !output.getItem().isEmpty() && !recipe.getOutputs().isEmpty() && !ItemStack.matches(output.getItem(), recipe.getOutputs().get(0).getItemStack()))
            return 0;
        for (Slot slot : handler.getCraftingSlots(screen.getMenu())) {
            if (slot != null) {
                stacks.add(slot.getItem());
            } else {
                stacks.add(ItemStack.EMPTY);
            }
        }
        long amount = Long.MAX_VALUE;
        outer:for (int i = 0; i < recipe.getInputs().size(); i++) {
            EmiIngredient input = recipe.getInputs().get(i);
            if (input.isEmpty()) {
                if (stacks.get(i).isEmpty()) {
                    continue;
                }
                return 0;
            }
            if (i >= stacks.size()) {
                return 0;
            }
            EmiStack es = EmiStack.of(stacks.get(i));
            for (EmiStack v : input.getEmiStacks()) {
                if (v.isEmpty()) {
                    continue;
                }
                if (v.isEqual(es) && es.getAmount() >= v.getAmount()) {
                    amount = Math.min(amount, es.getAmount() / v.getAmount());
                    continue outer;
                }
            }
            return 0;
        }
        if (amount < Long.MAX_VALUE && amount > 0) {
            return (int) amount;
        }
        return 0;
    }

    private static class DiscoveredItem {
        private static final Comparison COMPARISON = Comparison.DEFAULT_COMPARISON;
        public EmiStack ingredient;
        public ItemStack stack;
        public int consumed;
        public int amount;
        public int max;

        public DiscoveredItem(EmiStack ingredient, ItemStack stack, int amount, int consumed, int max) {
            this.ingredient = ingredient;
            this.stack = stack.copy();
            this.amount = amount;
            this.consumed = consumed;
            this.max = max;
        }

        public boolean catalyst() {
            return ingredient.getRemainder().isEqual(ingredient);
        }
    }
}
