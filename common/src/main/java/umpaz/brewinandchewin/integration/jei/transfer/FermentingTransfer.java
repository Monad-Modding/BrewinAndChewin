/**
 * The MIT License (MIT)
 * <br>
 * Copyright (c) 2014-2015 mezz
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
package umpaz.brewinandchewin.integration.jei.transfer;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.network.serverbound.JEITransferKegRecipeServerboundPacket;
import umpaz.brewinandchewin.common.registry.BnCMenuTypes;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.integration.jei.BnCJEIRecipeTypes;
import umpaz.brewinandchewin.integration.jei.KegFermentingPouringRecipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Code here has been modified from JEI internals.
 */
public class FermentingTransfer {
    public static class Info implements IRecipeTransferInfo<KegMenu, KegFermentingPouringRecipe> {
        public static final Info INSTANCE = new Info();

        protected Info() {}

        @Override
        public Class<? extends KegMenu> getContainerClass() {
            return KegMenu.class;
        }

        @Override
        public Optional<MenuType<KegMenu>> getMenuType() {
            return Optional.of(BnCMenuTypes.KEG);
        }

        @Override
        public RecipeType<KegFermentingPouringRecipe> getRecipeType() {
            return BnCJEIRecipeTypes.FERMENTING;
        }

        @Override
        public boolean canHandle(KegMenu container, KegFermentingPouringRecipe recipe) {
            return true;
        }

        @Override
        public List<Slot> getRecipeSlots(KegMenu container, KegFermentingPouringRecipe recipe) {
            List<Slot> slots = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                Slot slot = container.getSlot(i);
                slots.add(slot);
            }
            return slots;
        }

        @Override
        public List<Slot> getInventorySlots(KegMenu container, KegFermentingPouringRecipe recipe) {
            List<Slot> slots = new ArrayList<>();
            for (int i = 6; i < 42; i++) {
                Slot slot = container.getSlot(i);
                slots.add(slot);
            }
            return slots;
        }
    }

    public static class Handler implements IRecipeTransferHandler<KegMenu, KegFermentingPouringRecipe> {
        private final IRecipeTransferHandlerHelper helper;
        private final IStackHelper stackHelper;
        private final IPlatformFluidHelper<?> platformFluidHelper;

        public Handler(IRecipeTransferHandlerHelper helper, IStackHelper stackHelper, IPlatformFluidHelper<?> platformFluidHelper) {
            this.helper = helper;
            this.stackHelper = stackHelper;
            this.platformFluidHelper = platformFluidHelper;
        }

        @Override
        public Class<? extends KegMenu> getContainerClass() {
            return KegMenu.class;
        }

        @Override
        public Optional<MenuType<KegMenu>> getMenuType() {
            return Optional.of(BnCMenuTypes.KEG);
        }

        @Override
        public RecipeType<KegFermentingPouringRecipe> getRecipeType() {
            return BnCJEIRecipeTypes.FERMENTING;
        }

        @Override
        public @Nullable IRecipeTransferError transferRecipe(KegMenu menu, KegFermentingPouringRecipe recipe, IRecipeSlotsView view, Player player, boolean maxTransfer, boolean doTransfer) {
            if (!KegBlockEntity.isValidTemp(menu.getKegTemperature(), recipe.getTemperature())) {
                Component message = Component.translatable("brewinandchewin.jei.tooltip.error.recipe.transfer.temperature");
                return helper.createUserErrorWithTooltip(message);
            }

            Info info = Info.INSTANCE;
            List<Slot> craftingSlots = Collections.unmodifiableList(info.getRecipeSlots(menu, recipe));
            List<Slot> inventorySlots = Collections.unmodifiableList(info.getInventorySlots(menu, recipe));

            List<IRecipeSlotView> inputItemSlotViews = view.getSlotViews(RecipeIngredientRole.INPUT).stream().filter(ingredientView -> ingredientView.getAllIngredients().anyMatch(ingredient -> ingredient.getIngredient(VanillaTypes.ITEM_STACK).isPresent())).toList();

            InventoryState inv = createInvState(craftingSlots, inventorySlots);


            int requiredInv = inputItemSlotViews.size();
            if (!inv.hasRoom(requiredInv)) {
                Component message = Component.translatable("jei.tooltip.error.recipe.transfer.inventory.full");
                return helper.createUserErrorWithTooltip(message);
            }

            TransferOperations operations = createOperations(
                    inv.availableItemStacks,
                    inputItemSlotViews,
                    recipe.getFluidIngredient().isPresent() ?
                            view.getSlotViews(RecipeIngredientRole.INPUT).stream().filter(ingredientView -> ingredientView.getAllIngredients().anyMatch(ingredient -> ingredient.getIngredient(platformFluidHelper.getFluidIngredientType()).isPresent())).findFirst().orElse(null) :
                            null,
                    recipe,
                    menu,
                    craftingSlots,
                    maxTransfer
            );

            if (!operations.canEmpty) {
                Component message = Component.translatable("brewinandchewin.jei.tooltip.error.recipe.transfer.cant_empty");
                return helper.createUserErrorWithTooltip(message);
            }
            if (operations.notEnoughFluid) {
                Component message = Component.translatable("brewinandchewin.jei.tooltip.error.recipe.transfer.not_enough_fluid");
                return helper.createUserErrorWithTooltip(message);
            }
            if (operations.invalidFluid) {
                Component message = Component.translatable("brewinandchewin.jei.tooltip.error.recipe.transfer.invalid_fluid");
                return helper.createUserErrorWithTooltip(message);
            }
            if (!operations.missingItems.isEmpty()) {
                Component message = Component.translatable("jei.tooltip.error.recipe.transfer.missing");
                return helper.createUserErrorForMissingSlots(message, operations.missingItems);
            }

            if (doTransfer) {
                BrewinAndChewin.getHelper().sendServerbound(new JEITransferKegRecipeServerboundPacket(
                        recipe.getId(),
                        operations.results.stream().map(pair -> Pair.of(pair.getFirst().index, pair.getSecond().index)).toList(),
                        operations.fluidResults.stream().map(pair -> Pair.of(pair.getFirst().index, pair.getSecond())).toList(),
                        operations.emptyingResults.stream().map(pair -> Pair.of(pair.getFirst().index, pair.getSecond())).toList(),
                        craftingSlots.stream().map(slot -> slot.index).toList(),
                        inventorySlots.stream().map(slot -> slot.index).toList(),
                        maxTransfer
                ));
            }

            return null;
        }

        private InventoryState createInvState(
                Collection<Slot> craftingSlots,
                Collection<Slot> inventorySlots) {
            Map<Slot, ItemStack> availableItemStacks = new HashMap<>();
            int filledCraftSlotCount = 0;
            int emptySlotCount = 0;

            for (Slot slot : craftingSlots) {
                final ItemStack stack = slot.getItem();
                if (!stack.isEmpty()) {
                    filledCraftSlotCount++;
                    availableItemStacks.put(slot, stack.copy());
                }
            }

            for (Slot slot : inventorySlots) {
                final ItemStack stack = slot.getItem();
                if (!stack.isEmpty()) {
                    availableItemStacks.put(slot, stack.copy());
                } else {
                    emptySlotCount++;
                }
            }

            return new InventoryState(availableItemStacks, filledCraftSlotCount, emptySlotCount);
        }

        private TransferOperations createOperations(
                Map<Slot, ItemStack> availableItemStacks,
                List<IRecipeSlotView> requiredItemStacks,
                IRecipeSlotView requiredFluidStack,
                KegFermentingPouringRecipe recipe,
                KegMenu menu,
                List<Slot> craftingSlots,
                boolean maxTransfer
        ) {
            TransferOperations operations = new TransferOperations();
            Map<IRecipeSlotView, Map<ItemStack, List<SlotReference>>> relevantSlots = new IdentityHashMap<>();
            Map<ItemStack, List<SlotReference>> emptyingSlots = new IdentityHashMap<>();
            boolean hasTooMuchFluid = false;
            long fluidCapacity = 0;
            long largestFluidCapacity = 0;
            long largestEmptyCapacity = 0;

            for (Map.Entry<Slot, ItemStack> slotTuple : availableItemStacks.entrySet()) {
                for (IRecipeSlotView ingredient : requiredItemStacks) {
                    if (!ingredient.isEmpty() && ingredient.getItemStacks().anyMatch(it -> stackHelper.isEquivalent(it, slotTuple.getValue(), UidContext.Ingredient))) {
                        relevantSlots
                                .computeIfAbsent(ingredient, it -> new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
                                    @Override
                                    public int hashCode(ItemStack o) {
                                        return o.getItem().hashCode();
                                    }

                                    @Override
                                    public boolean equals(ItemStack a, ItemStack b) {
                                        return stackHelper.isEquivalent(a, b, UidContext.Ingredient);
                                    }
                                }))
                                .computeIfAbsent(slotTuple.getValue(), it -> new ArrayList<>())
                                .add(new SlotReference(slotTuple.getKey(), slotTuple.getValue(), null, 1));
                    }
                }


                if (!menu.kegTank.isEmpty() && (!maxTransfer || recipe.getFluidIngredient().isEmpty() || !recipe.getFluidIngredient().get().ingredient().matches(menu.kegTank.getAbstractedFluid()))) {
                    List<KegPouringRecipe> pouringRecipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().filter(kegPouringRecipe -> kegPouringRecipe.getFluid(slotTuple.getValue()).matches(menu.kegTank.getAbstractedFluid())).toList();
                    Optional<KegPouringRecipe> optionalData = pouringRecipes.stream().filter(pouring -> {
                        if (pouring.isStrict())
                            return ItemStack.isSameItemSameTags(slotTuple.getValue(), pouring.getContainer());
                        return ItemStack.isSameItem(slotTuple.getValue(), pouring.getContainer());
                    }).findFirst();
                    if (optionalData.isPresent()) {
                        if (optionalData.get().getRawFluid().amount() <= menu.kegTank.getAbstractedFluid().amount()) {
                            int shrinkAmount = (int) (menu.kegTank.getAbstractedFluid().amount() / optionalData.get().getRawFluid().amount());
                            emptyingSlots
                                    .computeIfAbsent(slotTuple.getValue(), it -> new ArrayList<>())
                                    .add(new SlotReference(slotTuple.getKey(), slotTuple.getValue(), optionalData.get().getRawFluid().amount() * shrinkAmount, shrinkAmount));
                        }
                        if (recipe.getFluidIngredient().isPresent() && recipe.getFluidIngredient().get().ingredient().matches(menu.kegTank.getAbstractedFluid())) {
                            if (optionalData.get().getRawFluid().amount() <= recipe.getFluidIngredient().get().amount() && fluidCapacity < recipe.getFluidIngredient().get().amount()) {
                                if (optionalData.get().getRawFluid().amount() > largestEmptyCapacity) {
                                    relevantSlots.remove(requiredFluidStack);
                                }
                                int shrinkAmount = (int) (recipe.getFluidIngredient().get().amount() / optionalData.get().getRawFluid().amount());
                                largestEmptyCapacity = optionalData.get().getRawFluid().amount();
                                relevantSlots
                                        .computeIfAbsent(requiredFluidStack, it -> new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
                                            @Override
                                            public int hashCode(ItemStack o) {
                                                return o.getItem().hashCode();
                                            }

                                            @Override
                                            public boolean equals(ItemStack a, ItemStack b) {
                                                return stackHelper.isEquivalent(a, b, UidContext.Ingredient);
                                            }
                                        }))
                                        .computeIfAbsent(slotTuple.getValue(), it -> new ArrayList<>())
                                        .add(new SlotReference(slotTuple.getKey(), optionalData.get().getResultItem(Minecraft.getInstance().level.registryAccess()).copyWithCount(shrinkAmount), optionalData.get().getRawFluid().amount() * shrinkAmount, shrinkAmount));
                            }
                        }
                    }
                }

                if (recipe.getFluidIngredient().isPresent() && !requiredFluidStack.isEmpty() && requiredFluidStack.getIngredients(platformFluidHelper.getFluidIngredientType()).findFirst().isPresent()) {
                    List<KegPouringRecipe> pouringRecipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().filter(kegPouringRecipe -> kegPouringRecipe.canFill() && recipe.getFluidIngredient().get().ingredient().matches(kegPouringRecipe.getFluid(slotTuple.getValue()))).toList();
                    Optional<KegPouringRecipe> optionalData = pouringRecipes.stream().filter(pouring -> {
                        if (pouring.isStrict())
                            return ItemStack.isSameItemSameTags(slotTuple.getValue(), pouring.getOutput());
                        return ItemStack.isSameItem(slotTuple.getValue(), pouring.getOutput());
                    }).findFirst();
                    long tankAmount = recipe.getFluidIngredient().get().ingredient().matches(menu.kegTank.getAbstractedFluid()) ? menu.kegTank.getAbstractedFluid().amount() : 0;
                    if (optionalData.isPresent()) {
                        if (optionalData.get().getRawFluid().amount() <= menu.kegTank.getFluidCapacity() - tankAmount && fluidCapacity < menu.kegTank.getFluidCapacity() - tankAmount) {
                            if (optionalData.get().getRawFluid().amount() > largestFluidCapacity) {
                                relevantSlots.remove(requiredFluidStack);
                            }
                            int shrinkAmount = (int) ((recipe.getFluidIngredient().get().amount() / optionalData.get().getRawFluid().amount()) - ((tankAmount % recipe.getFluidIngredient().get().amount()) / optionalData.get().getRawFluid().amount()));
                            largestFluidCapacity = optionalData.get().getRawFluid().amount();
                            fluidCapacity += optionalData.get().getRawFluid().amount() * shrinkAmount;
                            relevantSlots
                                    .computeIfAbsent(requiredFluidStack, it -> new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
                                        @Override
                                        public int hashCode(ItemStack o) {
                                            return o.getItem().hashCode();
                                        }

                                        @Override
                                        public boolean equals(ItemStack a, ItemStack b) {
                                            return stackHelper.isEquivalent(a, b, UidContext.Ingredient);
                                        }
                                    }))
                                    .computeIfAbsent(slotTuple.getValue(), it -> new ArrayList<>())
                                    .add(new SlotReference(slotTuple.getKey(), slotTuple.getValue(), optionalData.get().getRawFluid().amount() * shrinkAmount, shrinkAmount));
                        } else
                            hasTooMuchFluid = true;
                    }
                }
            }

            Map<IRecipeSlotView, List<List<SlotReference>>> bestMatches = new Object2ObjectArrayMap<>();
            List<List<SlotReference>> emptyingBestMatches = new ArrayList<>();

            for (Map.Entry<IRecipeSlotView, Map<ItemStack, List<SlotReference>>> entry : relevantSlots.entrySet()) {
                List<List<SlotReference>> countedAndSorted = new ArrayList<>();

                for (Map.Entry<ItemStack, List<SlotReference>> foundSlots : entry.getValue().entrySet()) {
                    foundSlots.getValue().sort((o1, o2) -> {
                        int compare = o1.fluidAmount != null && o2.fluidAmount != null ?
                                Long.compare(o1.fluidAmount, o2.fluidAmount) :
                                Integer.compare(o1.stack.getCount(), o2.stack.getCount());

                        if (compare == 0) {
                            return Integer.compare(o1.slot.index, o2.slot.index);
                        }

                        return compare;
                    });

                    countedAndSorted.add(foundSlots.getValue());
                }

                countedAndSorted.sort((o1, o2) -> {
                    int compare = Long.compare(o2.stream().mapToLong(it -> it.stack.getCount()).sum(), o1.stream().mapToLong(it -> it.stack.getCount()).sum());

                    if (compare == 0) {
                        return Integer.compare(
                                o1.stream().mapToInt(it -> it.slot.index).min().orElse(0),
                                o2.stream().mapToInt(it -> it.slot.index).min().orElse(0)
                        );
                    }

                    return compare;
                });

                bestMatches.put(entry.getKey(), countedAndSorted);
            }


            for (Map.Entry<ItemStack, List<SlotReference>> entry : emptyingSlots.entrySet()) {
                List<List<SlotReference>> countedAndSorted = new ArrayList<>();

                entry.getValue().sort((o1, o2) -> {
                    int compare = o1.fluidAmount != null && o2.fluidAmount != null ?
                            Long.compare(o1.fluidAmount, o2.fluidAmount) :
                            Integer.compare(o1.stack.getCount(), o2.stack.getCount());

                    if (compare == 0) {
                        return Integer.compare(o1.slot.index, o2.slot.index);
                    }

                    return compare;
                });

                countedAndSorted.add(entry.getValue());

                countedAndSorted.sort((o1, o2) -> {
                    int compare = Long.compare(o2.stream().mapToLong(it -> it.stack.getCount()).sum(), o1.stream().mapToLong(it -> it.stack.getCount()).sum());

                    if (compare == 0) {
                        return Integer.compare(
                                o1.stream().mapToInt(it -> it.slot.index).min().orElse(0),
                                o2.stream().mapToInt(it -> it.slot.index).min().orElse(0)
                        );
                    }

                    return compare;
                });

                emptyingBestMatches.addAll(countedAndSorted);
            }

            List<IRecipeSlotView> inclusiveRequiredItemStacks = new ArrayList<>(requiredItemStacks);
            if (requiredFluidStack != null)
                inclusiveRequiredItemStacks.add(requiredFluidStack);

            for (IRecipeSlotView ingredient : inclusiveRequiredItemStacks) {
                if (!ingredient.isEmpty()) {
                    bestMatches.computeIfAbsent(ingredient, it -> new ArrayList<>());
                }
            }

            for (int i = 0; i < requiredItemStacks.size(); i++) {
                IRecipeSlotView requiredItemStack = requiredItemStacks.get(i);

                if (requiredItemStack.isEmpty()) {
                    continue;
                }

                Slot craftingSlot = craftingSlots.get(i);

                SlotReference matching = bestMatches
                        .get(requiredItemStack)
                        .stream()
                        .flatMap(pairs -> pairs.stream().filter(p -> !p.stack.isEmpty()))
                        .findFirst()
                        .orElse(null);

                if (matching == null) {
                    operations.missingItems.add(requiredItemStack);
                } else {
                    matching.stack.shrink(matching.shrinkAmount);
                    operations.results.add(Pair.of(matching.slot, craftingSlot));
                }
            }

            if (requiredFluidStack != null && recipe.getFluidIngredient().isPresent()) {
                long amountToFill = Math.max((maxTransfer ? menu.kegTank.getFluidCapacity() : recipe.getFluidIngredient().get().amount()) - (recipe.getFluidIngredient().get().ingredient().matches(menu.kegTank.getAbstractedFluid()) ? menu.kegTank.getAbstractedFluid().amount() : 0), 0);

                if (amountToFill > 0) {
                    List<SlotReference> allMatching = bestMatches
                            .get(requiredFluidStack)
                            .stream()
                            .flatMap(pairs -> pairs.stream().filter(p -> !p.stack.isEmpty() && p.fluidAmount != null))
                            .toList();

                    if (allMatching.isEmpty()) {
                        operations.missingItems.add(requiredFluidStack);
                        if (hasTooMuchFluid)
                            operations.invalidFluid = true;
                    } else {
                        List<SlotReference> toShrink = new ArrayList<>();
                        for (SlotReference matching : allMatching) {
                            if (amountToFill <= 0)
                                break;
                            toShrink.add(matching);
                            operations.fluidResults.add(Pair.of(matching.slot, matching.fluidAmount));
                            amountToFill -= matching.fluidAmount != null ? matching.fluidAmount : 0;
                        }
                        if (amountToFill > 0) {
                            operations.fluidResults.clear();
                            operations.notEnoughFluid = true;
                        }
                        else
                            toShrink.forEach(slotReference -> slotReference.stack.shrink(slotReference.shrinkAmount));
                    }
                }
            }

            if (!emptyingBestMatches.isEmpty()) {
                long amountToEmpty = menu.kegTank.getAbstractedFluid().amount();

                List<SlotReference> allMatching = emptyingBestMatches
                        .stream()
                        .flatMap(pairs -> pairs.stream().filter(p -> !p.stack.isEmpty() && p.fluidAmount != null))
                        .toList();

                if (allMatching.isEmpty() && !menu.kegTank.isEmpty() && (recipe.getFluidIngredient().isEmpty() || !recipe.getFluidIngredient().get().ingredient().matches(menu.kegTank.getAbstractedFluid()))) {
                    operations.canEmpty = false;
                } else {
                    List<SlotReference> toShrink = new ArrayList<>();
                    for (SlotReference matching : allMatching) {
                        if (amountToEmpty <= 0)
                            break;
                        toShrink.add(matching);
                        operations.emptyingResults.add(Pair.of(matching.slot, matching.fluidAmount));
                        amountToEmpty -= matching.fluidAmount != null ? matching.fluidAmount : 0;
                    }
                    if (amountToEmpty > 0 && !menu.kegTank.isEmpty() && (recipe.getFluidIngredient().isEmpty() || !recipe.getFluidIngredient().get().ingredient().matches(menu.kegTank.getAbstractedFluid()))) {
                        operations.emptyingResults.clear();
                        operations.canEmpty = false;
                    } else
                        toShrink.forEach(slotReference -> slotReference.stack.shrink(slotReference.shrinkAmount));
                }
            }

            if (!menu.kegTank.isEmpty() && emptyingBestMatches.isEmpty() && (recipe.getFluidIngredient().isEmpty() || !recipe.getFluidIngredient().get().ingredient().matches(menu.kegTank.getAbstractedFluid()) || recipe.getFluidIngredient().isPresent() && menu.kegTank.getAbstractedFluid().amount() % recipe.getFluidIngredient().get().amount() != 0)) {
                operations.canEmpty = false;
            }

            return operations;
        }
    }

    private record SlotReference(
            Slot slot,
            ItemStack stack,
            @Nullable Long fluidAmount,
            int shrinkAmount
    ) {}

    public static class TransferOperations {
        public final List<Pair<Slot, Slot>> results = new ArrayList<>();
        public final List<Pair<Slot, Long>> fluidResults = new ArrayList<>();
        public final List<Pair<Slot, Long>> emptyingResults = new ArrayList<>();

        public final List<IRecipeSlotView> missingItems = new ArrayList<>();
        public boolean canEmpty = true;
        public boolean notEnoughFluid = false;
        public boolean invalidFluid = false;

        public static TransferOperations readFromIntegers(List<Pair<Integer, Integer>> resultSlots,
                                                          List<Pair<Integer, Long>> fluidSlots,
                                                          List<Pair<Integer, Long>> emptyingSlots,
                                                          AbstractContainerMenu menu) {
            TransferOperations operations = new TransferOperations();
            for (Pair<Integer, Integer> resultSlot : resultSlots) {
                int inventorySlotIndex = resultSlot.getFirst();
                int craftingSlotIndex = resultSlot.getSecond();
                operations.results.add(Pair.of(menu.getSlot(inventorySlotIndex), menu.getSlot(craftingSlotIndex)));
            }
            for (Pair<Integer, Long> fluidSlot : fluidSlots) {
                int fluidSlotIndex = fluidSlot.getFirst();
                long fluidAmount = fluidSlot.getSecond();
                operations.fluidResults.add(Pair.of(menu.getSlot(fluidSlotIndex), fluidAmount));
            }
            for (Pair<Integer, Long> emptyingSlot : emptyingSlots) {
                int emptyingSlotIndex = emptyingSlot.getFirst();
                long fluidAmount = emptyingSlot.getSecond();
                operations.emptyingResults.add(Pair.of(menu.getSlot(emptyingSlotIndex), fluidAmount));
            }
            return operations;
        }
    }

    private record InventoryState(
            Map<Slot, ItemStack> availableItemStacks,
            int filledCraftSlotCount,
            int emptySlotCount
    ) {

        private boolean hasRoom(int inputCount) {
            return filledCraftSlotCount - inputCount <= emptySlotCount;
        }
    }
}
