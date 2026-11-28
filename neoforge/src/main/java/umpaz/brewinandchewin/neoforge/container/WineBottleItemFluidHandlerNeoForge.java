package umpaz.brewinandchewin.neoforge.container;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.registry.BnCFluids;
import umpaz.brewinandchewin.common.registry.BnCItems;

public class WineBottleItemFluidHandlerNeoForge implements IFluidHandlerItem {
    public static final int CAPACITY = 250;

    private ItemStack container;

    public WineBottleItemFluidHandlerNeoForge(ItemStack container) {
        this.container = container;
    }

    @Nullable
    private static Item getBottledWine(FluidStack stack) {
        if (stack.isEmpty())
            return null;
        Fluid fluid = stack.getFluid();
        if (fluid == BnCFluids.RED_WINE) return BnCItems.RED_WINE;
        if (fluid == BnCFluids.WHITE_WINE) return BnCItems.WHITE_WINE;
        if (fluid == BnCFluids.CURRANT_WINE) return BnCItems.CURRANT_WINE;
        if (fluid == BnCFluids.VERRUCA_WINE) return BnCItems.VERRUCA_WINE;
        if (fluid == BnCFluids.TWISTED_WINE) return BnCItems.TWISTED_WINE;
        if (fluid == BnCFluids.RICE_WINE) return BnCItems.RICE_WINE;
        if (fluid == BnCFluids.VODKA) return BnCItems.VODKA;
        return null;
    }

    @Override
    public ItemStack getContainer() {
        return this.container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return CAPACITY;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return getBottledWine(stack) != null;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        Item wine = getBottledWine(resource);
        if (wine == null || resource.getAmount() < CAPACITY)
            return 0;
        if (action.execute())
            this.container = new ItemStack(wine);
        return CAPACITY;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
