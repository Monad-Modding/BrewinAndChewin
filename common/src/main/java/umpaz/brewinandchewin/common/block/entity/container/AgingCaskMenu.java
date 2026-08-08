package umpaz.brewinandchewin.common.block.entity.container;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import umpaz.brewinandchewin.common.block.entity.AgingCaskBlockEntity;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCMenuTypes;

import java.util.Objects;

public class AgingCaskMenu extends AbstractContainerMenu {
    public static final int GRID_X = 35;
    public static final int GRID_Y = 16;
    public static final int SLOT_SIZE = 18;
    public static final int OUTPUT_X = 137;
    public static final int OUTPUT_Y = 34;

    private static final int[] DISTILLATE_COLUMNS = {1, 2, 2, 2, 1, 0, 0, 0};
    private static final int[] DISTILLATE_ROWS = {0, 0, 1, 2, 2, 2, 1, 0};

    private final Container container;
    private final ContainerData caskData;
    private final ContainerLevelAccess access;

    public AgingCaskMenu(int windowId, Inventory playerInventory, BlockPos pos) {
        this(windowId, playerInventory, getBlockEntity(playerInventory, pos), new SimpleContainerData(AgingCaskBlockEntity.DATA_COUNT));
    }

    public AgingCaskMenu(int windowId, Inventory playerInventory, Container container, ContainerData caskData) {
        super(BnCMenuTypes.AGING_CASK, windowId);
        checkContainerSize(container, AgingCaskBlockEntity.INVENTORY_SIZE);
        checkContainerDataCount(caskData, AgingCaskBlockEntity.DATA_COUNT);
        this.container = container;
        this.caskData = caskData;
        this.access = container instanceof BlockEntity blockEntity && blockEntity.getLevel() != null
                ? ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos())
                : ContainerLevelAccess.NULL;

        this.addSlot(new Slot(container, AgingCaskBlockEntity.WINE_SLOT, GRID_X + SLOT_SIZE, GRID_Y + SLOT_SIZE) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(AgingCaskBlockEntity.WINE_SLOT, stack);
            }
        });

        for (int i = 0; i < AgingCaskBlockEntity.DISTILLATE_SLOTS; ++i) {
            int slot = AgingCaskBlockEntity.FIRST_DISTILLATE_SLOT + i;
            this.addSlot(new Slot(container,
                    slot,
                    GRID_X + DISTILLATE_COLUMNS[i] * SLOT_SIZE,
                    GRID_Y + DISTILLATE_ROWS[i] * SLOT_SIZE) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return container.canPlaceItem(slot, stack);
                }

                @Override
                public boolean isActive() {
                    return AgingCaskBlockEntity.isDistillateSlotUnlocked(container.getItem(AgingCaskBlockEntity.WINE_SLOT), slot);
                }
            });
        }

        this.addSlot(new Slot(container, AgingCaskBlockEntity.OUTPUT_SLOT, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, 9 + (row * 9) + column, 8 + (column * SLOT_SIZE), 84 + (row * SLOT_SIZE)));
            }
        }
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(playerInventory, column, 8 + (column * SLOT_SIZE), 142));
        }

        this.addDataSlots(caskData);
    }

    private static Container getBlockEntity(Inventory playerInventory, BlockPos pos) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(pos, "pos cannot be null");
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof AgingCaskBlockEntity cask)
            return cask;
        return new SimpleContainer(AgingCaskBlockEntity.INVENTORY_SIZE);
    }

    public static int distillateSlotColumn(int index) {
        return DISTILLATE_COLUMNS[index];
    }

    public static int distillateSlotRow(int index) {
        return DISTILLATE_ROWS[index];
    }

    public boolean isDistillateSlotOpen(int index) {
        return AgingCaskBlockEntity.isDistillateSlotUnlocked(
                this.container.getItem(AgingCaskBlockEntity.WINE_SLOT),
                AgingCaskBlockEntity.FIRST_DISTILLATE_SLOT + index);
    }

    public boolean hasWine() {
        return !this.container.getItem(AgingCaskBlockEntity.WINE_SLOT).isEmpty();
    }

    public int getAgingTime() {
        return this.caskData.get(0);
    }

    public int getAgingProgressScaled(int pixels) {
        int total = this.caskData.get(1);
        return total == 0 ? 0 : this.caskData.get(0) * pixels / total;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, BnCBlocks.AGING_CASK);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int startPlayerInv = AgingCaskBlockEntity.INVENTORY_SIZE;
        int endPlayerInv = startPlayerInv + 36;

        Slot slot = this.slots.get(index);
        ItemStack copy = ItemStack.EMPTY;
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            copy = slotStack.copy();
            if (index < startPlayerInv) {
                if (!this.moveItemStackTo(slotStack, startPlayerInv, endPlayerInv, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, copy);
            } else if (!this.moveItemStackTo(slotStack, AgingCaskBlockEntity.WINE_SLOT, AgingCaskBlockEntity.OUTPUT_SLOT, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == copy.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, slotStack);
        }
        return copy;
    }
}
