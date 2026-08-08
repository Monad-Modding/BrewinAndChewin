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
import umpaz.brewinandchewin.common.block.entity.DistilleryBlockEntity;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCMenuTypes;

import java.util.Objects;

public class DistilleryMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData distilleryData;
    private final ContainerLevelAccess access;

    public DistilleryMenu(int windowId, Inventory playerInventory, BlockPos pos) {
        this(windowId, playerInventory, getBlockEntity(playerInventory, pos), new SimpleContainerData(DistilleryBlockEntity.DATA_COUNT));
    }

    public DistilleryMenu(int windowId, Inventory playerInventory, Container container, ContainerData distilleryData) {
        super(BnCMenuTypes.DISTILLERY, windowId);
        checkContainerSize(container, DistilleryBlockEntity.INVENTORY_SIZE);
        checkContainerDataCount(distilleryData, DistilleryBlockEntity.DATA_COUNT);
        this.container = container;
        this.distilleryData = distilleryData;
        this.access = container instanceof BlockEntity blockEntity && blockEntity.getLevel() != null
                ? ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos())
                : ContainerLevelAccess.NULL;

        this.addSlot(new Slot(container, DistilleryBlockEntity.INPUT_SLOT, 44, 17));
        this.addSlot(new Slot(container, DistilleryBlockEntity.FUEL_SLOT, 44, 53));
        this.addSlot(new Slot(container, DistilleryBlockEntity.WATER_SLOT, 80, 17));
        this.addSlot(new Slot(container, DistilleryBlockEntity.OUTPUT_SLOT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, 9 + (row * 9) + column, 8 + (column * 18), 84 + (row * 18)));
            }
        }
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(playerInventory, column, 8 + (column * 18), 142));
        }

        this.addDataSlots(distilleryData);
    }

    private static Container getBlockEntity(Inventory playerInventory, BlockPos pos) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(pos, "pos cannot be null");
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof DistilleryBlockEntity distillery)
            return distillery;
        return new SimpleContainer(DistilleryBlockEntity.INVENTORY_SIZE);
    }

    public int getDistillingProgressScaled(int pixels) {
        int progress = this.distilleryData.get(2);
        int total = this.distilleryData.get(3);
        return total != 0 && progress != 0 ? progress * pixels / total : 0;
    }

    public int getLitProgressScaled(int pixels) {
        int duration = this.distilleryData.get(1);
        if (duration == 0)
            duration = 200;
        return this.distilleryData.get(0) * pixels / duration;
    }

    public boolean isLit() {
        return this.distilleryData.get(0) > 0;
    }

    public int getWater() {
        return this.distilleryData.get(4);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, BnCBlocks.DISTILLERY);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int startPlayerInv = DistilleryBlockEntity.INVENTORY_SIZE;
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
            } else if (!this.moveItemStackTo(slotStack, DistilleryBlockEntity.INPUT_SLOT, DistilleryBlockEntity.OUTPUT_SLOT, false)) {
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

    public Container getContainer() {
        return this.container;
    }
}
