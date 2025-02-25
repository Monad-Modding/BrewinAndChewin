package umpaz.brewinandchewin.integration.jei.transfer;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
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
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.network.BnCNetworkHandler;
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
 * <br>
 * JEI is licensed under the MIT license.
 * <a href="https://github.com/mezz/JustEnoughItems/blob/1.21.x/LICENSE.txt">You may read the license here.</a>
 * {@see mezz.jei.library.transfer.BasicRecipeTransferHandler}
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
            return Optional.of(BnCMenuTypes.KEG.get());
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

        public Handler(IRecipeTransferHandlerHelper helper, IStackHelper stackHelper) {
            this.helper = helper;
            this.stackHelper = stackHelper;
        }

        @Override
        public Class<? extends KegMenu> getContainerClass() {
            return KegMenu.class;
        }

        @Override
        public Optional<MenuType<KegMenu>> getMenuType() {
            return Optional.of(BnCMenuTypes.KEG.get());
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
                    recipe.getFluidIngredient() != null ?
                            view.getSlotViews(RecipeIngredientRole.INPUT).stream().filter(ingredientView -> ingredientView.getAllIngredients().anyMatch(ingredient -> ingredient.getIngredient(ForgeTypes.FLUID_STACK).isPresent())).findFirst().orElse(null) :
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
                BnCNetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new JEITransferKegRecipeServerboundPacket(
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
            int fluidCapacity = 0;
            int largestFluidCapacity = 0;
            int largestEmptyCapacity = 0;

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


                if (!menu.kegTank.isEmpty() && (!maxTransfer || recipe.getFluidIngredient() == null || !menu.kegTank.getFluid().isFluidEqual(recipe.getFluidIngredient()))) {
                    List<KegPouringRecipe> pouringRecipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING.get()).stream().filter(kegPouringRecipe -> kegPouringRecipe.getFluid(slotTuple.getValue()).isFluidEqual(menu.kegTank.getFluid())).toList();
                    Optional<KegPouringRecipe> optionalData = pouringRecipes.stream().filter(pouring -> {
                        if (pouring.isStrict())
                            return ItemStack.isSameItemSameTags(slotTuple.getValue(), pouring.getContainer());
                        return ItemStack.isSameItem(slotTuple.getValue(), pouring.getContainer());
                    }).findFirst();
                    if (optionalData.isPresent()) {
                        if (optionalData.get().getAmount() <= menu.kegTank.getFluidAmount()) {
                            int shrinkAmount = menu.kegTank.getFluidAmount() / optionalData.get().getAmount();
                            emptyingSlots
                                    .computeIfAbsent(slotTuple.getValue(), it -> new ArrayList<>())
                                    .add(new SlotReference(slotTuple.getKey(), slotTuple.getValue(), optionalData.get().getAmount() * shrinkAmount, shrinkAmount));
                        }
                        if (recipe.getFluidIngredient() != null && menu.kegTank.getFluid().isFluidEqual(recipe.getFluidIngredient())) {
                            if (optionalData.get().getAmount() <= recipe.getFluidIngredient().getAmount() && fluidCapacity < recipe.getFluidIngredient().getAmount()) {
                                if (optionalData.get().getAmount() > largestEmptyCapacity) {
                                    relevantSlots.remove(requiredFluidStack);
                                }
                                int shrinkAmount = recipe.getFluidIngredient().getAmount() / optionalData.get().getAmount();
                                largestEmptyCapacity = optionalData.get().getAmount();
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
                                        .add(new SlotReference(slotTuple.getKey(), optionalData.get().getResultItem(Minecraft.getInstance().level.registryAccess()).copyWithCount(shrinkAmount), optionalData.get().getAmount() * shrinkAmount, shrinkAmount));
                            }
                        }
                    }
                }

                if (recipe.getFluidIngredient() != null && !requiredFluidStack.isEmpty() && requiredFluidStack.getIngredients(ForgeTypes.FLUID_STACK).findFirst().isPresent()) {
                    List<KegPouringRecipe> pouringRecipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING.get()).stream().filter(kegPouringRecipe -> kegPouringRecipe.canFill() && kegPouringRecipe.getFluid(slotTuple.getValue()).isFluidEqual(recipe.getFluidIngredient())).toList();
                    Optional<KegPouringRecipe> optionalData = pouringRecipes.stream().filter(pouring -> {
                        if (pouring.isStrict())
                            return ItemStack.isSameItemSameTags(slotTuple.getValue(), pouring.getOutput());
                        return ItemStack.isSameItem(slotTuple.getValue(), pouring.getOutput());
                    }).findFirst();
                    int tankAmount = menu.kegTank.getFluid().isFluidEqual(recipe.getFluidIngredient()) ? menu.kegTank.getFluidAmount() : 0;
                    if (optionalData.isPresent()) {
                        if (optionalData.get().getAmount() <= menu.kegTank.getCapacity() - tankAmount && fluidCapacity < menu.kegTank.getCapacity() - tankAmount) {
                            if (optionalData.get().getAmount() > largestFluidCapacity) {
                                relevantSlots.remove(requiredFluidStack);
                            }
                            int shrinkAmount = (recipe.getFluidIngredient().getAmount() / optionalData.get().getAmount()) - ((tankAmount % recipe.getFluidIngredient().getAmount()) / optionalData.get().getAmount());
                            largestFluidCapacity = optionalData.get().getAmount();
                            fluidCapacity += optionalData.get().getAmount() * shrinkAmount;
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
                                    .add(new SlotReference(slotTuple.getKey(), slotTuple.getValue(), optionalData.get().getAmount() * shrinkAmount, shrinkAmount));
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
                                Integer.compare(o1.fluidAmount, o2.fluidAmount) :
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
                            Integer.compare(o1.fluidAmount, o2.fluidAmount) :
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

            if (requiredFluidStack != null && recipe.getFluidIngredient() != null) {
                int amountToFill = Math.max((maxTransfer ? menu.kegTank.getCapacity() : recipe.getFluidIngredient().getAmount()) - (menu.kegTank.getFluid().isFluidEqual(recipe.getFluidIngredient()) ? menu.kegTank.getFluidAmount() : 0), 0);

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
                            amountToFill -= matching.fluidAmount;
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
                int amountToEmpty = menu.kegTank.getFluidAmount();

                List<SlotReference> allMatching = emptyingBestMatches
                        .stream()
                        .flatMap(pairs -> pairs.stream().filter(p -> !p.stack.isEmpty() && p.fluidAmount != null))
                        .toList();

                if (allMatching.isEmpty() && !menu.kegTank.isEmpty() && (recipe.getFluidIngredient() == null || !menu.kegTank.getFluid().isFluidEqual(recipe.getFluidIngredient()))) {
                    operations.canEmpty = false;
                } else {
                    List<SlotReference> toShrink = new ArrayList<>();
                    for (SlotReference matching : allMatching) {
                        if (amountToEmpty <= 0)
                            break;
                        toShrink.add(matching);
                        operations.emptyingResults.add(Pair.of(matching.slot, matching.fluidAmount));
                        amountToEmpty -= matching.fluidAmount;
                    }
                    if (amountToEmpty > 0 && !menu.kegTank.isEmpty() && (recipe.getFluidIngredient() == null || !menu.kegTank.getFluid().isFluidEqual(recipe.getFluidIngredient()))) {
                        operations.emptyingResults.clear();
                        operations.canEmpty = false;
                    } else
                        toShrink.forEach(slotReference -> slotReference.stack.shrink(slotReference.shrinkAmount));
                }
            }

            if (!menu.kegTank.isEmpty() && emptyingBestMatches.isEmpty() && (recipe.getFluidIngredient() == null || !menu.kegTank.getFluid().isFluidEqual(recipe.getFluidIngredient()) || recipe.getFluidIngredient() != null && menu.kegTank.getFluidAmount() % recipe.getFluidIngredient().getAmount() != 0)) {
                operations.canEmpty = false;
            }

            return operations;
        }
    }

    private record SlotReference(
            Slot slot,
            ItemStack stack,
            @Nullable Integer fluidAmount,
            int shrinkAmount
    ) {}

    public static class TransferOperations {
        public final List<Pair<Slot, Slot>> results = new ArrayList<>();
        public final List<Pair<Slot, Integer>> fluidResults = new ArrayList<>();
        public final List<Pair<Slot, Integer>> emptyingResults = new ArrayList<>();

        public final List<IRecipeSlotView> missingItems = new ArrayList<>();
        public boolean canEmpty = true;
        public boolean notEnoughFluid = false;
        public boolean invalidFluid = false;

        public static TransferOperations readFromIntegers(List<Pair<Integer, Integer>> resultSlots,
                                                          List<Pair<Integer, Integer>> fluidSlots,
                                                          List<Pair<Integer, Integer>> emptyingSlots,
                                                          AbstractContainerMenu menu) {
            TransferOperations operations = new TransferOperations();
            for (Pair<Integer, Integer> resultSlot : resultSlots) {
                int inventorySlotIndex = resultSlot.getFirst();
                int craftingSlotIndex = resultSlot.getSecond();
                operations.results.add(Pair.of(menu.getSlot(inventorySlotIndex), menu.getSlot(craftingSlotIndex)));
            }
            for (Pair<Integer, Integer> fluidSlot : fluidSlots) {
                int fluidSlotIndex = fluidSlot.getFirst();
                int fluidAmount = fluidSlot.getSecond();
                operations.fluidResults.add(Pair.of(menu.getSlot(fluidSlotIndex), fluidAmount));
            }
            for (Pair<Integer, Integer> emptyingSlot : emptyingSlots) {
                int emptyingSlotIndex = emptyingSlot.getFirst();
                int fluidAmount = emptyingSlot.getSecond();
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
