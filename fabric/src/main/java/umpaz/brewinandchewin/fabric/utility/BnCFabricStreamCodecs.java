
package umpaz.brewinandchewin.fabric.utility;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;

public class BnCFabricStreamCodecs {
    public static final StreamCodec<RegistryFriendlyByteBuf, AbstractedFluidStack> FLUID_STACK_WRAPPER = AmountedFluidVariant.STREAM_CODEC.map(
            amountedFluidVariant -> new AbstractedFluidStack(amountedFluidVariant.variant().getFluid(), amountedFluidVariant.amount(), amountedFluidVariant.variant().getComponents(), amountedFluidVariant.fluidUnit(), amountedFluidVariant),
            wrapper -> {
                if (wrapper.loaderSpecific() instanceof AmountedFluidVariant fluidVariant)
                    return fluidVariant;
                return wrapper.isEmpty() ? AmountedFluidVariant.EMPTY : new AmountedFluidVariant(FluidVariant.of(wrapper.fluid(), wrapper.components() instanceof PatchedDataComponentMap patch ? patch.asPatch() : DataComponentPatch.EMPTY), wrapper.amount(), wrapper.unit());
            });
}
