package umpaz.brewinandchewin.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;

public class BottleRackBlockEntity extends SyncedBlockEntity implements Clearable {
    public static final int SLOT_COUNT = 9;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public BottleRackBlockEntity(BlockPos pos, BlockState state) {
        super(BnCBlockEntityTypes.BOTTLE_RACK, pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? this.inventory.get(slot) : ItemStack.EMPTY;
    }

    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT)
            return;
        this.inventory.set(slot, stack);
        this.inventoryChanged();
    }

    @Override
    public void clearContent() {
        this.inventory.clear();
        this.inventoryChanged();
    }

    public int countFilledSlots() {
        return (int) this.inventory.stream().filter(stack -> !stack.isEmpty()).count();
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        this.inventory.clear();
        ContainerHelper.loadAllItems(nbt, this.inventory, provider);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
        super.saveAdditional(nbt, provider);
        ContainerHelper.saveAllItems(nbt, this.inventory, provider);
    }
}
