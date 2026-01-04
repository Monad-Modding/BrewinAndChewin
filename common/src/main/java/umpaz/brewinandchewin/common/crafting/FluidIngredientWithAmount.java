package umpaz.brewinandchewin.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import umpaz.brewinandchewin.common.utility.AbstractedFluidIngredient;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;

import java.util.Optional;

public record FluidIngredientWithAmount(AbstractedFluidIngredient ingredient, long amount, Optional<FluidUnit> unit) {
    public FluidIngredientWithAmount {
        if (ingredient.matches(AbstractedFluidStack.EMPTY))
            throw new IllegalArgumentException("Fluid Ingredient must not accept empty.");
        if (amount <= 0)
            throw new IllegalArgumentException("Fluid Ingredient amount must be higher than 0.");
    }

    public FluidUnit getUnit() {
        return unit().orElse(FluidUnit.getLoaderUnit());
    }

    public long loaderAmount() {
        FluidUnit unit = unit().orElse(FluidUnit.getLoaderUnit());
        return unit.convertToLoader(amount);
    }

    public static FluidIngredientWithAmount fromJson(JsonElement element) {
        JsonObject obj = element.getAsJsonObject();

        AbstractedFluidIngredient ingredient = AbstractedFluidIngredient.fromJson(obj.get("ingredient"));

        long amount = obj.has("amount")
                ? obj.get("amount").getAsLong()
                : ingredient.displayStacks().get(0).amount();

        Optional<FluidUnit> unit = obj.has("unit")
                ? Optional.of(FluidUnit.fromJson(obj.get("unit")))
                : Optional.empty();

        return new FluidIngredientWithAmount(ingredient, amount, unit);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        ingredient.toNetwork(buf);
        buf.writeLong(amount);
        buf.writeBoolean(unit.isPresent());
        unit.ifPresent(u -> u.toNetwork(buf));
    }

    public static FluidIngredientWithAmount fromNetwork(FriendlyByteBuf buf) {
        AbstractedFluidIngredient ingredient = AbstractedFluidIngredient.fromNetwork(buf);
        long amount = buf.readLong();
        Optional<FluidUnit> unit = buf.readBoolean() ? Optional.of(FluidUnit.fromNetwork(buf)) : Optional.empty();
        return new FluidIngredientWithAmount(ingredient, amount, unit);
    }
}
