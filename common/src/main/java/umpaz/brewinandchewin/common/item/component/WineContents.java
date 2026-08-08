package umpaz.brewinandchewin.common.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import umpaz.brewinandchewin.common.item.WineType;

import java.util.ArrayList;
import java.util.List;

public record WineContents(List<FortifiedEffect> effects, int distillates, int agingCycles, int tipsyBonus, boolean sickening, boolean delicious, int variant) {
    public static final WineContents EMPTY = new WineContents(List.of(), 0, 0, 0, false, false, 0);

    public static final Codec<WineContents> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            FortifiedEffect.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(WineContents::effects),
            Codec.INT.optionalFieldOf("distillates", 0).forGetter(WineContents::distillates),
            Codec.INT.optionalFieldOf("aging_cycles", 0).forGetter(WineContents::agingCycles),
            Codec.INT.optionalFieldOf("tipsy_bonus", 0).forGetter(WineContents::tipsyBonus),
            Codec.BOOL.optionalFieldOf("sickening", false).forGetter(WineContents::sickening),
            Codec.BOOL.optionalFieldOf("delicious", false).forGetter(WineContents::delicious),
            Codec.INT.optionalFieldOf("variant", 0).forGetter(WineContents::variant)
    ).apply(inst, WineContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WineContents> STREAM_CODEC = StreamCodec.of(
            (buf, contents) -> {
                FortifiedEffect.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, contents.effects());
                buf.writeVarInt(contents.distillates());
                buf.writeVarInt(contents.agingCycles());
                buf.writeVarInt(contents.tipsyBonus());
                buf.writeBoolean(contents.sickening());
                buf.writeBoolean(contents.delicious());
                buf.writeVarInt(contents.variant());
            },
            buf -> new WineContents(
                    FortifiedEffect.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readVarInt()));

    public boolean isEmpty() {
        return this.effects.isEmpty() && this.distillates == 0 && this.agingCycles == 0 && this.tipsyBonus == 0
                && !this.sickening && !this.delicious;
    }

    public boolean canUpgradeEffects() {
        return this.effects.stream().anyMatch(FortifiedEffect::canUpgrade);
    }

    public boolean canBeAged(WineType type) {
        return this.remainingCapacity(type) > 0 || this.canUpgradeEffects();
    }

    public boolean isFinelyAged(WineType type) {
        return !this.canBeAged(type);
    }

    public boolean hasFoam() {
        return this.sickening;
    }

    public boolean hasEffect(Holder<MobEffect> effect) {
        return this.effects.stream().anyMatch(instance -> instance.is(effect));
    }

    public int remainingCapacity(WineType type) {
        if (!type.hasFortificationLimit())
            return Integer.MAX_VALUE;
        return Math.max(0, type.getDistillateCapacity() - this.distillates);
    }

    public WineContents withEffects(List<FortifiedEffect> newEffects) {
        return new WineContents(List.copyOf(newEffects), this.distillates, this.agingCycles, this.tipsyBonus, this.sickening, this.delicious, this.variant);
    }

    public WineContents withDistillates(int newDistillates) {
        return new WineContents(this.effects, newDistillates, this.agingCycles, this.tipsyBonus, this.sickening, this.delicious, this.variant);
    }

    public WineContents withAgingCycles(int newAgingCycles) {
        return new WineContents(this.effects, this.distillates, newAgingCycles, this.tipsyBonus, this.sickening, this.delicious, this.variant);
    }

    public WineContents withTipsyBonus(int newTipsyBonus) {
        return new WineContents(this.effects, this.distillates, this.agingCycles, newTipsyBonus, this.sickening, this.delicious, this.variant);
    }

    public WineContents withSickening(boolean newSickening) {
        return new WineContents(this.effects, this.distillates, this.agingCycles, this.tipsyBonus, newSickening, this.delicious, this.variant);
    }

    public WineContents withDelicious(boolean newDelicious) {
        return new WineContents(this.effects, this.distillates, this.agingCycles, this.tipsyBonus, this.sickening, newDelicious, this.variant);
    }

    public WineContents withVariant(int newVariant) {
        return new WineContents(this.effects, this.distillates, this.agingCycles, this.tipsyBonus, this.sickening, this.delicious, newVariant);
    }

    public WineContents empowered() {
        List<FortifiedEffect> empowered = new ArrayList<>();
        for (FortifiedEffect effect : this.effects) {
            empowered.add(effect.upgraded());
        }
        return this.withEffects(empowered);
    }
}
