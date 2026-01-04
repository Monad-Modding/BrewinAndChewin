package umpaz.brewinandchewin.client.recipebook;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;

public enum FermentingBookCategory implements StringRepresentable {
    MEALS("meals", 0),
    DRINKS("drinks", 1);

    final String name;
    final int id;

    FermentingBookCategory(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    private int id() {
        return id;
    }


    // Lookup by id
    public static FermentingBookCategory byId(int id) {
        for (FermentingBookCategory cat : values()) {
            if (cat.id == id) return cat;
        }
        return DRINKS; // fallback
    }

    // Lookup by name
    public static FermentingBookCategory fromString(String name) {
        return Arrays.stream(values())
                .filter(cat -> cat.name.equals(name))
                .findFirst()
                .orElse(DRINKS);
    }

    // Write to network
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(id);
    }

    // Read from network
    public static FermentingBookCategory read(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        return byId(id);
    }
}
