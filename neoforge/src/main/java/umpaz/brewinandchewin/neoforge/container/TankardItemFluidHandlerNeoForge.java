package umpaz.brewinandchewin.neoforge.container;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import umpaz.brewinandchewin.common.registry.BnCFluids;
import umpaz.brewinandchewin.common.registry.BnCItems;


public class TankardItemFluidHandlerNeoForge implements IFluidHandlerItem {
//Warning may contain silly references :P
    private ItemStack container;
    private FluidStack fluid = FluidStack.EMPTY;
    private final int capacity;

    public TankardItemFluidHandlerNeoForge(ItemStack container, int capacity) {
        this.container = container;
        this.capacity = capacity;
    }

    private ItemStack convertToBrew(FluidStack fluid) {
        if (fluid.isEmpty()) return container;
        if (fluid.getFluid() == BnCFluids.BEER) {
            return new ItemStack(BnCItems.BEER);
        }
        if (fluid.getFluid() == BnCFluids.VODKA) {
            return new ItemStack(BnCItems.VODKA);
        }
        if (fluid.getFluid() == BnCFluids.MEAD) {
            return new ItemStack(BnCItems.MEAD);
        }
        if (fluid.getFluid() == BnCFluids.DREAD_NOG) {
            return new ItemStack(BnCItems.DREAD_NOG);
        }
        if (fluid.getFluid() == BnCFluids.BLOODY_MARY) {
            return new ItemStack(BnCItems.BLOODY_MARY);
        }
        if (fluid.getFluid() == BnCFluids.EGG_GROG) {
            return new ItemStack(BnCItems.EGG_GROG);
        }
        if (fluid.getFluid() == BnCFluids.GLITTERING_GRENADINE) {
            return new ItemStack(BnCItems.GLITTERING_GRENADINE);
        }
        if (fluid.getFluid() == BnCFluids.PALE_JANE) {
            return new ItemStack(BnCItems.PALE_JANE);
        }
        if (fluid.getFluid() == BnCFluids.RED_RUM) {
            return new ItemStack(BnCItems.RED_RUM);
        }
        if (fluid.getFluid() == BnCFluids.SACCHARINE_RUM) {
            return new ItemStack(BnCItems.SACCHARINE_RUM);
        }
        if (fluid.getFluid() == BnCFluids.SALTY_FOLLY) {
            return new ItemStack(BnCItems.SALTY_FOLLY);
        }
        if (fluid.getFluid() == BnCFluids.STEEL_TOE_STOUT) {
            return new ItemStack(BnCItems.STEEL_TOE_STOUT);
        }
        if (fluid.getFluid() == BnCFluids.STRONGROOT_ALE) {
            return new ItemStack(BnCItems.STRONGROOT_ALE);
        }
        if (fluid.getFluid() == BnCFluids.WITHERING_DROSS) {
            return new ItemStack(BnCItems.WITHERING_DROSS);
        }
        return container;
    }

    private FluidStack getFluidFromBrew(ItemStack stack){
        Item item = stack.getItem();
        if (item == BnCItems.BEER) return new FluidStack(BnCFluids.BEER, capacity);
        if (item == BnCItems.VODKA) return new FluidStack(BnCFluids.VODKA, capacity);
        if (item == BnCItems.MEAD) return new FluidStack(BnCFluids.MEAD, capacity);
        if (item == BnCItems.BLOODY_MARY) return new FluidStack(BnCFluids.BLOODY_MARY, capacity);
        if (item == BnCItems.DREAD_NOG) return new FluidStack(BnCFluids.DREAD_NOG, capacity);
        if (item == BnCItems.EGG_GROG) return new FluidStack(BnCFluids.EGG_GROG, capacity);
        if (item == BnCItems.GLITTERING_GRENADINE) return new FluidStack(BnCFluids.GLITTERING_GRENADINE, capacity);
        if (item == BnCItems.PALE_JANE) return new FluidStack(BnCFluids.PALE_JANE, capacity);
        if (item == BnCItems.RED_RUM) return new FluidStack(BnCFluids.RED_RUM, capacity);
        if (item == BnCItems.SACCHARINE_RUM) return new FluidStack(BnCFluids.SACCHARINE_RUM, capacity);
        if (item == BnCItems.SALTY_FOLLY) return new FluidStack(BnCFluids.SALTY_FOLLY, capacity);
        if (item == BnCItems.STEEL_TOE_STOUT) return new FluidStack(BnCFluids.STEEL_TOE_STOUT, capacity);
        if (item == BnCItems.STRONGROOT_ALE) return new FluidStack(BnCFluids.STRONGROOT_ALE, capacity);
        if (item == BnCItems.WITHERING_DROSS) return new FluidStack(BnCFluids.WITHERING_DROSS, capacity);

        return FluidStack.EMPTY;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        Item Merlinsbeard = container.getItem();
        if (Merlinsbeard == BnCItems.TANKARD){
            return fluid;
        }
        return new FluidStack(
                getFluidFromBrew(container).getFluid(),
                capacity
        );
    }

    @Override
    public int getTankCapacity(int tank) {
        return capacity;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return container.getItem() == BnCItems.TANKARD;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        int fillAmount = Math.min(capacity - fluid.getAmount(), resource.getAmount());
        if (action.execute()) {
            fluid = new FluidStack(resource.getFluidHolder(), fluid.getAmount() + fillAmount);
            if (fluid.getAmount() >= capacity) {
                ItemStack newItem = convertToBrew(fluid);

                newItem.applyComponents(fluid.getComponents());

                this.container = newItem;
            }
        }
        return fillAmount;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (!fluid.isFluidEqual(resource)) return FluidStack.EMPTY;
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack flowingriver = getFluidInTank(0);
        FluidStack current = getFluidInTank(0);

        if (current.isEmpty()) return FluidStack.EMPTY;

        int drained = Math.min(maxDrain, current.getAmount());
        FluidStack result = new FluidStack(flowingriver.getFluidHolder(), drained);

        if (action.execute()) {
            if (container.getItem() != BnCItems.TANKARD) {
                fluid = current.copy();
                container = new ItemStack(BnCItems.TANKARD);
                fluid = FluidStack.EMPTY;
            } else {
                fluid.shrink(drained);
                if (fluid.getAmount() <= 0) {
                    fluid = FluidStack.EMPTY;
                }
            }
        }
        return result;
    }
}


