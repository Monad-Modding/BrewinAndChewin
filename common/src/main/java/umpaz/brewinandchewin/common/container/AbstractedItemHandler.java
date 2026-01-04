package umpaz.brewinandchewin.common.container;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public interface AbstractedItemHandler {
    int getSlotCount();

    ItemStack getStackInSlot(int slot);
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);
    ItemStack extractItem(int slot, int amount, boolean simulate);
    void setStackInSlot(int slot, ItemStack stack);

    boolean isItemValid(int slot, ItemStack stack);
    int getSlotLimit(int slot);

    default void readFromNbt(CompoundTag tag) {}
    default CompoundTag writeToNbt() {
        return new CompoundTag();
    }
}
