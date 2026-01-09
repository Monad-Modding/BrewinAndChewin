package umpaz.brewinandchewin.fabric.container;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.fabric.utility.AmountedFluidVariant;

public class KegFluidItemStorageFabric implements AbstractedFluidTank {
    private final Storage<FluidVariant> storage;
    private final ContainerItemContext context;
    private long capacity = -1L;

    public KegFluidItemStorageFabric(ItemStack stack) {
        context = ContainerItemContext.withConstant(stack);
        storage = FluidStorage.ITEM.find(stack, context);
    }

    @Override
    public long getFluidCapacity(int slot) {
        if (capacity < 0) {
            if (storage instanceof SlottedStorage<FluidVariant> view) {
                capacity = view.getSlot(slot).getCapacity();
            } else if (storage instanceof StorageView<?> view) {
                capacity = view.getCapacity();
            } else {
                for (var view : storage) {
                    capacity = view.getCapacity();
                    break;
                }
            }
        }
        return capacity;
    }


    @Override
    public AbstractedFluidStack getAbstractedFluid() {
        if (storage instanceof StorageView<?> view) {
            FluidVariant variant = (FluidVariant)view.getResource();
            AmountedFluidVariant amounted = new AmountedFluidVariant(variant, view.getAmount(), FluidUnit.DROPLET);
            return new AbstractedFluidStack(variant.getFluid(), view.getAmount(), FluidUnit.DROPLET, amounted);
        }
        return AbstractedFluidStack.EMPTY;
    }

    @Override
    public void setAbstractedFluid(AbstractedFluidStack stack) {
        if (storage.supportsInsertion()) {
            try (Transaction t = Transaction.openOuter()) {
                FluidVariant variant = FluidVariant.of(stack.fluid());
                storage.insert(variant, capacity, t);
                t.commit();
            }
        }
    }

    @Override
    public AbstractedFluidStack fill(AbstractedFluidStack stack, boolean simulate) {
        if (storage.supportsInsertion()) {
            try (Transaction t = Transaction.openOuter()) {
                FluidVariant variant = FluidVariant.of(stack.fluid());
                long newFill = storage.insert(variant, capacity, t);
                if (!simulate)
                    t.commit();
                return new AbstractedFluidStack(variant.getFluid(), newFill, FluidUnit.DROPLET, new AmountedFluidVariant(variant, newFill, FluidUnit.DROPLET));
            }
        }
        return getAbstractedFluid();
    }

    @Override
    public AbstractedFluidStack drain(int slot, long maxDrain, FluidUnit unit, boolean simulate) {
        if (storage.supportsExtraction() && storage instanceof SlottedStorage<FluidVariant> slottedStorage) {
            try (Transaction t = Transaction.openOuter()) {
                SingleSlotStorage<FluidVariant> singleSlot = slottedStorage.getSlot(slot);
                long extractedAmount = storage.extract(singleSlot.getResource(), unit.convertToLoader(maxDrain), t);
                AbstractedFluidStack stack = new AbstractedFluidStack(singleSlot.getResource().getFluid(), extractedAmount, FluidUnit.DROPLET, new AmountedFluidVariant(singleSlot.getResource(), extractedAmount, FluidUnit.DROPLET));
                if (!simulate)
                    t.commit();
                return stack;
            }
        }
        return AbstractedFluidStack.EMPTY;
    }

    @Override
    public ItemStack getContainer() {
        return context.getItemVariant().toStack();
    }

    @Override
    public boolean isEmpty() {
        return storage.nonEmptyIterator().hasNext();
    }

    @Override
    public boolean isFluidValid(int slot, AbstractedFluidStack stack) {
        try (Transaction t = Transaction.openOuter()) {
            return storage.insert(((AmountedFluidVariant) stack.loaderSpecific()).variant(), getFluidCapacity(), t) > 0;
        }
    }
}
