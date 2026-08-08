package umpaz.brewinandchewin.common.item;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Rarity;

public enum WineType implements StringRepresentable {
    RED("red", 0, 2400, 2, 0.7F),
    WHITE("white", 0, 3000, 4, 0.3F),
    CURRANT("currant", 0, 3600, 1, 1.2F),
    VERRUCA("verruca", 2, 2400, Integer.MAX_VALUE, 0.3F),
    TWISTED("twisted", 0, 3600, 8, 1.0F),
    RICE("rice", 1, 3600, 1, 1.4F),
    OLD("old", 2, 3600, 3, 1.2F);

    public static final Codec<WineType> CODEC = StringRepresentable.fromEnum(WineType::values);
    public static final StreamCodec<RegistryFriendlyByteBuf, WineType> STREAM_CODEC = ByteBufCodecs.idMapper(index -> values()[index], WineType::ordinal).cast();

    public static final int MAX_EFFECT_UPGRADES = 1;
    public static final int OLD_WINE_VARIANTS = 9;
    public static final int INTOXICATION_PER_EXTRA_DISTILLATE = 400;
    public static final int POISON_PER_EXTRA_DISTILLATE = 200;
    public static final int VERRUCA_POISON_THRESHOLD = 5;
    public static final float TWISTED_NEGATIVE_CHANCE = 0.5F;

    private final String name;
    private final int tipsyAmplifier;
    private final int tipsyDuration;
    private final int distillateCapacity;
    private final float durationModifier;

    WineType(String name, int tipsyAmplifier, int tipsyDuration, int distillateCapacity, float durationModifier) {
        this.name = name;
        this.tipsyAmplifier = tipsyAmplifier;
        this.tipsyDuration = tipsyDuration;
        this.distillateCapacity = distillateCapacity;
        this.durationModifier = durationModifier;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String getItemName() {
        return this.name + "_wine";
    }

    public int getTipsyAmplifier() {
        return this.tipsyAmplifier;
    }

    public int getTipsyDuration() {
        return this.tipsyDuration;
    }

    public int getDistillateCapacity() {
        return this.distillateCapacity;
    }

    public float getDurationModifier() {
        return this.durationModifier;
    }

    public boolean hasFortificationLimit() {
        return this.distillateCapacity != Integer.MAX_VALUE;
    }

    public boolean addsIntoxicationPerDistillate() {
        return this == VERRUCA || this == TWISTED;
    }

    public boolean poisonsWhenOverFortified() {
        return this == VERRUCA;
    }

    public boolean spawnsNegativeEffects() {
        return this == TWISTED;
    }

    public boolean isRandomlyGenerated() {
        return this == OLD;
    }

    public Rarity getFinelyAgedRarity() {
        return this == OLD ? Rarity.EPIC : Rarity.RARE;
    }
}
