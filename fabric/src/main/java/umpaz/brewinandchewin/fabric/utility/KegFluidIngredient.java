package umpaz.brewinandchewin.fabric.utility;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.utility.AbstractedFluidIngredient;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;

import java.util.ArrayList;
import java.util.List;

public class KegFluidIngredient {
    public static final Codec<AbstractedFluidIngredient> CODEC = Codec.either(Exact.CODEC, Tag.CODEC)
            .xmap(Either::unwrap, wrapper -> {
                if (wrapper instanceof Exact exact)
                    return Either.left(exact);
                if (wrapper instanceof Tag neoForgeIngredient)
                    return Either.right(neoForgeIngredient);
                throw new UnsupportedOperationException("Unsupported wrapped fluid ingredient class.");
            });
    public static final StreamCodec<RegistryFriendlyByteBuf, AbstractedFluidIngredient> STREAM_CODEC = ByteBufCodecs.either(Exact.STREAM_CODEC, Tag.STREAM_CODEC)
            .map(Either::unwrap, wrapper -> {
                if (wrapper instanceof Exact exact)
                    return Either.left(exact);
                if (wrapper instanceof Tag neoForgeIngredient)
                    return Either.right(neoForgeIngredient);
                throw new UnsupportedOperationException("Unsupported wrapped fluid ingredient class.");
            });

    public static class Exact implements AbstractedFluidIngredient {
        public static final Codec<Exact> DIRECT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
                BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(exact -> exact.displayStack.fluid()),
                DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(exact -> exact.displayStack.components() instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY)
        ).apply(inst, Exact::new));
        public static final Codec<Exact> ALTERNATIVE_CODEC = RecordCodecBuilder.create(inst -> inst.group(
                BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(exact -> exact.displayStack.fluid()),
                DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(exact -> exact.displayStack.components() instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY)
        ).apply(inst, Exact::new));
        public static final Codec<Exact> CODEC = Codec.withAlternative(DIRECT_CODEC, ALTERNATIVE_CODEC);
        public static final StreamCodec<RegistryFriendlyByteBuf, Exact> STREAM_CODEC = BnCFabricStreamCodecs.FLUID_STACK_WRAPPER.map(Exact::new, exact -> exact.displayStack);

        private final AbstractedFluidStack displayStack;

        private Exact(AbstractedFluidStack displayStack) {
            this.displayStack = displayStack;
        }

        public Exact(Fluid fluid, PatchedDataComponentMap components) {
            displayStack = new AbstractedFluidStack(fluid, 81000L, components, FluidUnit.DROPLET, new AmountedFluidVariant(FluidVariant.of(fluid, components.asPatch()), 81000L, FluidUnit.DROPLET));
        }

        public Exact(Fluid fluid, DataComponentPatch patch) {
            this(fluid, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch));
        }

        public Exact(Fluid fluid) {
            this(fluid, new PatchedDataComponentMap(DataComponentMap.EMPTY));
        }

        @Override
        public List<AbstractedFluidStack> displayStacks() {
            return List.of(displayStack);
        }

        @Override
        public boolean matches(AbstractedFluidStack wrapper) {
            if (displayStack.components().isEmpty())
                return displayStack.fluid().isSame(wrapper.fluid());
            return displayStack.matches(wrapper);
        }
    }

    public static class Tag implements AbstractedFluidIngredient {
        public static final Codec<Tag> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                RegistryCodecs.holderSet(Registries.FLUID).fieldOf("tag").forGetter(tag -> tag.fluidTag),
                DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(tag -> tag.components instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY)
        ).apply(inst, Tag::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Tag> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.holderSet(Registries.FLUID), tag -> tag.fluidTag,
                DataComponentPatch.STREAM_CODEC, tag -> tag.components.asPatch(),
                Tag::new
        );

        private final HolderSet<Fluid> fluidTag;
        private final PatchedDataComponentMap components;
        private final List<AbstractedFluidStack> fluidStacks = new ArrayList<>();

        public Tag(HolderSet<Fluid> fluidTag, PatchedDataComponentMap components) {
            this.fluidTag = fluidTag;
            this.components = components;
        }

        public Tag(HolderSet<Fluid> fluid, DataComponentPatch patch) {
            this(fluid, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch));
        }

        public Tag(HolderSet<Fluid> fluid) {
            this(fluid, new PatchedDataComponentMap(DataComponentMap.EMPTY));
        }

        @Nullable
        public TagKey<Fluid> getTagKey() {
            return fluidTag.unwrapKey().orElse(null);
        }

        @Override
        public List<AbstractedFluidStack> displayStacks() {
            if (fluidTag.size() > 0 && fluidStacks.isEmpty()) {
                for (Holder<Fluid> fluidHolder :  fluidTag) {
                    fluidStacks.add(new AbstractedFluidStack(fluidHolder.value(), 81000L, components, FluidUnit.DROPLET, new AmountedFluidVariant(FluidVariant.of(fluidHolder.value(), components.asPatch()), 81000L, FluidUnit.DROPLET)));
                }
            }
            return fluidStacks;
        }

        @Override
        public boolean matches(AbstractedFluidStack wrapper) {
            if (components.isEmpty())
                return fluidTag.contains(wrapper.fluid().builtInRegistryHolder());
            return fluidTag.contains(wrapper.fluid().builtInRegistryHolder()) && wrapper.components().equals(components);
        }
    }
}