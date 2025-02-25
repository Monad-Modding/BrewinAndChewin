package umpaz.brewinandchewin.common.block.entity.container;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.mixin.ServerPlaceRecipeAccessor;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KegPlaceRecipe extends ServerPlaceRecipe<KegRecipeWrapper> {
    private final RecipeManager manager;

    public KegPlaceRecipe(RecipeBookMenu<KegRecipeWrapper> menu, RecipeManager manager) {
        super(menu);
        this.manager = manager;
        if (menu instanceof KegMenu kegMenu)
            ((ServerPlaceRecipeAccessor)this).brewinandchewin$setStackedContents(new KegStackedContents(kegMenu, manager));
    }

    @Override
    protected void handleRecipeClicked(Recipe<KegRecipeWrapper> recipe, boolean placeAll) {
        boolean flag = this.menu.recipeMatches(recipe);

        ((KegStackedContents)stackedContents).setIgnoreFluids(true);
        int biggestCraftableStack = this.stackedContents.getBiggestCraftableStack(recipe, null);
        ((KegStackedContents)stackedContents).setIgnoreFluids(false);

        boolean shouldHandleItems = true;
        boolean shouldHandleFluid = true;

        if (flag) {
            for (int j = 0; j < this.menu.getGridHeight() * this.menu.getGridWidth() + 1; ++j) {
                if (j != this.menu.getResultSlotIndex()) {
                    ItemStack itemstack = this.menu.getSlot(j).getItem();
                    if (!itemstack.isEmpty() && Math.min(biggestCraftableStack, itemstack.getMaxStackSize()) < itemstack.getCount() + 1) {
                        shouldHandleItems = false;
                    }
                }
            }
        }

        if (menu instanceof KegMenu kegMenu && recipe instanceof KegFermentingRecipe fermentingRecipe) {
            if (fermentingRecipe.getFluidIngredient() == null && kegMenu.kegTank.isEmpty() || fermentingRecipe.getFluidIngredient() != null && kegMenu.kegTank.getFluid().isFluidEqual(fermentingRecipe.getFluidIngredient()) && kegMenu.kegTank.getFluidAmount() >= kegMenu.kegTank.getCapacity())
                shouldHandleFluid = false;
        }

        if (!shouldHandleItems && !shouldHandleFluid)
            return;

        ((KegStackedContents)stackedContents).setIgnoreItems(!shouldHandleItems);

        int stackSize = placeAll ? biggestCraftableStack : !shouldHandleFluid ? getStackSize(false, biggestCraftableStack, flag) : 1;
        IntList intlist = new IntArrayList();
        if (this.stackedContents.canCraft(recipe, intlist, stackSize)) {
            int k = stackSize;

            for (int l : intlist) {
                int i1 = StackedContents.fromStackingIndex(l).getMaxStackSize();
                if (!((KegStackedContents)stackedContents).isFluidItem(recipe, l) && i1 < k) {
                    k = i1;
                }
            }
            if (recipe instanceof KegFermentingRecipe fermentingRecipe && menu instanceof KegMenu kegMenu) {
                if (this.stackedContents.canCraft(recipe, intlist, k) && shouldHandleFluid) {
                    KegBlockEntity blockEntity = kegMenu.blockEntity;
                    FluidTank kegTank = kegMenu.kegTank;

                    if (fermentingRecipe.getFluidIngredient() == null) {
                        if (!kegTank.isEmpty()) {
                            for (int i = 0; i < inventory.items.size(); ++i) {
                                if (kegTank.isEmpty())
                                    break;
                                ItemStack stack = inventory.items.get(i);
                                List<KegPouringRecipe> pouringRecipes = manager.getAllRecipesFor(BnCRecipeTypes.KEG_POURING.get()).stream().filter(kegPouringRecipe -> kegPouringRecipe.getFluid(stack).isFluidEqual(kegTank.getFluid())).toList();
                                Optional<KegPouringRecipe> optionalData = pouringRecipes.stream().filter(pouring -> {
                                    if (pouring.isStrict())
                                        return ItemStack.isSameItemSameTags(stack, pouring.getContainer());
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
                        boolean shouldRemoveIndex = !kegTank.isEmpty() && !kegTank.getFluid().isFluidEqual(fermentingRecipe.getFluidIngredient()) || kegTank.getFluidAmount() < fermentingRecipe.getFluidIngredient().getAmount();

                        if (!kegTank.isEmpty() && !kegTank.getFluid().isFluidEqual(fermentingRecipe.getFluidIngredient())) {
                            int fluidToExtract = kegTank.getFluidAmount();
                            for (int i = 0; i < inventory.items.size(); ++i) {
                                ItemStack stack = inventory.items.get(i);
                                List<KegPouringRecipe> pouringRecipes = manager.getAllRecipesFor(BnCRecipeTypes.KEG_POURING.get()).stream().filter(kegPouringRecipe -> kegPouringRecipe.getFluid(stack).isFluidEqual(kegTank.getFluid())).toList();
                                Optional<KegPouringRecipe> optionalData = pouringRecipes.stream().filter(pouring -> {
                                    if (stack.isEmpty())
                                        return false;
                                    if (pouring.isStrict())
                                        return ItemStack.isSameItemSameTags(stack, pouring.getContainer());
                                    return ItemStack.isSameItem(stack, pouring.getContainer());
                                }).findFirst();
                                if (optionalData.isPresent()) {
                                    int itemAmount = Math.min(fermentingRecipe.getFluidIngredient().getAmount() / optionalData.get().getAmount(), stack.getCount());
                                    ItemStack inputStack = optionalData.get().getResultItem(blockEntity.getLevel().registryAccess());
                                    if (extractItems.stream().anyMatch(recipeItem -> recipeItem.fluidAmount > optionalData.get().getAmount()))
                                        continue;
                                    if (extractItems.stream().anyMatch(recipeItem -> recipeItem.fluidAmount < optionalData.get().getAmount())) {
                                        fluidToExtract = kegTank.getFluidAmount();
                                        extractItems.clear();
                                    }
                                    if (fluidToExtract <= 0)
                                        continue;
                                    extractItems.add(new RecipeItem(i, itemAmount, optionalData.get().getAmount() * itemAmount, optionalData.get().getContainer().copyWithCount(itemAmount), inputStack.copyWithCount(itemAmount)));
                                    fluidToExtract -= optionalData.get().getAmount() * itemAmount;
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
                            List<KegPouringRecipe> pouringRecipes = manager.getAllRecipesFor(BnCRecipeTypes.KEG_POURING.get()).stream().filter(kegPouringRecipe -> kegPouringRecipe.canFill() && kegPouringRecipe.getFluid(stack).isFluidEqual(fermentingRecipe.getFluidIngredient())).toList();
                            Optional<KegPouringRecipe> optionalData = pouringRecipes.stream().filter(pouring -> {
                                if (pouring.isStrict())
                                    return ItemStack.isSameItemSameTags(stack, pouring.getOutput());
                                return ItemStack.isSameItem(stack, pouring.getOutput());
                            }).findFirst();
                            if (optionalData.isPresent()) {
                                int itemAmount = Mth.clamp(((fermentingRecipe.getFluidIngredient().getAmount() / optionalData.get().getAmount()) - ((kegTank.getFluidAmount() % fermentingRecipe.getFluidIngredient().getAmount()) / optionalData.get().getAmount())) * k, 1, Math.min(stack.getCount(), kegTank.getCapacity() / optionalData.get().getAmount()));
                                ItemStack outputStack = optionalData.get().getOutput().copyWithCount(itemAmount);
                                if (insertItems.stream().anyMatch(recipeItem -> recipeItem.fluidAmount > optionalData.get().getAmount()))
                                    continue;
                                if (insertItems.stream().anyMatch(recipeItem -> recipeItem.fluidAmount < optionalData.get().getAmount())) {
                                    fluidToInsert = 0;
                                    insertItems.clear();
                                }
                                if (fluidToInsert >= fermentingRecipe.getFluidIngredient().getAmount())
                                    continue;
                                insertItems.add(new RecipeItem(i, itemAmount, optionalData.get().getAmount() * itemAmount, optionalData.get().getContainer().copy(), outputStack));
                                fluidToInsert += optionalData.get().getAmount() * itemAmount;
                            }
                        }
                        int endFluidAmount = fluidToInsert + (!kegTank.getFluid().isFluidEqual(fermentingRecipe.getFluidIngredient()) ? 0 : kegTank.getFluidAmount());
                        if (endFluidAmount % fermentingRecipe.getFluidIngredient().getAmount() != 0) {
                            int itemCount = endFluidAmount / fermentingRecipe.getFluidIngredient().getAmount();
                            for (int i = 0; i < itemCount; ++i) {
                                if (itemCount % fermentingRecipe.getFluidIngredient().getAmount() == 0)
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
        }
        ((KegStackedContents)stackedContents).setIgnoreItems(false);
    }

    private record RecipeItem(int slot, int maxInsert, int fluidAmount, ItemStack container, ItemStack output) {}
}
