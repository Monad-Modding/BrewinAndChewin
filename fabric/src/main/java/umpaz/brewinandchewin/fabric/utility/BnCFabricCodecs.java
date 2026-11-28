package umpaz.brewinandchewin.fabric.utility;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;

public class BnCFabricCodecs {
    public static final Codec<AbstractedFluidStack> FLUID_VARIANT_WRAPPER = AmountedFluidVariant.CODEC.xmap(
            fluidVariant -> new AbstractedFluidStack(fluidVariant.variant().getFluid(), fluidVariant.amount(), fluidVariant.variant().getComponents(), fluidVariant.fluidUnit(), fluidVariant),
            wrapper -> {
                if (wrapper.loaderSpecific() instanceof AmountedFluidVariant fluidVariant)
                    return fluidVariant;
                return wrapper.isEmpty() ? AmountedFluidVariant.EMPTY : new AmountedFluidVariant(FluidVariant.of(wrapper.fluid(), wrapper.components() instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY), wrapper.amount(), wrapper.unit());
            });
}
