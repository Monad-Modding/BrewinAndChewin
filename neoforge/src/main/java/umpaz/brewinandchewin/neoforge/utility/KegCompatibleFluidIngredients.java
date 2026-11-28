package umpaz.brewinandchewin.neoforge.utility;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import umpaz.brewinandchewin.common.utility.AbstractedFluidIngredient;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KegCompatibleFluidIngredients {
    public static final int DISPLAY_AMOUNT = 1000;
    public static final Codec<AbstractedFluidIngredient> CODEC = Codec.either(Codec.either(Exact.CODEC, Tag.CODEC), NeoForgeIngredient.CODEC)
            .xmap(either -> either.map(Either::unwrap, neoForgeIngredient -> neoForgeIngredient), wrapper -> {
                if (wrapper instanceof Exact exact)
                    return Either.left(Either.left(exact));
                if (wrapper instanceof Tag tag)
                    return Either.left(Either.right(tag));
                if (wrapper instanceof NeoForgeIngredient neoForgeIngredient)
                    return Either.right(neoForgeIngredient);
                throw new UnsupportedOperationException("Unsupported wrapped fluid ingredient class.");
            });

    public static final StreamCodec<RegistryFriendlyByteBuf, AbstractedFluidIngredient> STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.either(Exact.STREAM_CODEC, Tag.STREAM_CODEC), NeoForgeIngredient.STREAM_CODEC)
            .map(either -> either.map(Either::unwrap, neoForgeIngredient -> neoForgeIngredient), wrapper -> {
                if (wrapper instanceof Exact exact)
                    return Either.left(Either.left(exact));
                if (wrapper instanceof Tag tag)
                    return Either.left(Either.right(tag));
                if (wrapper instanceof NeoForgeIngredient neoForgeIngredient)
                    return Either.right(neoForgeIngredient);
                throw new UnsupportedOperationException("Unsupported wrapped fluid ingredient class.");
            });

    public static class Exact implements AbstractedFluidIngredient {
        public static final Codec<Exact> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                        FluidStack.FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(stack -> stack.displayStack.fluid().builtInRegistryHolder()),
                        DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(fluidStack -> fluidStack.displayStack.components() instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY))
                .apply(inst, (t1, t2) -> new Exact(t1.value(), t2)));
        public static final StreamCodec<RegistryFriendlyByteBuf, Exact> STREAM_CODEC = AbstractedFluidStack.STREAM_CODEC.map(Exact::new, exact -> exact.displayStack);
        private final AbstractedFluidStack displayStack;

        private Exact(AbstractedFluidStack displayStack) {
            this.displayStack = displayStack;
        }

        public Exact(Fluid fluid, PatchedDataComponentMap components) {
            displayStack = new AbstractedFluidStack(fluid, DISPLAY_AMOUNT, components, FluidUnit.MILLIBUCKET, new FluidStack(fluid.builtInRegistryHolder(), DISPLAY_AMOUNT, components.asPatch()));
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
                RegistryCodecs.homogeneousList(Registries.FLUID).fieldOf("tag").forGetter(tag -> tag.fluidTag),
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

        public TagKey<Fluid> getTagKey() {
            return fluidTag.unwrapKey().orElse(null);
        }

        @Override
        public List<AbstractedFluidStack> displayStacks() {
            if (fluidTag.size() > 0 && fluidStacks.isEmpty()) {
                for (Holder<Fluid> fluidHolder :  fluidTag) {
                    fluidStacks.add(new AbstractedFluidStack(fluidHolder.value(), DISPLAY_AMOUNT, components, FluidUnit.MILLIBUCKET, new FluidStack(fluidHolder, DISPLAY_AMOUNT, components.asPatch())));
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

    public static class NeoForgeIngredient implements AbstractedFluidIngredient {
        public static final Codec<NeoForgeIngredient> CODEC = FluidIngredient.CODEC.xmap(NeoForgeIngredient::new, neoForgeIngredient -> neoForgeIngredient.ingredient);
        public static final StreamCodec<RegistryFriendlyByteBuf, NeoForgeIngredient> STREAM_CODEC = FluidIngredient.STREAM_CODEC.map(NeoForgeIngredient::new, neoForgeIngredient -> neoForgeIngredient.ingredient);

        private final FluidIngredient ingredient;
        private final List<AbstractedFluidStack> displayStacks;

        public NeoForgeIngredient(FluidIngredient ingredient) {
            this.ingredient = ingredient;
            displayStacks = Arrays.stream(ingredient.getStacks()).map(fluidStack -> new AbstractedFluidStack(fluidStack.getFluid(), DISPLAY_AMOUNT, fluidStack.getComponents(), FluidUnit.MILLIBUCKET, fluidStack)).toList();
        }

        @Override
        public List<AbstractedFluidStack> displayStacks() {
            return displayStacks;
        }

        @Override
        public boolean matches(AbstractedFluidStack wrapper) {
            if (!wrapper.isEmpty() && wrapper.loaderSpecific() instanceof FluidStack fluidStack)
                return ingredient.test(fluidStack);
            return ingredient.test(FluidStack.EMPTY);
        }
    }
}