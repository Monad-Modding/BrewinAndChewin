package umpaz.brewinandchewin.common.block.entity;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.block.KegBlock;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.block.entity.container.SidedKegWrapper;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.tag.BnCTags;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;
import vectorwing.farmersdelight.common.tag.ModTags;
import vectorwing.farmersdelight.common.utility.ItemUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class KegBlockEntity extends SyncedBlockEntity implements MenuProvider, Nameable, RecipeCraftingHolder {

    public static final int CONTAINER_SLOT = 4;
    public static final int OUTPUT_SLOT = 5;
    public static final int INVENTORY_SIZE = OUTPUT_SLOT + 1;
    public static final int RANGE = 2;

    private final AbstractedItemHandler inventory;
    private final SidedKegWrapper inputHandler;
    private final SidedKegWrapper outputHandler;
    private final AbstractedFluidTank fluidTank;
    private final KegRecipeWrapper recipeWrapper;

    private int fermentTime;
    private int fermentTimeTotal;
    private Component customName;

    private boolean deferFluidExtraction = false;
    private boolean currentlyOperating = false;
    public int kegTemperature;
    private boolean initialisedTemperature = false;

    protected final ContainerData kegData;
    private final Object2IntOpenHashMap<ResourceLocation> usedRecipeTracker;

    private ResourceLocation lastRecipeID;
    private boolean checkNewRecipe;

    public KegBlockEntity(BlockPos pos, BlockState state) {
        super(BnCBlockEntityTypes.KEG, pos, state);
        this.inventory = createHandler();
        this.inputHandler = BrewinAndChewin.getHelper().createSidedKegWrapper(inventory, Direction.UP);
        this.outputHandler = BrewinAndChewin.getHelper().createSidedKegWrapper(inventory, Direction.DOWN);
        this.fluidTank = createFluidTank();
        this.kegData = createIntArray();
        this.usedRecipeTracker = new Object2IntOpenHashMap<>();
        this.checkNewRecipe = true;
        this.recipeWrapper = BrewinAndChewin.getHelper().createRecipeWrapper(inventory, fluidTank);
    }

    @Override
    public void loadAdditional(CompoundTag compound, HolderLookup.Provider provider) {
        super.loadAdditional(compound, provider);
        inventory.readFromNbt(compound.getCompound("Inventory"), provider);
        fluidTank.readFromNbt(compound.getCompound("FluidTank"), provider);
        fermentTime = compound.getInt("FermentTime");
        fermentTimeTotal = compound.getInt("FermentTimeTotal");
        if (compound.contains("CustomName", 8)) {
            customName = Component.Serializer.fromJson(compound.getString("CustomName"), provider);
        }
        CompoundTag compoundRecipes = compound.getCompound("RecipesUsed");
        for (String key : compoundRecipes.getAllKeys()) {
            usedRecipeTracker.put(ResourceLocation.tryParse(key), compoundRecipes.getInt(key));
        }
        if (compound.contains("Temperature", Tag.TAG_INT))
            kegTemperature = compound.getInt("Temperature");
        checkNewRecipe = true;
    }

    public static AbstractedFluidStack getMealFromItem(ItemStack kegStack, HolderLookup.Provider provider) {
        if (!kegStack.is(BnCItems.KEG)) {
            return AbstractedFluidStack.EMPTY;
        }

        CustomData data = kegStack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        if (!tag.isEmpty()) {
            if (tag.contains("FluidTank", Tag.TAG_COMPOUND)) {
                return BrewinAndChewin.getHelper().deserializeTankFluidStack(tag.getCompound("FluidTank"), provider);
            }
        }

        return AbstractedFluidStack.EMPTY;
    }

    public AbstractedFluidStack getOutput() {
        return fluidTank.getAbstractedFluid();
    }

    public SidedKegWrapper getSidedHandler(Direction direction) {
        if (direction == Direction.UP)
            return inputHandler;
        return outputHandler;
    }

    public CustomData writeMeal(CompoundTag tag, HolderLookup.Provider provider) {
        writeDrink(tag, provider);
        return CustomData.of(tag);
    }


    @Override
    public void saveAdditional(CompoundTag compound, HolderLookup.Provider provider) {
        super.saveAdditional(compound, provider);
        compound.put("Inventory", inventory.writeToNbt(provider));
        compound.put("FluidTank", fluidTank.writeToNbt(provider));
        compound.putInt("FermentTime", fermentTime);
        compound.putInt("FermentTimeTotal", fermentTimeTotal);
        if (customName != null) {
            compound.putString("CustomName", Component.Serializer.toJson(customName, provider));
        }
        CompoundTag compoundRecipes = new CompoundTag();
        usedRecipeTracker.forEach((recipeId, craftedAmount) -> compoundRecipes.putInt(recipeId.toString(), craftedAmount));
        compound.put("RecipesUsed", compoundRecipes);
    }

    private CompoundTag writeUpdateTag(CompoundTag compound, HolderLookup.Provider provider) {
        super.saveAdditional(compound, provider);
        compound.put("Inventory", inventory.writeToNbt(provider));
        compound.put("FluidTank", fluidTank.writeToNbt(provider));
        compound.putInt("FermentTime", fermentTime);
        compound.putInt("FermentTimeTotal", fermentTimeTotal);
        compound.putInt("Temperature", kegTemperature);
        return compound;
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        components.set(DataComponents.CUSTOM_DATA, writeMeal(new CompoundTag(), level.registryAccess()));
    }

    public CompoundTag writeDrink(CompoundTag compound, HolderLookup.Provider provider) {
        compound.putString("id", BnCBlockEntityTypes.KEG.builtInRegistryHolder().getRegisteredName());
        if (customName != null) {
            compound.putString("CustomName", Component.Serializer.toJson(customName, provider));
        }
        if (!fluidTank.isEmpty()) {
            compound.put("FluidTank", this.fluidTank.writeToNbt(provider));
        }
        return compound;
    }


    public static boolean isValidTemp(int kegTemp, int want) {
        return switch (want) {
            case 1 -> kegTemp <= 1;
            case 2 -> kegTemp <= 2;
            case 3 -> kegTemp < 5 && kegTemp > 1;
            case 4 -> kegTemp >= 4;
            case 5 -> kegTemp >= 5;
            default -> false;
        };
    }
    private boolean inventoryContainsOnlyIngredients(KegFermentingRecipe recipe) {
        List<Ingredient> ingredients = new ArrayList<>(recipe.getIngredients());
        for (int i = 0; i < CONTAINER_SLOT; ++i) { // self reminder: Output slot has index number 5, container slot has index number 4, so 0,1,2,3 are the crafting grid ~Oska
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            Optional<Ingredient> match = ingredients.stream()
                    .filter(ingredient -> ingredient.test(stack))
                    .findFirst();
            if (match.isPresent()) {
                ingredients.remove(match.get());
            } else {
                return false;
            }
        }
        return true;
    }
    protected boolean canFerment(KegFermentingRecipe recipe, KegBlockEntity keg) {
        if (!hasInput()) return false;
        if (level == null) return false;
        if (!isValidTemp(keg.getTemperature(), recipe.getTemperature()))
            return false; // make sure the temperature is valid
        if (!recipe.getIngredients().isEmpty() && !inventoryContainsOnlyIngredients(recipe)){
            return false;
        }

        if (recipe.getFluidIngredient().isEmpty()) { // if the recipe does not require a fluid
            return keg.fluidTank.isEmpty(); // make sure the fluid is empty
        }
        if (!recipe.getFluidIngredient().get().ingredient().matches(keg.fluidTank.getAbstractedFluid()))
            return false; // make sure the fluid is the same
        return keg.fluidTank.getAbstractedFluid().amount() % recipe.getFluidIngredient().get().amount() == 0; // make sure the fluid amount is a multiple of the recipe amount
    }

    public static void fermentingTick(Level level, BlockPos pos, BlockState state, KegBlockEntity keg) {
        boolean didInventoryChange = false;

        if (level.getGameTime() % 80 == 0) // Every 4s
            keg.updateTemperature();

        if (keg.deferFluidExtraction) {
            keg.deferFluidExtraction = false;
            List<ItemStack> out = keg.extractInGui(keg.inventory.getStackInSlot(CONTAINER_SLOT), keg.inventory.getSlotLimit(OUTPUT_SLOT));
            if (!out.isEmpty())
                keg.inventory.insertItem(OUTPUT_SLOT, out.getFirst(), false);
        }


        if (keg.hasInput()) {
            Optional<RecipeHolder<KegFermentingRecipe>> recipe = keg.getMatchingRecipe(keg.recipeWrapper);
            if (recipe.isPresent()) {
                if (keg.canFerment(recipe.get().value(), keg)) {
                    didInventoryChange = keg.processFermenting(recipe.get().value(), keg);
                } else {
                    keg.fermentTime = Math.max(0, keg.fermentTime - 20);
                }
            } else {
                keg.fermentTime = Math.max(0, keg.fermentTime - 20);
            }
        } else if (keg.fermentTime > 0) {
            keg.fermentTime = Math.max(0, keg.fermentTime - 20);
        }

        if (didInventoryChange) {
            keg.inventoryChanged();
        }
        if (keg.isFermenting()) {
            keg.bubbleTickCounter++;
            // Every 10 ticks/0.5 seconds, this has been done to keep TPS lightweight
            //while initially this was set to 60 it appeared to be not very visible
            if (keg.bubbleTickCounter >= 15) {
                keg.bubbleTickCounter = 0;
                level.sendBlockUpdated(pos, state, state, 3);
            }
        } else {
            keg.bubbleTickCounter = 0;
        }

    }

    public Optional<RecipeHolder<KegFermentingRecipe>> getRecipeWithoutTemperature() {
        if (!hasInput())
            return Optional.empty();
        Optional<RecipeHolder<KegFermentingRecipe>> recipe = getMatchingRecipe(recipeWrapper);
        if (recipe.isEmpty())
            return Optional.empty();
        if (recipe.get().value().getFluidIngredient().isEmpty()) { // if the recipe does not require a fluid
            if (!fluidTank.isEmpty()) // make sure the fluid is empty
                return Optional.empty();
        } else {
            if (!recipe.get().value().getFluidIngredient().get().ingredient().matches(fluidTank.getAbstractedFluid()))
                return Optional.empty(); // make sure the fluid is the same
            if (fluidTank.getAbstractedFluid().amount() % recipe.get().value().getFluidIngredient().get().amount() != 0) // make sure the fluid amount is a multiple of the recipe amount
                return Optional.empty();
        }
        return recipe;
    }

    private Optional<RecipeHolder<KegFermentingRecipe>> getMatchingRecipe(KegRecipeWrapper inventoryWrapper) {
        if (level == null) return Optional.empty();

        if (checkNewRecipe) {
            Optional<RecipeHolder<KegFermentingRecipe>> recipe = level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.FERMENTING).stream().filter(a -> a.value().matches(inventoryWrapper, level)).findFirst();
            if (recipe.isPresent()) {
                ResourceLocation newRecipeID = recipe.get().id();
                if (lastRecipeID != null && !lastRecipeID.equals(newRecipeID)) {
                    fermentTime = 0;
                }
                lastRecipeID = newRecipeID;
                return recipe;
            }
        }
        checkNewRecipe = false;

        if (lastRecipeID != null) {
            Optional<RecipeHolder<KegFermentingRecipe>> recipe = level.getRecipeManager()
                    .getRecipeFor(BnCRecipeTypes.FERMENTING, inventoryWrapper, level, lastRecipeID);
            if (recipe.isPresent() && recipe.get().value().matches(inventoryWrapper, level)) {
                return recipe;
            }
        }

        return Optional.empty();
    }

    private boolean hasInput() {
        for (int i = 0; i < OUTPUT_SLOT; ++i) {
            if (!inventory.getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    private boolean processFermenting(KegFermentingRecipe recipe, KegBlockEntity keg) {
        if (level == null) return false;

        ++fermentTime;
        fermentTimeTotal = recipe.getFermentTime();
        if (fermentTime < fermentTimeTotal) {
            setChanged();
            return false;
        }


        fermentTime = 0;
        if (recipe.getResult().left().isPresent()) {
            deferFluidExtraction = true;
            keg.fluidTank.setAbstractedFluid(recipe.getResult().left().get());
            if (!keg.level.isClientSide()) {
                Vec3 center = keg.getBlockPos().getCenter();
                keg.level.playSound(null, center.x(), center.y(), center.z(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.6f, 0.8f);
            }
        }

        if (recipe.getResult().right().isPresent()) {
            if (recipe.getFluidIngredient().isPresent())
                keg.fluidTank.drain(recipe.getFluidIngredient().get().amount(), recipe.getUnit(),false);
            keg.inventory.insertItem(OUTPUT_SLOT, recipe.getResult().right().get().copy(), false);
        }


        for (int i = 0; i < CONTAINER_SLOT; ++i) {
            ItemStack slotStack = inventory.getStackInSlot(i);
            boolean matchesIngredient = recipe.getIngredients().stream().anyMatch(ingredient -> ingredient.test(slotStack));
            if (matchesIngredient) {
                ItemStack remainder = BrewinAndChewin.getHelper().getCraftingRemainingItem(slotStack);
                if (!remainder.isEmpty()) ejectIngredientRemainder(remainder);
                inventory.extractItem(i, 1, false);
            }
        }
        return true;
    }

    public List<ItemStack> extractInGui(ItemStack slotIn, int maxTakeAmount) {
        return fluidExtract(slotIn, maxTakeAmount, true, false);
    }

    public List<ItemStack> extractInWorld(ItemStack slotIn, int maxTakeAmount,boolean isCreative) {
        return fluidExtract(slotIn, maxTakeAmount, false, isCreative);
    }

    private List<ItemStack> fluidExtract(ItemStack slotIn, int maxTakeAmount, boolean inGui, boolean isCreative) {
        if (slotIn.isEmpty() || deferFluidExtraction ||
                inventory.getStackInSlot(OUTPUT_SLOT).getCount() >= Math.min(inventory.getSlotLimit(OUTPUT_SLOT), inventory.getStackInSlot(OUTPUT_SLOT).getMaxStackSize()))
            return List.of();

        Optional<KegPouringRecipe> recipe = getPouringRecipe(slotIn);
        boolean changed = false;

        List<ItemStack> outputs = new ArrayList<>();
        currentlyOperating = true;

        if (recipe.isPresent() && (fluidTank.isEmpty() || fluidTank.getAbstractedFluid().fluid() == recipe.get().getRawFluid().fluid())) { // if the recipe is present and the fluid is empty or the same
            ItemStack resultItem = recipe.get().assemble(recipeWrapper, level.registryAccess());
            if (ItemStack.isSameItem(slotIn, recipe.get().getContainer(resultItem)) && // if container is same
                    recipe.get().getRawFluid().amount() <= fluidTank.getAbstractedFluid().amount() && // the amount is LTE the fluid amount
                    (!inGui || inventory.getStackInSlot(OUTPUT_SLOT).isEmpty() || ItemStack.isSameItemSameComponents(resultItem, inventory.getStackInSlot(OUTPUT_SLOT)))) { // the output slot can accept this itemaccept this item
                int containerAmount = (int) Mth.clamp(Math.min(Math.min(slotIn.getCount(), resultItem.getMaxStackSize()), maxTakeAmount), 1, fluidTank.getAbstractedFluid().amount() / recipe.get().getLoaderAmount());
                fluidTank.drain(recipe.get().getRawFluid().amount() * containerAmount, recipe.get().getUnit(),false);

                if (!isCreative) {
                    long overflow = containerAmount;
                    while (overflow > 0 && (!inGui || outputs.isEmpty()) && !slotIn.isEmpty()) {
                        ItemStack newResult = resultItem.copyWithCount((int) Math.min(Math.min(slotIn.getCount(), maxTakeAmount), overflow));
                        outputs.add(newResult);
                        overflow -= newResult.getCount();
                        slotIn.shrink(newResult.getCount());
                    }
                    if (!slotIn.isEmpty())
                        outputs.add(slotIn);
                } else {
                    outputs.add(slotIn);
                }
                changed = true;
            } else if (recipe.filter(KegPouringRecipe::canFill).isPresent() && // if the recipe can fill
                    (recipe.get().isStrict() && ItemStack.isSameItemSameComponents(resultItem, slotIn) || !recipe.get().isStrict() && ItemStack.isSameItem(slotIn, resultItem)) && // if result is same
                    (fluidTank.isEmpty() || fluidTank.getAbstractedFluid().matches(recipe.get().getFluid(slotIn)) && fluidTank.getAbstractedFluid().amount() < fluidTank.getFluidCapacity()) && // if the result can fit in the container
                    (!inGui || inventory.getStackInSlot(OUTPUT_SLOT).isEmpty() || ItemStack.isSameItemSameComponents(recipe.get().getContainer(slotIn), inventory.getStackInSlot(OUTPUT_SLOT)))) { // the output slot can accept this item
                int containerAmount = (int) Mth.clamp(Math.min(Math.min(slotIn.getCount(), recipe.get().getContainer(slotIn).getMaxStackSize()), fluidTank.getFluidCapacity() / recipe.get().getLoaderAmount()), 1, maxTakeAmount);
                fluidTank.fill(new AbstractedFluidStack(recipe.get().getFluid(slotIn).fluid(), recipe.get().getRawFluid().amount() * containerAmount, recipe.get().getFluid(slotIn).components(), recipe.get().getUnit(), null), false);

                if (!isCreative) {
                    ItemStack recipeItem = recipe.get().getContainer(slotIn);
                    int overflow = containerAmount;
                    while (overflow > 0 && !slotIn.isEmpty()) {
                        ItemStack newResult = recipeItem.copyWithCount(Math.min(Math.min(slotIn.getCount(), maxTakeAmount), overflow));
                        outputs.add(newResult);
                        overflow -= newResult.getCount();
                        slotIn.shrink(newResult.getCount());
                    }
                    if (!slotIn.isEmpty())
                        outputs.add(slotIn);
                } else {
                    outputs.add(slotIn);
                }
                changed = true;
            }

            if (changed) {
                inventoryChanged();
            }
        }

        if (!outputs.isEmpty() || recipe.isPresent()) {
            currentlyOperating = false;
            return outputs;
        }

        // TODO: Account for stacks with multiple tanks.
        AbstractedFluidTank itemFluidContainer = BrewinAndChewin.getHelper().getFluidContainerFromItem(slotIn);

        if (itemFluidContainer != null && !slotIn.isEmpty()) {
            if ((fluidTank.getAbstractedFluid().matches(itemFluidContainer.getAbstractedFluid()) || fluidTank.getAbstractedFluid().isEmpty()) &&
                    (!inGui || inventory.getStackInSlot(OUTPUT_SLOT).isEmpty() || inventory.getStackInSlot(OUTPUT_SLOT).is(itemFluidContainer.getContainer().getItem())) &&
                    level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING).stream().anyMatch(pouringRecipe -> pouringRecipe.value().getFluid(slotIn).matches(fluidTank.getAbstractedFluid()))) {
                long amountToDrain = fluidTank.getFluidCapacity() - fluidTank.getAbstractedFluid().amount();
                long amount = fluidTank.fill(itemFluidContainer.drain(amountToDrain, FluidUnit.getLoaderUnit(), true), true).amount();
                if (amount <= amountToDrain && amount > 0) {
                    fluidTank.fill(itemFluidContainer.drain(amountToDrain, FluidUnit.getLoaderUnit(), true), true);
                    if (!isCreative) {
                        ItemStack recipeItem = BrewinAndChewin.getHelper().getCraftingRemainingItem(slotIn).isEmpty() ? itemFluidContainer.getContainer() : BrewinAndChewin.getHelper().getCraftingRemainingItem(slotIn);
                        int overflow = (int) (amount / fluidTank.getFluidCapacity());
                        while (overflow > 0 && !slotIn.isEmpty()) {
                            ItemStack newResult = recipeItem.copyWithCount(Math.min(Math.min(slotIn.getCount(), maxTakeAmount), overflow));
                            outputs.add(newResult);
                            overflow -= newResult.getCount();
                            slotIn.shrink(newResult.getCount());
                        }
                    } else {
                        outputs.add(slotIn);
                    }
                    setChanged();
                    inventoryChanged();
                }
            } else if (!fluidTank.getAbstractedFluid().isEmpty() && itemFluidContainer.isFluidValid(fluidTank.getAbstractedFluid())
            && (!inGui || inventory.getStackInSlot(OUTPUT_SLOT).isEmpty() || inventory.getStackInSlot(OUTPUT_SLOT).is(itemFluidContainer.getContainer().getItem()))) {
                long amountToDrain = itemFluidContainer.getFluidCapacity();
                itemFluidContainer = BrewinAndChewin.getHelper().getFluidContainerFromItem(slotIn.copyWithCount((int) (amountToDrain / itemFluidContainer.getFluidCapacity())));
                long amount = itemFluidContainer.fill(fluidTank.drain(amountToDrain, FluidUnit.getLoaderUnit(), true), true).amount();
                if (amount > 0) {
                    itemFluidContainer.fill(fluidTank.drain(amountToDrain, FluidUnit.getLoaderUnit(), false), false);
                    if (amount <= amountToDrain) {
                        outputs.add(slotIn);
                        setChanged();
                        inventoryChanged();
                    }
                }
            }

        }

        currentlyOperating = false;
        return outputs;
    }



    public Optional<KegPouringRecipe> getPouringRecipe(ItemStack slot) {
        if (level == null) return Optional.empty();
        return level.getRecipeManager().getAllRecipesFor(BnCRecipeTypes.KEG_POURING)
                .stream()
                .map(RecipeHolder::value)
                .sorted(Comparator.comparingInt(value -> value.isStrict() ? 0 : 1))
                .filter(r -> {
                    boolean containerCheck = false;
                    boolean resultCheck = false;
                    boolean fluidCheck = false;
                    if (r.isStrict() && ItemStack.isSameItemSameComponents(r.getContainer(), slot) || !r.isStrict() && (r.getContainer().getItem() == slot.getItem()))
                        containerCheck = true;
                    if (!containerCheck && r.canFill() && (r.isStrict() && ItemStack.isSameItemSameComponents(r.assemble(recipeWrapper, level.registryAccess()), slot) || !r.isStrict() && r.assemble(recipeWrapper, level.registryAccess()).getItem() == slot.getItem()))
                        resultCheck = true;
                    if (recipeWrapper.getFluid().isEmpty() || (containerCheck && r.getRawFluid().fluid() == recipeWrapper.getFluid().fluid() || r.getFluid(slot).matches(recipeWrapper.getFluid())))
                        fluidCheck = true;
                    return (containerCheck || resultCheck) && fluidCheck;
                })
                .findFirst();
    }

    public void updateTemperature() {
        ArrayList<BlockState> states = new ArrayList<>();
        for (int x = -RANGE; x <= RANGE; x++) {
            for (int y = -RANGE; y <= RANGE; y++) {
                for (int z = -RANGE; z <= RANGE; z++) {
                    states.add(level.getBlockState(worldPosition.offset(x, y, z)));
                }
            }
        }

        int heat = states.stream().filter(s -> s.is(ModTags.Blocks.HEAT_SOURCES) && s.hasProperty(BlockStateProperties.LIT)).filter(s -> s.getValue(BlockStateProperties.LIT)).mapToInt(s -> 1).sum();
        heat += states.stream().filter(s -> s.is(ModTags.Blocks.HEAT_SOURCES) && !s.hasProperty(BlockStateProperties.LIT)).mapToInt(s -> 1).sum();

        // Compat with mods that have lit states, such as a future Pug FD addon.
        int cold = states.stream().filter(s -> s.is(BnCTags.Blocks.FREEZE_SOURCES) && s.hasProperty(BlockStateProperties.LIT)).filter(s -> s.hasProperty(BlockStateProperties.LIT)).filter(s -> s.getValue(BlockStateProperties.LIT)).mapToInt(s -> 1).sum();
        cold += states.stream().filter(s -> s.is(BnCTags.Blocks.FREEZE_SOURCES) && !s.hasProperty(BlockStateProperties.LIT)).mapToInt(s -> 1).sum();

        if (BnCConfiguration.COMMON_CONFIG.get().keg().biomeTemp()) {
            Holder<Biome> biome = level.getBiome(worldPosition);
            if (biome.isBound()) {
                float biomeTemperature = biome.value().getBaseTemperature();
                if (biomeTemperature <= 0) {
                    cold += 1;
                } else if (biomeTemperature == 2) {
                    heat += 1;
                }
            }
        }

        int temp = heat - cold;

        if (BnCConfiguration.COMMON_CONFIG.get().keg().dimTemp() && level.dimensionType().ultraWarm())
            temp += 2;

        if (!initialisedTemperature || temp != kegTemperature) {
            kegTemperature = temp;
            initialisedTemperature = true;
            inventoryChanged();
        }
    }

    public int getTemperature() {
        if (kegTemperature <= -BnCConfiguration.COMMON_CONFIG.get().keg().cold()) {
            return 1;
        } else if (kegTemperature <= -BnCConfiguration.COMMON_CONFIG.get().keg().chilly()) {
            return 2;
        } else if (kegTemperature < BnCConfiguration.COMMON_CONFIG.get().keg().warm()) {
            return 3;
        } else if (kegTemperature < BnCConfiguration.COMMON_CONFIG.get().keg().hot()) {
            return 4;
        } else {
            return 5;
        }
    }

    protected void ejectIngredientRemainder(ItemStack remainderStack) {
        Direction direction = getBlockState().getValue(KegBlock.FACING).getCounterClockWise();
        double x = worldPosition.getX() + 0.5 + (direction.getStepX() * 0.25);
        double y = worldPosition.getY() + 0.7;
        double z = worldPosition.getZ() + 0.5 + (direction.getStepZ() * 0.25);
        ItemUtils.spawnItemEntity(level, remainderStack, x, y, z,
                direction.getStepX() * 0.08F, 0.25F, direction.getStepZ() * 0.08F);
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipe) {
        if (recipe != null) {
            ResourceLocation recipeID = recipe.id();
            usedRecipeTracker.addTo(recipeID, 1);
        }
    }

    @Nullable
    @Override
    public RecipeHolder<?> getRecipeUsed() {
        return null;
    }

    @Override
    public void awardUsedRecipes(Player player, List<ItemStack> items) {
        List<RecipeHolder<?>> usedRecipes = getUsedRecipesAndPopExperience(player.level(), player.position());
        player.awardRecipes(usedRecipes);
        usedRecipeTracker.clear();
    }

    public List<RecipeHolder<?>> getUsedRecipesAndPopExperience(Level level, Vec3 pos) {
        List<RecipeHolder<?>> list = Lists.newArrayList();

        for (Object2IntMap.Entry<ResourceLocation> entry : usedRecipeTracker.object2IntEntrySet()) {
            level.getRecipeManager().byKey(entry.getKey()).ifPresent((recipe) -> {
                list.add(recipe);
                splitAndSpawnExperience((ServerLevel) level, pos, entry.getIntValue(), ((KegFermentingRecipe)recipe.value()).getExperience());
            });
        }

        return list;
    }

    private static void splitAndSpawnExperience(ServerLevel level, Vec3 pos, int craftedAmount, float experience) {
        int expTotal = Mth.floor((float) craftedAmount * experience);
        float expFraction = Mth.frac((float) craftedAmount * experience);
        if (expFraction > 0.0F && Math.random() < (double) expFraction) {
            ++expTotal;
        }

        ExperienceOrb.award(level, pos, expTotal);
    }

    public AbstractedItemHandler getInventory() {
        return inventory;
    }

    public AbstractedFluidTank getFluidTank() {
        return fluidTank;
    }

    public NonNullList<ItemStack> getDroppableInventory() {
        NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < INVENTORY_SIZE; ++i) {
            drops.add(inventory.getStackInSlot(i));
        }
        return drops;
    }

    @Override
    public Component getName() {
        return customName != null ? customName : BnCTextUtils.getTranslation("container.keg");
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    @Nullable
    public Component getCustomName() {
        return customName;
    }

    public void setCustomName(Component name) {
        customName = name;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player, Player entity) {
        return new KegMenu(id, player, this, kegData);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return writeUpdateTag(new CompoundTag(), provider);
    }

    private AbstractedItemHandler createHandler() {
        return BrewinAndChewin.getHelper().createKegInventory(INVENTORY_SIZE, (handler, slot) -> {
            if (!getLevel().isClientSide() && (slot == CONTAINER_SLOT || slot == OUTPUT_SLOT) && !currentlyOperating) {
                deferFluidExtraction = true;
            }
            if (slot >= 0 && slot < OUTPUT_SLOT) {
                checkNewRecipe = true;
            }
            inventoryChanged();
        });
    }

    private AbstractedFluidTank createFluidTank() {
        return BrewinAndChewin.getHelper().createKegTank(BnCConfiguration.COMMON_CONFIG.get().keg().localizedCapacity(), () -> {
            AbstractedItemHandler handler = KegBlockEntity.this.inventory;
            if (!getLevel().isClientSide() && !currentlyOperating && !deferFluidExtraction) {
                List<ItemStack> out = KegBlockEntity.this.extractInGui(handler.getStackInSlot(CONTAINER_SLOT), handler.getSlotLimit(OUTPUT_SLOT));
                if (!out.isEmpty())
                    handler.insertItem(OUTPUT_SLOT, out.get(0), false);
            }
            inventoryChanged();
            checkNewRecipe = true;
        });
    }

    private ContainerData createIntArray() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> KegBlockEntity.this.fermentTime;
                    case 1 -> KegBlockEntity.this.fermentTimeTotal;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> KegBlockEntity.this.fermentTime = value;
                    case 1 -> KegBlockEntity.this.fermentTimeTotal = value;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }
    private int bubbleTickCounter = 0;

    public boolean isFermenting() {
        return fermentTime > 0 && fermentTime < fermentTimeTotal;
    }
}