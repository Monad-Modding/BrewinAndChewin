package umpaz.brewinandchewin.common.utility;

import net.minecraft.world.Container;

public interface KegContainer extends Container {
    AbstractedFluidStack getFluid();
    long getTankCapacity();

    @Override
    default boolean isEmpty() {
        if (getFluid().isEmpty())
            return false;
        return true;
    }
}
