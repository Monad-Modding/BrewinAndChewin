package umpaz.brewinandchewin.fabric.utility;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import umpaz.brewinandchewin.common.utility.FluidUnit;

public record AmountedFluidVariant(FluidVariant variant, long amount, FluidUnit fluidUnit) {
    public static final AmountedFluidVariant EMPTY = new AmountedFluidVariant(FluidVariant.blank(), 0, FluidUnit.DROPLET);
    public static final Codec<AmountedFluidVariant> DIRECT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(amounted -> amounted.variant.getFluid()),
            Codec.LONG.fieldOf("amount").forGetter(AmountedFluidVariant::amount),
            FluidUnit.CODEC.optionalFieldOf("unit", FluidUnit.DROPLET).forGetter(AmountedFluidVariant::fluidUnit),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(amounted -> amounted.variant.getComponents())
    ).apply(inst, (t1, t2, t3, t4) -> new AmountedFluidVariant(FluidVariant.of(t1, t4), t2, t3)));
    public static final Codec<AmountedFluidVariant> ALTERNATIVE_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(amounted -> amounted.variant.getFluid()),
            Codec.LONG.fieldOf("amount").forGetter(AmountedFluidVariant::amount),
            FluidUnit.CODEC.optionalFieldOf("unit", FluidUnit.DROPLET).forGetter(AmountedFluidVariant::fluidUnit),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(amounted -> amounted.variant.getComponents())
    ).apply(inst, (t1, t2, t3, t4) -> new AmountedFluidVariant(FluidVariant.of(t1, t4), t2, t3)));
    public static final Codec<AmountedFluidVariant> CODEC = Codec.withAlternative(DIRECT_CODEC, ALTERNATIVE_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, AmountedFluidVariant> STREAM_CODEC = StreamCodec.composite(
            FluidVariant.PACKET_CODEC, AmountedFluidVariant::variant,
            BnCStreamCodecs.LONG, AmountedFluidVariant::amount,
            FluidUnit.STREAM_CODEC, AmountedFluidVariant::fluidUnit,
            AmountedFluidVariant::new
    );
}
