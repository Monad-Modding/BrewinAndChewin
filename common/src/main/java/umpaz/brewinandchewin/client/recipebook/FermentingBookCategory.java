package umpaz.brewinandchewin.client.recipebook;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum FermentingBookCategory implements StringRepresentable {
    MEALS("meals", 0),
    DRINKS("drinks", 1),
    MISC("misc", 2);

    final String name;
    final int id;

    public static final Codec<FermentingBookCategory> CODEC = StringRepresentable.fromEnum(FermentingBookCategory::values);
    public static final IntFunction<FermentingBookCategory> BY_ID = ByIdMap.continuous(FermentingBookCategory::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, FermentingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FermentingBookCategory::id);

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
}
