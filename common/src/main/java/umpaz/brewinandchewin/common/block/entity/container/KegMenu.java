package umpaz.brewinandchewin.common.block.entity.container;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.BnCRecipeBookTypes;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCMenuTypes;
import umpaz.brewinandchewin.common.utility.KegContainer;

import java.util.Objects;

public class KegMenu extends RecipeBookMenu<KegContainer>  {
    public static final ResourceLocation EMPTY_CONTAINER_SLOT_TANKARD = BrewinAndChewin.asResource("item/empty_container_slot_tankard");

    public final KegBlockEntity blockEntity;
    public final AbstractedItemHandler inventory;
    public final AbstractedFluidTank kegTank;
    private final ContainerData kegData;
    private final ContainerLevelAccess canInteractWithCallable;
    protected final Level level;
    private final KegContainer recipeWrapper;

    public KegMenu(final int windowId, final Inventory playerInventory, final BlockPos data) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data), new SimpleContainerData(4));
    }

    public KegMenu(final int windowId, final Inventory playerInventory, final KegBlockEntity blockEntity, ContainerData kegDataIn) {
        super(BnCMenuTypes.KEG, windowId);
        this.blockEntity = blockEntity;
        this.inventory = blockEntity.getInventory();
        this.kegTank = blockEntity.getFluidTank();
        this.kegData = kegDataIn;
        this.level = playerInventory.player.level();
        this.canInteractWithCallable = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.recipeWrapper = BrewinAndChewin.getHelper().createRecipeWrapper(inventory, kegTank);

        // Ingredient Slots - 2 Rows x 2 Columns
        int startX = 8;
        int startY = 18;
        int inputStartX = 39;
        int inputStartY = 17;
        int borderSlotSize = 18;
        for (int row = 0; row < 2; ++row) {
            for (int column = 0; column < 2; ++column) {
                this.addSlot(BrewinAndChewin.getHelper().createKegSlot(inventory, (row * 2) + column,
                        inputStartX + (column * borderSlotSize),
                        inputStartY + (row * borderSlotSize)));
            }
        }


        // Tankard Input
        this.addSlot(BrewinAndChewin.getHelper().createKegContainerSlot(inventory, 4, 91, 55));

        // Tankard Output
        this.addSlot(BrewinAndChewin.getHelper().createKegResultSlot(inventory, 5, 124, 55));


        // Main Player Inventory
        int startPlayerInvY = startY * 4 + 12;
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, 9 + (row * 9) + column, startX + (column * borderSlotSize),
                        startPlayerInvY + (row * borderSlotSize)));
            }
        }

        // Hotbar
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(playerInventory, column, startX + (column * borderSlotSize), 142));
        }

        this.addDataSlots(kegDataIn);
    }

    private static KegBlockEntity getTileEntity(final Inventory playerInventory, final BlockPos data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        final BlockEntity tileAtPos = playerInventory.player.level().getBlockEntity(data);
        if (tileAtPos instanceof KegBlockEntity) {
            return (KegBlockEntity) tileAtPos;
        }
        throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(canInteractWithCallable, playerIn, BnCBlocks.KEG);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        int indexContainerInput = 4;
        int indexOutput = 5;
        int startPlayerInv = indexOutput + 1;
        int endPlayerInv = startPlayerInv + 36;

        Slot slot = this.slots.get(index);
        ItemStack slotStackCopy = ItemStack.EMPTY;
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            slotStackCopy = slotStack.copy();
            if (index == indexOutput) {
                if (!this.moveItemStackTo(slotStack, startPlayerInv, endPlayerInv, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index > indexOutput) {
                boolean isValidContainer = slotStack.is(blockEntity.getInventory().getStackInSlot(4).getItem()) || blockEntity.getPouringRecipe(slotStack).isPresent();
                if (isValidContainer && !this.moveItemStackTo(slotStack, indexContainerInput, indexContainerInput + 1, false)) {
                    return ItemStack.EMPTY;
                }
                else if ( !this.moveItemStackTo(slotStack, 0, indexContainerInput, false) ) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, startPlayerInv, endPlayerInv, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == slotStackCopy.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, slotStack);
        }


        return slotStackCopy;

    }

    public int getFermentProgressionScaled() {
        int i = this.kegData.get(0);
        int j = this.kegData.get(1);
        return j != 0 && i != 0 ? i * 22 / j : 0;
    }

    public float getProgression() {
        return this.kegData.get(0);
    }

    public int getKegTemperature() {
        return this.blockEntity.getTemperature();
    }

    public boolean isFermenting() {
        var recipe = blockEntity.getRecipeWithoutTemperature();
        return recipe.isPresent() && KegBlockEntity.isValidTemp(getKegTemperature(), recipe.get().getTemperature());
    }

    /*
    @Override
    public void handlePlacement(boolean placeAll, Recipe<?> recipe, ServerPlayer player) {
        Recipe<KegContainer> recipeHolder = (Recipe<KegContainer>) recipe;
        new KegPlaceRecipe(this, level.getRecipeManager()).recipeClicked(player, recipeHolder, placeAll);
    } */

    @Override
    public void fillCraftSlotsStackedContents(StackedContents helper) {
        for (int i = 0; i < inventory.getSlotCount(); i++) {
            helper.accountSimpleStack(inventory.getStackInSlot(i));
        }
    }

    @Override
    public void clearCraftingContent() {
        for (int i = 0; i < 4; i++) {
            this.inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean recipeMatches(Recipe<? super KegContainer> recipe) {
        return recipe.matches(recipeWrapper, level);
    }

    @Override
    public int getResultSlotIndex() {
        return 5;
    }

    @Override
    public int getGridWidth() {
        return 2;
    }

    @Override
    public int getGridHeight() {
        return 2;
    }

    @Override
    public int getSize() {
        return 6;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return BnCRecipeBookTypes.FERMENTING;
    }

    @Override
    public boolean shouldMoveToInventory(int slot) {
        return slot < (getGridWidth() * getGridHeight());
    }
}