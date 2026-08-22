package umpaz.brewinandchewin.common.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import umpaz.brewinandchewin.common.registry.BnCItems;

import java.util.function.Supplier;

public enum GrapeColour implements StringRepresentable {
    NONE("none", () -> null, () -> null),
    RED("red", () -> BnCItems.RED_GRAPES, () -> BnCItems.RED_GRAPE_SEEDS),
    WHITE("white", () -> BnCItems.WHITE_GRAPES, () -> BnCItems.WHITE_GRAPE_SEEDS);

    private final String name;
    private final Supplier<Item> grapes;
    private final Supplier<Item> seeds;

    GrapeColour(String name, Supplier<Item> grapes, Supplier<Item> seeds) {
        this.name = name;
        this.grapes = grapes;
        this.seeds = seeds;
    }

    public static GrapeColour fromSeed(Item item) {
        for (GrapeColour colour : values())
            if (colour != NONE && colour.seeds.get() == item)
                return colour;
        return NONE;
    }

    public static Item seedsOf(GrapeColour colour) {
        return colour.seeds.get();
    }

    public Item getGrapes() {
        return this.grapes.get();
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
