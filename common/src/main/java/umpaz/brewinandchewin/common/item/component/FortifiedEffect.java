package umpaz.brewinandchewin.common.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import umpaz.brewinandchewin.common.item.WineType;

public record FortifiedEffect(MobEffectInstance effect, int upgrades) {
    public static final Codec<FortifiedEffect> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            MobEffectInstance.CODEC.fieldOf("effect").forGetter(FortifiedEffect::effect),
            Codec.INT.optionalFieldOf("upgrades", 0).forGetter(FortifiedEffect::upgrades)
    ).apply(inst, FortifiedEffect::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FortifiedEffect> STREAM_CODEC = StreamCodec.composite(
            MobEffectInstance.STREAM_CODEC, FortifiedEffect::effect,
            ByteBufCodecs.VAR_INT, FortifiedEffect::upgrades,
            FortifiedEffect::new);

    public static FortifiedEffect of(MobEffectInstance effect) {
        return new FortifiedEffect(effect, 0);
    }

    public boolean is(Holder<MobEffect> holder) {
        return this.effect.is(holder);
    }

    public boolean canUpgrade() {
        return this.upgrades < WineType.MAX_EFFECT_UPGRADES;
    }

    public static FortifiedEffect finished(MobEffectInstance effect) {
        return new FortifiedEffect(effect, WineType.MAX_EFFECT_UPGRADES);
    }

    public FortifiedEffect upgraded() {
        if (!this.canUpgrade())
            return this;
        MobEffectInstance stronger = new MobEffectInstance(this.effect.getEffect(), this.effect.getDuration(),
                this.effect.getAmplifier() + 1, this.effect.isAmbient(), this.effect.isVisible(), this.effect.showIcon());
        return new FortifiedEffect(stronger, this.upgrades + 1);
    }
}
