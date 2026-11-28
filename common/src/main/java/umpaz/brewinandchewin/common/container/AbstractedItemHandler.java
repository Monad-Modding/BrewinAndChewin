package umpaz.brewinandchewin.common.container;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface AbstractedItemHandler {
    int getSlotCount();

    ItemStack getStackInSlot(int slot);
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);
    ItemStack extractItem(int slot, int amount, boolean simulate);
    void setStackInSlot(int slot, ItemStack stack);

    boolean isItemValid(int slot, ItemStack stack);
    int getSlotLimit(int slot);

    default void readFromValue(ValueInput input) {}
    default void writeToValue(ValueOutput output) {}
}
