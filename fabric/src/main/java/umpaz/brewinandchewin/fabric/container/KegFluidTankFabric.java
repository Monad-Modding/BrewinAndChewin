package umpaz.brewinandchewin.fabric.container;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.fabric.utility.AmountedFluidVariant;

public class KegFluidTankFabric extends SingleFluidStorage implements AbstractedFluidTank {
    private final long capacity;

    public KegFluidTankFabric(long capacity) {
        this.capacity = capacity;
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return getFluidCapacity();
    }

    @Override
    public long getFluidCapacity(int slot) {
        return capacity;
    }

    @Override
    public AbstractedFluidStack getAbstractedFluid() {
        return new AbstractedFluidStack(variant.getFluid(), getAmount(), variant.getComponents(), FluidUnit.DROPLET, new AmountedFluidVariant(variant, getAmount(), FluidUnit.DROPLET));
    }

    @Override
    public void setAbstractedFluid(AbstractedFluidStack stack) {
        try (Transaction t = Transaction.openOuter()) {
            if (!variant.isBlank() && getAmount() > 0) {
                extract(variant, getAmount(), t);
            }
            if (!stack.isEmpty()) {
                AmountedFluidVariant wrapped = unwrapFluid(stack);
                if (!wrapped.variant().isBlank()) {
                    insert(wrapped.variant(), FluidUnit.convertToLoader(stack.amount(), stack.unit()), t);
                }
            }
            t.commit();
        }
    }

    @Override
    public AbstractedFluidStack fill(AbstractedFluidStack fluidStack, boolean simulate) {
        long newAmount = fluidStack.unit().convertToLoader(fluidStack.amount());
        try {
            Transaction t = Transaction.openOuter();
            FluidVariant variant = FluidVariant.of(fluidStack.fluid(), fluidStack.components() instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY);
            long newFill = insert(variant, newAmount, t);
            if (!simulate)
                t.commit();
            t.close();
            return new AbstractedFluidStack(variant.getFluid(), newFill, variant.getComponents(), FluidUnit.DROPLET, new AmountedFluidVariant(variant, newFill, FluidUnit.DROPLET));
        } catch (Exception e) {
            BrewinAndChewin.LOG.error("Failed to fill keg with {} of fluid {}.", fluidStack.fluid(), fluidStack.unit().shortFormat(String.valueOf(fluidStack.unit().convertToLoader(fluidStack.amount()))));
        }
        return AbstractedFluidStack.EMPTY;
    }

    @Override
    public AbstractedFluidStack drain(int slot, long maxDrain, FluidUnit unit, boolean simulate) {
        long newMax = unit.convertToLoader(maxDrain);
        try {
            Transaction t = Transaction.openOuter();
            long extractedAmount = extract(variant, newMax, t);
            AbstractedFluidStack stack = new AbstractedFluidStack(variant.getFluid(), extractedAmount, variant.getComponents(), FluidUnit.DROPLET, new AmountedFluidVariant(variant, extractedAmount, FluidUnit.DROPLET));
            if (!simulate)
                t.commit();
            t.close();
            return stack;
        } catch (Exception e) {
            BrewinAndChewin.LOG.error("Failed to extract {} from keg.", unit.shortFormat(String.valueOf(unit.convertToLoader(maxDrain))));
        }
        return AbstractedFluidStack.EMPTY;
    }

    @Override
    public void readFromValue(ValueInput input) {
        readValue(input);
    }

    @Override
    public void writeToValue(ValueOutput output) {
        writeValue(output);
    }

    @Override
    public boolean isEmpty() {
        return isResourceBlank();
    }

    private AmountedFluidVariant unwrapFluid(AbstractedFluidStack stack)  {
        if (stack.loaderSpecific() instanceof AmountedFluidVariant fluidVariant)
            return fluidVariant;

        if (stack.isEmpty())
            return AmountedFluidVariant.EMPTY;

        return new AmountedFluidVariant(FluidVariant.of(stack.fluid(), stack.components() instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY), stack.unit().convertToLoader(stack.amount()), FluidUnit.DROPLET);
    }
}
