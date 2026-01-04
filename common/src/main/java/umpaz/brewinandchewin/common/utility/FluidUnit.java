package umpaz.brewinandchewin.common.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.StringRepresentable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.platform.BnCPlatform;

import java.util.Optional;
import java.util.function.Function;

public enum FluidUnit implements StringRepresentable {
    LITER("liters", l -> l + " L", l -> l + " liters",1),
    MILLIBUCKET("millibuckets", l -> l + " mB", l -> l + " millibuckets",1),
    DROPLET("droplets", l -> l + " d", l -> l + " droplets",81);

    private final String name;
    private final Function<String, String> shortFormFormatFunc;
    private final Function<String, String> longFormFormatFunc;
    private final long oneL;

    FluidUnit(String name, Function<String, String> shortFormFormatFunc, Function<String, String> longFormFormatFunc, long oneL) {
        this.name = name;
        this.shortFormFormatFunc = shortFormFormatFunc;
        this.longFormFormatFunc = longFormFormatFunc;
        this.oneL = oneL;
    }

    public long convert(long value, FluidUnit unit) {
        return convert(value, this, unit);
    }

    public long convertToLoader(long value) {
        return convertToLoader(value, this);
    }

    public static FluidUnit getOpposite(FluidUnit unit) {
        if (unit == DROPLET)
            return BrewinAndChewin.getHelper().getPlatform() == BnCPlatform.NEOFORGE ? MILLIBUCKET : LITER;
        return DROPLET;
    }

    public static FluidUnit getLoaderUnit() {
        return BrewinAndChewin.getHelper().getPlatform() == BnCPlatform.NEOFORGE ? MILLIBUCKET : DROPLET;
    }

    public static long convertToLoader(long value, FluidUnit unit) {
        return convert(value, unit, getLoaderUnit());
    }

    public static long convert(long value, FluidUnit originalUnit, FluidUnit newUnit) {
        if (originalUnit.oneL == newUnit.oneL)
            return value;
        return value / originalUnit.oneL * newUnit.oneL;
    }

    public String shortFormat(String value) {
        return shortFormFormatFunc.apply(value);
    }

    public String longFormat(String value) {
        return longFormFormatFunc.apply(value);
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static FluidUnit fromJson(JsonElement element) {
        if (element.isJsonPrimitive()) {
            String name = element.getAsString();
            return fromString(name).orElseThrow(() -> new IllegalArgumentException("Unknown FluidUnit: " + name));
        }
        throw new IllegalArgumentException("FluidUnit must be a string in JSON");
    }

    public JsonElement toJson() {
        return new JsonPrimitive(this.name());
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeByte(this.ordinal());
    }

    public static FluidUnit fromNetwork(FriendlyByteBuf buf) {
        int ord = buf.readByte();
        FluidUnit[] values = values();
        if (ord < 0 || ord >= values.length) ord = 0;
        return values[ord];
    }

    public static Optional<FluidUnit> fromString(String name) {
        for (FluidUnit unit : values()) {
            if (unit.name.equalsIgnoreCase(name) || unit.name().equalsIgnoreCase(name)) {
                return Optional.of(unit);
            }
        }
        return Optional.empty();
    }
}
