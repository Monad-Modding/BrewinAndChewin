package umpaz.brewinandchewin.common.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import umpaz.brewinandchewin.BrewinAndChewin;

import static umpaz.brewinandchewin.BrewinAndChewin.asResource;

public class AbstractedFluidStack {
    public static final AbstractedFluidStack EMPTY = new AbstractedFluidStack(Fluids.EMPTY, 0, FluidUnit.getLoaderUnit(), null);

    private final Fluid fluid;
    private final long amount;
    private final FluidUnit unit;
    private Object loaderSpecific;

    public AbstractedFluidStack(Fluid fluid, long amount, FluidUnit unit, Object loaderSpecific) {
        this.fluid = fluid;
        this.amount = amount;
        this.unit = unit;
        this.loaderSpecific = loaderSpecific;
    }

    public AbstractedFluidStack(Fluid fluid, long amount, FluidUnit unit) {
        this(fluid, amount, unit, null);
    }

    public AbstractedFluidStack(Fluid fluid, long amount) {
        this(fluid, amount, FluidUnit.getLoaderUnit(), null);
    }

    public boolean isEmpty() {
        return this == EMPTY || fluid == Fluids.EMPTY || amount <= 0;
    }

    public boolean matches(AbstractedFluidStack other) {
        return fluid == other.fluid;
    }

    public Fluid fluid() {
        return isEmpty() ? Fluids.EMPTY : fluid;
    }

    public long amount() {
        return isEmpty() ? 0 : amount;
    }

    public FluidUnit unit() {
        return unit;
    }

    public Object loaderSpecific() {
        if (loaderSpecific == null)
            loaderSpecific = BrewinAndChewin.getHelper().createLoaderFluidStack(this);

        return BrewinAndChewin.getHelper().copyLoaderFluidStack(loaderSpecific);
    }

    public static AbstractedFluidStack fromJson(JsonElement element) {
        JsonObject obj = element.getAsJsonObject();

        String fluidId = obj.get("fluid").getAsString();
        Fluid fluid = BuiltInRegistries.FLUID.get(asResource(fluidId));

        long amount = obj.has("amount") ? obj.get("amount").getAsLong() : 1;
        FluidUnit unit = obj.has("unit") ? FluidUnit.fromJson(obj.get("unit")) : FluidUnit.getLoaderUnit();
        return new AbstractedFluidStack(fluid, amount, unit);
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
        obj.addProperty("amount", amount);
        obj.add("unit", unit.toJson());
        return obj;
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(BuiltInRegistries.FLUID.getKey(fluid).toString());
        buf.writeLong(amount);
        unit.toNetwork(buf);
    }

    public static AbstractedFluidStack fromNetwork(FriendlyByteBuf buf) {
        Fluid fluid = BuiltInRegistries.FLUID.get(asResource(buf.readUtf()));

        long amount = buf.readLong();
        FluidUnit unit = FluidUnit.fromNetwork(buf);
        return new AbstractedFluidStack(fluid, amount, unit);
    }
}
