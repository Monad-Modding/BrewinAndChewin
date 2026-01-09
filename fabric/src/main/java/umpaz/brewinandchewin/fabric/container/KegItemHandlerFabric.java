package umpaz.brewinandchewin.fabric.container;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;

import static io.github.fabricators_of_create.porting_lib.util.NBTSerializer.deserializeNBT;
import static io.github.fabricators_of_create.porting_lib.util.NBTSerializer.serializeNBT;

public class KegItemHandlerFabric implements AbstractedItemHandler, Container {

    private final NonNullList<ItemStack> stacks;

    public KegItemHandlerFabric(int size) {
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public void readFromNbt(CompoundTag tag, HolderLookup.Provider provider) {
        deserializeNBT(provider, tag);
    }

    public CompoundTag writeToNbt(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, stacks);
        return tag;
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int i) {
        return null;
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        return null;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return null;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {

    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {

    }

    @Override
    public int getSlotCount() {
        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return null;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return null;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return null;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {

    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 0;
    }
}
