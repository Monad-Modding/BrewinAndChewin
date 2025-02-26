package umpaz.brewinandchewin.integration.jei.transfer;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Code here has been modified from JEI internals.
 * <br>
 * JEI is licensed under the MIT license.
 * <a href="https://github.com/mezz/JustEnoughItems/blob/1.21.x/LICENSE.txt">You may read the license here.</a>
 * {@see mezz.jei.library.transfer.BasicRecipeTransferHandlerServer}
 */
public class FermentingTransferServer {
    public static void setItems(
            Player player,
            KegFermentingRecipe recipe,
            FermentingTransfer.TransferOperations transferOperations,
            List<Slot> craftingSlots,
            List<Slot> inventorySlots,
            boolean maxTransfer) {
        if (!(player.containerMenu instanceof KegMenu kegMenu))
            return;

        Map<Slot, Pair<Slot, ItemStack>> recipeSlotToRequiredItemStack = calculateRequiredStacks(transferOperations, player);
        List<Pair<Slot, Pair<ItemStack, Integer>>> requiredFluidStacks  = calculateRequiredFluidOrEmptyingStacks(transferOperations.fluidResults, player, Either.left(recipe));
        List<Pair<Slot, Pair<ItemStack, Integer>>> requiredEmptyingStacks  = calculateRequiredFluidOrEmptyingStacks(transferOperations.emptyingResults, player, Either.right(kegMenu));
        if (recipeSlotToRequiredItemStack == null || requiredFluidStacks == null || requiredEmptyingStacks == null)
            return;

        boolean transferAsCompleteSets = !maxTransfer;

        Map<Slot, ItemStack> recipeSlotToTakenStacks = takeItemsFromInventory(
                player,
                recipeSlotToRequiredItemStack,
                craftingSlots,
                inventorySlots,
                transferAsCompleteSets,
                maxTransfer
        );
        List<ItemStack> emptyingSlotToTakenStacks = takeFluidOrEmptyingItemsFromInventory(
                player,
                kegMenu,
                requiredEmptyingStacks,
                craftingSlots,
                inventorySlots,
                transferAsCompleteSets,
                maxTransfer,
                false
        );
        List<ItemStack> fluidSlotToTakenStacks = takeFluidOrEmptyingItemsFromInventory(
                player,
                kegMenu,
                requiredFluidStacks,
                craftingSlots,
                inventorySlots,
                transferAsCompleteSets,
                maxTransfer,
                true
        );

        if (recipeSlotToTakenStacks.isEmpty() && fluidSlotToTakenStacks.isEmpty() && emptyingSlotToTakenStacks.isEmpty())
            return;

        boolean sameFluid = recipe.getFluidIngredient() != null && recipe.getFluidIngredient().isFluidEqual(kegMenu.kegTank.getFluid());

        List<ItemStack> clearedFluidItems = extractFromFluidTank(emptyingSlotToTakenStacks, kegMenu, false, null);


        if (sameFluid && !maxTransfer)
            fluidSlotToTakenStacks = clearedFluidItems;
        else
            stowItems(player, inventorySlots, clearedFluidItems);

        List<ItemStack> fluidItems = extractFromFluidTank(fluidSlotToTakenStacks, kegMenu, true, !maxTransfer ? recipe : null);

        List<ItemStack> clearedCraftingItems = clearCraftingGrid(craftingSlots, player);

        List<ItemStack> remainderItems = putItemsIntoCraftingGrid(recipeSlotToTakenStacks);

        stowItems(player, inventorySlots, fluidItems);
        stowItems(player, inventorySlots, clearedCraftingItems);
        stowItems(player, inventorySlots, remainderItems);

        kegMenu.blockEntity.setChanged();
        player.level().sendBlockUpdated(kegMenu.blockEntity.getBlockPos(), kegMenu.blockEntity.getBlockState(), kegMenu.blockEntity.getBlockState(), 2);
    }

    @NotNull
    private static Map<Slot, ItemStack> takeItemsFromInventory(
            Player player,
            Map<Slot, Pair<Slot, ItemStack>> recipeSlotToRequiredItemStack,
            List<Slot> craftingSlots,
            List<Slot> inventorySlots,
            boolean transferAsCompleteSets,
            boolean maxTransfer
    ) {
        if (!maxTransfer) {
            return removeOneSetOfItemsFromInventory(
                    player,
                    recipeSlotToRequiredItemStack,
                    craftingSlots,
                    inventorySlots,
                    transferAsCompleteSets
            );
        }

        final Map<Slot, ItemStack> recipeSlotToResult = new HashMap<>(recipeSlotToRequiredItemStack.size());
        while (true) {
            final Map<Slot, ItemStack> foundItemsInSet = removeOneSetOfItemsFromInventory(
                    player,
                    recipeSlotToRequiredItemStack,
                    craftingSlots,
                    inventorySlots,
                    transferAsCompleteSets
            );

            if (foundItemsInSet.isEmpty()) {
                break;
            }

            Set<Slot> fullSlots = merge(recipeSlotToResult, foundItemsInSet);

            for (Slot fullSlot : fullSlots) {
                recipeSlotToRequiredItemStack.remove(fullSlot);
            }
        }

        return recipeSlotToResult;
    }

    private static Map<Slot, ItemStack> removeOneSetOfItemsFromInventory(
            Player player,
            Map<Slot, Pair<Slot, ItemStack>> recipeSlotToRequiredItemStack,
            List<Slot> craftingSlots,
            List<Slot> inventorySlots,
            boolean transferAsCompleteSets
    ) {
        Map<Slot, ItemStack> originalSlotContents = null;
        if (transferAsCompleteSets) {
            originalSlotContents = new HashMap<>();
        }

        final Map<Slot, ItemStack> foundItemsInSet = new HashMap<>(recipeSlotToRequiredItemStack.size());

        for (Map.Entry<Slot, Pair<Slot, ItemStack>> entry : recipeSlotToRequiredItemStack.entrySet()) {
            final Slot recipeSlot = entry.getKey();
            final ItemStack requiredStack = entry.getValue().getSecond();
            final Slot hint = entry.getValue().getFirst();

            final Slot slot = getSlotWithStack(player, requiredStack, craftingSlots, inventorySlots, hint)
                    .orElse(null);
            if (slot != null) {
                if (originalSlotContents != null && !originalSlotContents.containsKey(slot)) {
                    originalSlotContents.put(slot, slot.getItem().copy());
                }

                ItemStack removedItemStack = slot.remove(1);
                foundItemsInSet.put(recipeSlot, removedItemStack);
            } else {
                if (transferAsCompleteSets) {
                    for (Map.Entry<Slot, ItemStack> slotEntry : originalSlotContents.entrySet()) {
                        ItemStack stack = slotEntry.getValue();
                        slotEntry.getKey().set(stack);
                    }
                    return Map.of();
                }
            }
        }
        return foundItemsInSet;
    }

    @NotNull
    private static List<ItemStack> takeFluidOrEmptyingItemsFromInventory(
            Player player,
            KegMenu kegMenu,
            List<Pair<Slot, Pair<ItemStack, Integer>>> requiredItemStacks,
            List<Slot> craftingSlots,
            List<Slot> inventorySlots,
            boolean transferAsCompleteSets,
            boolean maxTransfer,
            boolean insert
    ) {
        if (!maxTransfer) {
            return removeOneSetOfFluidOrEmptyingItemsFromInventory(
                    player,
                    requiredItemStacks,
                    craftingSlots,
                    inventorySlots,
                    transferAsCompleteSets
            ).getFirst();
        }
        final List<ItemStack> recipeSlotToResult = new ArrayList<>(requiredItemStacks.size());
        int fluidCapacity = 0;

        while (true) {
            if (insert && fluidCapacity >= kegMenu.kegTank.getCapacity() || !insert && fluidCapacity >= kegMenu.kegTank.getFluidAmount())
                break;

            final Pair<List<ItemStack>, Integer> foundItemsInSet = removeOneSetOfFluidOrEmptyingItemsFromInventory(
                    player,
                    requiredItemStacks,
                    craftingSlots,
                    inventorySlots,
                    transferAsCompleteSets
            );

            if (foundItemsInSet.getFirst().isEmpty())
                break;

            fluidCapacity += foundItemsInSet.getSecond();

            recipeSlotToResult.addAll(foundItemsInSet.getFirst());
        }

        return recipeSlotToResult;
    }

    private static Pair<List<ItemStack>, Integer> removeOneSetOfFluidOrEmptyingItemsFromInventory(
            Player player,
            List<Pair<Slot, Pair<ItemStack, Integer>>> requiredItemStacks,
            List<Slot> craftingSlots,
            List<Slot> inventorySlots,
            boolean transferAsCompleteSets
    ) {
        Map<Slot, ItemStack> originalSlotContents = null;
        if (transferAsCompleteSets)
            originalSlotContents = new HashMap<>();

        final List<ItemStack> foundItemsInSet = new ArrayList<>(requiredItemStacks.size());
        int fluidAmount = 0;

        for (Pair<Slot, Pair<ItemStack, Integer>> entry : requiredItemStacks) {
            final ItemStack requiredStack = entry.getSecond().getFirst();
            final Slot hint = entry.getFirst();

            final Slot slot = getSlotWithStack(player, requiredStack, craftingSlots, inventorySlots, hint)
                    .orElse(null);
            if (slot != null) {
                if (originalSlotContents != null && !originalSlotContents.containsKey(slot))
                    originalSlotContents.put(slot, slot.getItem().copy());

                ItemStack removedItemStack = slot.remove(requiredStack.getCount());
                foundItemsInSet.add(removedItemStack);
                fluidAmount += entry.getSecond().getSecond();
            } else {
                if (transferAsCompleteSets) {
                    for (Map.Entry<Slot, ItemStack> slotEntry : originalSlotContents.entrySet()) {
                        ItemStack stack = slotEntry.getValue();
                        slotEntry.getKey().set(stack);
                    }
                    return Pair.of(List.of(), 0);
                }
            }
        }
        return Pair.of(foundItemsInSet, fluidAmount);
    }

    private static Set<Slot> merge(Map<Slot, ItemStack> result, Map<Slot, ItemStack> addition) {
        Set<Slot> fullSlots = new HashSet<>();

        addition.forEach((slot, itemStack) -> {
            assert itemStack.getCount() == 1;

            ItemStack resultItemStack = result.get(slot);
            if (resultItemStack == null) {
                resultItemStack = itemStack;
                result.put(slot, resultItemStack);
            } else {
                assert ItemStack.isSameItemSameTags(resultItemStack, itemStack);
                resultItemStack.grow(itemStack.getCount());
            }
            if (resultItemStack.getCount() == slot.getMaxStackSize(resultItemStack)) {
                fullSlots.add(slot);
            }
        });

        return fullSlots;
    }

    @Nullable
    private static Map<Slot, Pair<Slot, ItemStack>> calculateRequiredStacks(FermentingTransfer.TransferOperations transferOperations, Player player) {
        Map<Slot, Pair<Slot, ItemStack>> recipeSlotToRequired = new HashMap<>(transferOperations.results.size());
        for (Pair<Slot, Slot> transferOperation : transferOperations.results) {
            Slot recipeSlot = transferOperation.getSecond();
            Slot inventorySlot = transferOperation.getFirst();
            if (!inventorySlot.mayPickup(player))
                return null;
            final ItemStack slotStack = inventorySlot.getItem();
            if (slotStack.isEmpty())
                return null;
            ItemStack stack = slotStack.copy();
            stack.setCount(1);
            recipeSlotToRequired.put(recipeSlot, Pair.of(inventorySlot, stack));
        }
        return recipeSlotToRequired;
    }

    @Nullable
    private static List<Pair<Slot, Pair<ItemStack, Integer>>> calculateRequiredFluidOrEmptyingStacks(List<Pair<Slot, Integer>> slots, Player player, Either<KegFermentingRecipe, KegMenu> pouringRecipeSource) {
        if (pouringRecipeSource.left().isPresent() && pouringRecipeSource.left().get().getFluidIngredient() == null)
            return List.of();
        List<Pair<Slot, Pair<ItemStack, Integer>>> recipeSlotToRequired = new ArrayList<>(slots.size());
        for (Pair<Slot, Integer> inventorySlot : slots) {
            if (!inventorySlot.getFirst().mayPickup(player))
                return null;
            final ItemStack slotStack = inventorySlot.getFirst().getItem();
            if (slotStack.isEmpty())
                return null;
            ItemStack stack = slotStack.copy();

            int fluidStackAmount = 1;
            List<KegPouringRecipe> pouringRecipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING.get()).stream().filter(kegPouringRecipe -> (pouringRecipeSource.left().isEmpty() || kegPouringRecipe.canFill()) && kegPouringRecipe.getFluid(stack).isFluidEqual(pouringRecipeSource.map(KegFermentingRecipe::getFluidIngredient, menu -> menu.kegTank.getFluid()))).toList();
            Optional<KegPouringRecipe> optionalData = pouringRecipes.stream().filter(pouring -> {
                if (pouring.isStrict())
                    return ItemStack.isSameItemSameTags(stack, pouringRecipeSource.map(ignored -> pouring.getOutput(), ignored -> pouring.getContainer()));
                return ItemStack.isSameItem(stack, pouringRecipeSource.map(ignored -> pouring.getOutput(), ignored -> pouring.getContainer()));
            }).findFirst();
            if (optionalData.isPresent())
                fluidStackAmount = pouringRecipeSource.map(fermentingRecipe -> fermentingRecipe.getFluidIngredient().getAmount(), kegMenu -> kegMenu.kegTank.getFluidAmount()) / optionalData.get().getAmount();

            stack.setCount(fluidStackAmount);
            recipeSlotToRequired.add(Pair.of(inventorySlot.getFirst(), Pair.of(stack, inventorySlot.getSecond())));
        }
        return recipeSlotToRequired;
    }

    private static void stowItems(Player player, List<Slot> inventorySlots, List<ItemStack> itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            ItemStack remainder = stowItem(inventorySlots, itemStack);
            if (!remainder.isEmpty()) {
                if (!player.getInventory().add(remainder)) {
                    player.drop(remainder, false);
                }
            }
        }
    }

    private static List<ItemStack> extractFromFluidTank(
            List<ItemStack> emptyingStacks,
            KegMenu kegMenu,
            boolean insert,
            @Nullable KegFermentingRecipe recipe
    ) {
        List<ItemStack> remainderItems = new ArrayList<>();
        KegBlockEntity blockEntity = kegMenu.blockEntity;

        for (ItemStack stack : emptyingStacks) {
            if (insert && kegMenu.kegTank.getFluidAmount() >= (recipe != null ? recipe.getFluidIngredient().getAmount() : kegMenu.kegTank.getCapacity()))
                break;
            if (!insert && kegMenu.kegTank.isEmpty())
                break;

            int toExtract = stack.getCount();
            if (recipe != null && recipe.getFluidIngredient() != null) {
                List<KegPouringRecipe> pouringRecipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING.get()).stream().filter(kegPouringRecipe -> (!insert || kegPouringRecipe.canFill()) && kegPouringRecipe.getFluid(stack).isFluidEqual(recipe.getFluidIngredient())).toList();
                Optional<KegPouringRecipe> optionalData = pouringRecipes.stream().filter(pouring -> {
                    if (pouring.isStrict())
                        return ItemStack.isSameItemSameTags(stack, pouring.getOutput());
                    return ItemStack.isSameItem(stack, pouring.getOutput());
                }).findFirst();
                if (optionalData.isPresent())
                    toExtract = recipe.getFluidIngredient().getAmount() / optionalData.get().getAmount();
            }
            var extracted = blockEntity.extractInGui(stack, toExtract);
            for (ItemStack extract : extracted) {
                if (!extract.isEmpty())
                    remainderItems.add(extract);
            }
        }

        return remainderItems;
    }

    private static List<ItemStack> clearCraftingGrid(List<Slot> craftingSlots, Player player) {
        List<ItemStack> clearedCraftingItems = new ArrayList<>();
        for (Slot craftingSlot : craftingSlots) {
            if (!craftingSlot.mayPickup(player)) {
                continue;
            }
            if (craftingSlot.hasItem()) {
                ItemStack craftingItem = craftingSlot.remove(Integer.MAX_VALUE);
                clearedCraftingItems.add(craftingItem);
            }
        }
        return clearedCraftingItems;
    }

    private static List<ItemStack> putItemsIntoCraftingGrid(
            Map<Slot, ItemStack> recipeSlotToTakenStacks
    ) {
        final int slotStackLimit = getSlotStackLimit(recipeSlotToTakenStacks);
        List<ItemStack> remainderItems = new ArrayList<>();

        recipeSlotToTakenStacks.forEach((slot, stack) -> {
            if (slot.getItem().isEmpty() && slot.mayPlace(stack)) {
                ItemStack remainder = slot.safeInsert(stack, slotStackLimit);
                if (!remainder.isEmpty()) {
                    remainderItems.add(remainder);
                }
            } else {
                remainderItems.add(stack);
            }
        });

        return remainderItems;
    }

    private static int getSlotStackLimit(
            Map<Slot, ItemStack> recipeSlotToTakenStacks
    ) {
        return recipeSlotToTakenStacks.entrySet().stream()
                .mapToInt(e -> {
                    Slot craftingSlot = e.getKey();
                    ItemStack transferItem = e.getValue();
                    if (craftingSlot.mayPlace(transferItem)) {
                        return craftingSlot.getMaxStackSize(transferItem);
                    }
                    return Integer.MAX_VALUE;
                })
                .min()
                .orElse(Integer.MAX_VALUE);
    }

    private static ItemStack stowItem(Collection<Slot> slots, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        final ItemStack remainder = stack.copy();

        for (Slot slot : slots) {
            final ItemStack inventoryStack = slot.getItem();
            if (!inventoryStack.isEmpty() && inventoryStack.isStackable()) {
                slot.safeInsert(remainder);
                if (remainder.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        for (Slot slot : slots) {
            if (slot.getItem().isEmpty()) {
                slot.safeInsert(remainder);
                if (remainder.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        return remainder;
    }

    private static Optional<Slot> getSlotWithStack(Player player, ItemStack stack, List<Slot> craftingSlots, List<Slot> inventorySlots, Slot hint) {
        return getSlotWithStack(player, craftingSlots, stack)
                .or(() -> getValidatedHintSlot(player, stack, hint))
                .or(() -> getSlotWithStack(player, inventorySlots, stack));
    }

    private static Optional<Slot> getValidatedHintSlot(Player player, ItemStack stack, Slot hint) {
        if (hint.mayPickup(player) &&
                !hint.getItem().isEmpty() &&
                ItemStack.isSameItemSameTags(stack, hint.getItem())
        ) {
            return Optional.of(hint);
        }

        return Optional.empty();
    }

    private static Optional<Slot> getSlotWithStack(Player player, Collection<Slot> slots, ItemStack itemStack) {
        return slots.stream()
                .filter(slot -> {
                    ItemStack slotStack = slot.getItem();
                    return ItemStack.isSameItemSameTags(itemStack, slotStack) &&
                            slot.mayPickup(player);
                })
                .findFirst();
    }
}
