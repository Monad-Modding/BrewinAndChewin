package umpaz.brewinandchewin.common.block.entity.container;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.mixin.ServerPlaceRecipeAccessor;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.KegContainer;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KegPlaceRecipe extends ServerPlaceRecipe<KegContainer> {
    private final RecipeManager manager;

    public KegPlaceRecipe(KegMenu menu, RecipeManager manager) {
        super(menu);
        this.manager = manager;
        ((ServerPlaceRecipeAccessor)this).brewinandchewin$setStackedContents(new KegStackedContents(menu, manager));
    }

    @Override
    protected void handleRecipeClicked(Recipe<KegContainer> recipe, boolean placeAll) {
        KegFermentingRecipe fermentingRecipe = (KegFermentingRecipe) recipe;
        KegMenu menu = (KegMenu) this.menu;
        boolean flag = menu.recipeMatches(recipe);

        ((KegStackedContents) stackedContents).setIgnoreFluids(true);
        int biggestCraftableStack = this.stackedContents.getBiggestCraftableStack(recipe, null);
        ((KegStackedContents) stackedContents).setIgnoreFluids(false);

        boolean shouldHandleItems = true;
        boolean shouldHandleFluid = true;

        if (flag) {
            for (int j = 0; j < menu.getGridHeight() * menu.getGridWidth() + 1; ++j) {
                if (j != menu.getResultSlotIndex()) {
                    ItemStack itemstack = menu.getSlot(j).getItem();
                    if (!itemstack.isEmpty() && Math.min(biggestCraftableStack, itemstack.getMaxStackSize()) < itemstack.getCount() + 1) {
                        shouldHandleItems = false;
                    }
                }
            }
        }

        if (fermentingRecipe.getFluidIngredient().isEmpty() && menu.kegTank.isEmpty() || fermentingRecipe.getFluidIngredient().isPresent() && fermentingRecipe.getFluidIngredient().get().ingredient().matches(menu.kegTank.getAbstractedFluid()) && menu.kegTank.getAbstractedFluid().amount() >= menu.kegTank.getFluidCapacity())
            shouldHandleFluid = false;

        if (!shouldHandleItems && !shouldHandleFluid)
            return;

        ((KegStackedContents) stackedContents).setIgnoreItems(!shouldHandleItems);

        int stackSize = placeAll ? biggestCraftableStack : !shouldHandleFluid ? getStackSize(false, biggestCraftableStack, flag) : 1;
        IntList intlist = new IntArrayList();
        if (stackedContents.canCraft(recipe, intlist, stackSize)) {
            int k = stackSize;

            for (int l : intlist) {
                int i1 = StackedContents.fromStackingIndex(l).getMaxStackSize();
                if (!((KegStackedContents) stackedContents).isFluidItem(recipe, l) && i1 < k) {
                    k = i1;
                }
            }
            if (stackedContents.canCraft(fermentingRecipe, intlist, k) && shouldHandleFluid) {
                KegBlockEntity blockEntity = menu.blockEntity;
                AbstractedFluidTank kegTank = menu.kegTank;

                if (fermentingRecipe.getFluidIngredient().isEmpty()) {
                    if (!kegTank.isEmpty()) {
                        for (int i = 0; i < inventory.items.size(); ++i) {
                            if (kegTank.isEmpty())
                                break;
                            ItemStack stack = inventory.items.get(i);
                            List<KegPouringRecipe> pouringRecipes = manager.getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().map(RecipeHolder::value).filter(kegPouringRecipe -> kegPouringRecipe.getFluid(stack).matches(kegTank.getAbstractedFluid())).toList();
                            Optional<KegPouringRecipe> optionalData = pouringRecipes.stream().filter(pouring -> {
                                if (pouring.isStrict())
                                    return ItemStack.isSameItemSameComponents(stack, pouring.getContainer());
                                return ItemStack.isSameItem(stack, pouring.getContainer());
                            }).findFirst();
                            if (optionalData.isPresent()) {
                                int finalI = i;
                                blockEntity.extractInGui(stack, stack.getCount()).forEach(s -> {
                                    if (!inventory.add(inventory.items.get(finalI).isEmpty() ? finalI : inventory.getSlotWithRemainingSpace(s), s))
                                        inventory.player.drop(s, false);
                                });
                            }
                        }
                    }
                } else {
                    List<RecipeItem> extractItems = new ArrayList<>();
                    boolean shouldRemoveIndex = !kegTank.isEmpty() && !fermentingRecipe.getFluidIngredient().get().ingredient().matches(kegTank.getAbstractedFluid()) || kegTank.getAbstractedFluid().amount() < fermentingRecipe.getFluidIngredient().get().amount();

                    if (!kegTank.isEmpty() && !fermentingRecipe.getFluidIngredient().get().ingredient().matches(kegTank.getAbstractedFluid())) {
                        long fluidToExtract = kegTank.getAbstractedFluid().amount();
                        for (int i = 0; i < inventory.items.size(); ++i) {
                            ItemStack stack = inventory.items.get(i);
                            List<KegPouringRecipe> pouringRecipes = manager.getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().map(RecipeHolder::value).filter(kegPouringRecipe -> kegPouringRecipe.getFluid(stack).matches(kegTank.getAbstractedFluid())).toList();
                            Optional<KegPouringRecipe> optionalData = pouringRecipes.stream().filter(pouring -> {
                                if (stack.isEmpty())
                                    return false;
                                if (pouring.isStrict())
                                    return ItemStack.isSameItemSameComponents(stack, pouring.getContainer());
                                return ItemStack.isSameItem(stack, pouring.getContainer());
                            }).findFirst();
                            if (optionalData.isPresent()) {
                                int itemAmount = (int) Math.min(fermentingRecipe.getFluidIngredient().get().amount() / optionalData.get().getRawFluid().amount(), stack.getCount());
                                ItemStack inputStack = optionalData.get().getResultItem(blockEntity.getLevel().registryAccess());
                                if (extractItems.stream().anyMatch(recipeItem -> recipeItem.fluidAmount > optionalData.get().getRawFluid().amount()))
                                    continue;
                                if (extractItems.stream().anyMatch(recipeItem -> recipeItem.fluidAmount < optionalData.get().getRawFluid().amount())) {
                                    fluidToExtract = kegTank.getAbstractedFluid().amount();
                                    extractItems.clear();
                                }
                                if (fluidToExtract <= 0)
                                    continue;
                                extractItems.add(new RecipeItem(i, itemAmount, optionalData.get().getRawFluid().amount() * itemAmount, optionalData.get().getContainer().copyWithCount(itemAmount), inputStack.copyWithCount(itemAmount)));
                                fluidToExtract -= optionalData.get().getRawFluid().amount() * itemAmount;
                            }
                        }

                        if (fluidToExtract > 0)
                            return;

                        List<RecipeItem> temporaryExtracts = List.copyOf(extractItems);
                        extractItems.clear();
                        for (RecipeItem extractItem : temporaryExtracts) {
                            inventory.items.get(extractItem.slot).shrink(extractItem.maxInsert);
                            ItemStack copiedContainer = extractItem.container.copy();
                            List<ItemStack> extracted = blockEntity.extractInGui(extractItem.container, extractItem.maxInsert);
                            extractItems.addAll(extracted.stream().map(stack -> new RecipeItem(extractItem.slot, extractItem.maxInsert, extractItem.fluidAmount, copiedContainer, stack)).toList());
                        }
                        if (!extractItems.isEmpty())
                            intlist.removeInt(intlist.size() - 1);
                    }

                    List<RecipeItem> insertItems = new ArrayList<>();
                    int fluidToInsert = 0;
                    for (int i = 0; i < inventory.items.size(); ++i) {
                        ItemStack stack = inventory.items.get(i);
                        List<KegPouringRecipe> pouringRecipes = manager.getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().map(RecipeHolder::value).filter(kegPouringRecipe -> kegPouringRecipe.canFill() && fermentingRecipe.getFluidIngredient().get().ingredient().matches(kegPouringRecipe.getFluid(stack))).toList();
                        Optional<KegPouringRecipe> optionalData = pouringRecipes.stream().filter(pouring -> {
                            if (pouring.isStrict())
                                return ItemStack.isSameItemSameComponents(stack, pouring.getOutput());
                            return ItemStack.isSameItem(stack, pouring.getOutput());
                        }).findFirst();
                        if (optionalData.isPresent()) {
                            int itemAmount = (int) Mth.clamp(((fermentingRecipe.getFluidIngredient().get().amount() / optionalData.get().getRawFluid().amount()) - ((kegTank.getAbstractedFluid().amount() % fermentingRecipe.getFluidIngredient().get().amount()) / optionalData.get().getRawFluid().amount())) * k, 1, Math.min(stack.getCount(), kegTank.getFluidCapacity() / optionalData.get().getRawFluid().amount()));
                            ItemStack outputStack = optionalData.get().getOutput().copyWithCount(itemAmount);
                            if (insertItems.stream().anyMatch(recipeItem -> recipeItem.fluidAmount > optionalData.get().getRawFluid().amount()))
                                continue;
                            if (insertItems.stream().anyMatch(recipeItem -> recipeItem.fluidAmount < optionalData.get().getRawFluid().amount())) {
                                fluidToInsert = 0;
                                insertItems.clear();
                            }
                            if (fluidToInsert >= fermentingRecipe.getFluidIngredient().get().amount())
                                continue;
                            insertItems.add(new RecipeItem(i, itemAmount, optionalData.get().getRawFluid().amount() * itemAmount, optionalData.get().getContainer().copy(), outputStack));
                            fluidToInsert += optionalData.get().getRawFluid().amount() * itemAmount;
                        }
                    }
                    long endFluidAmount = fluidToInsert + (!fermentingRecipe.getFluidIngredient().get().ingredient().matches(kegTank.getAbstractedFluid()) ? 0 : kegTank.getAbstractedFluid().amount());
                    if (endFluidAmount % fermentingRecipe.getFluidIngredient().get().amount() != 0) {
                        int itemCount = (int) (endFluidAmount / fermentingRecipe.getFluidIngredient().get().amount());
                        for (int i = 0; i < itemCount; ++i) {
                            if (itemCount % fermentingRecipe.getFluidIngredient().get().amount() == 0)
                                break;
                            insertItems.remove(insertItems.size() - 1);
                            --itemCount;
                        }
                    }
                    if (!insertItems.isEmpty()) {
                        List<RecipeItem> temporaryInserts = List.copyOf(insertItems);
                        insertItems.clear();
                        for (RecipeItem insertItem : temporaryInserts) {
                            inventory.items.get(insertItem.slot).shrink(insertItem.maxInsert);
                            ItemStack copiedOutput = insertItem.output.copy();
                            List<ItemStack> inserted = blockEntity.extractInGui(insertItem.output, insertItem.maxInsert);
                            insertItems.addAll(inserted.stream().map(stack -> new RecipeItem(insertItem.slot, insertItem.maxInsert, insertItem.fluidAmount, copiedOutput, stack)).toList());
                        }

                        if (!extractItems.isEmpty())
                            insertItems.addAll(extractItems);

                        insertItems.removeIf(insertItem -> insertItem.output == null || insertItem.container.isEmpty());
                        insertItems.forEach(e -> {
                            if (!inventory.add(inventory.items.get(e.slot).isEmpty() ? e.slot : inventory.getSlotWithRemainingSpace(e.output), e.output))
                                inventory.player.drop(e.output, false);
                        });
                        if (shouldRemoveIndex)
                            intlist.removeInt(intlist.size() - 1);
                    }
                }
            }
            if (shouldHandleItems) {
                this.clearGrid();
                this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe, intlist.iterator(), k);
            }
        }
        ((KegStackedContents) stackedContents).setIgnoreItems(false);
    }

    private record RecipeItem(int slot, int maxInsert, long fluidAmount, ItemStack container, ItemStack output) {}
}
