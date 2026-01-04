package umpaz.brewinandchewin.common.utility;

import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public interface AbstractedFluidIngredient {
    List<AbstractedFluidStack> displayStacks();
    boolean matches(AbstractedFluidStack wrapper);

    static AbstractedFluidIngredient fromJson(JsonElement element) {
        throw new UnsupportedOperationException("fromJson must be implemented by the concrete ingredient class");
    }

    void toNetwork(FriendlyByteBuf buf);

    static AbstractedFluidIngredient fromNetwork(FriendlyByteBuf buf) {
        throw new UnsupportedOperationException("fromNetwork must be implemented by the concrete ingredient class");
    }
}
