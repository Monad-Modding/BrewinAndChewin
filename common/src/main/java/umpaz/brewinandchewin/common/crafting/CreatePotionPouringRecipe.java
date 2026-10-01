package umpaz.brewinandchewin.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.RecipeSerializer;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

import java.util.Optional;

public class CreatePotionPouringRecipe extends KegPouringRecipe {
    public CreatePotionPouringRecipe(Optional<ItemStack> container, ItemStack result, long amount, Optional<FluidUnit> unit, boolean canFill) {
        super(new AbstractedFluidStack(BrewinAndChewin.getHelper().getCreatePotionFluid(), amount), container, result, unit, false, canFill);
    }

    @Override
    public ItemStack assemble(KegRecipeWrapper recipeWrapper, HolderLookup.Provider provider) {
        ItemStack stack = super.getResultItem(provider).copy();
        AbstractedFluidStack fluidStack = recipeWrapper.getFluid();
        if (fluidStack.components().has(DataComponents.POTION_CONTENTS) && fluidStack.components().get(DataComponents.POTION_CONTENTS) != PotionContents.EMPTY)
            stack.set(DataComponents.POTION_CONTENTS, fluidStack.components().get(DataComponents.POTION_CONTENTS));
        return stack;
    }

    @Override
    public AbstractedFluidStack getFluid(ItemStack container) {
        AbstractedFluidStack fluidStack = super.getFluid(container);
        DataComponentPatch.Builder patch = DataComponentPatch.builder();
        for (var entry : container.getComponentsPatch().entrySet())
            patch.set((DataComponentType<Object>) entry.getKey(), entry.getValue().get());
        if (container.has(DataComponents.POTION_CONTENTS) && container.get(DataComponents.POTION_CONTENTS) != PotionContents.EMPTY)
            patch.set(DataComponents.POTION_CONTENTS, container.get(DataComponents.POTION_CONTENTS));
        return new AbstractedFluidStack(fluidStack.fluid(), fluidStack.amount(), PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch.build()), getUnit());
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
    
    @Override
    @SuppressWarnings("DataFlowIssue")
    public RecipeSerializer<?> getSerializer() {
        return BnCRecipeSerializers.CREATE_POTION_POURING;
    }

    public static class Serializer implements RecipeSerializer<CreatePotionPouringRecipe> {
        public static final MapCodec<CreatePotionPouringRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                ItemStack.CODEC.optionalFieldOf("container").forGetter(KegPouringRecipe::getRawContainer),
                ItemStack.CODEC.fieldOf("output").forGetter(KegPouringRecipe::getOutput),
                Codec.LONG.fieldOf("fluid_amount").forGetter(pouringRecipe -> pouringRecipe.getRawFluid().amount()),
                FluidUnit.CODEC.optionalFieldOf("unit").forGetter(KegPouringRecipe::getRawUnit),
                Codec.BOOL.optionalFieldOf("can_fill", true).forGetter(KegPouringRecipe::canFill)
        ).apply(inst, CreatePotionPouringRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, CreatePotionPouringRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        public Serializer() {}

        public static CreatePotionPouringRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Optional<ItemStack> container = ByteBufCodecs.optional(ItemStack.STREAM_CODEC).decode(buf);
            ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
            long amount = buf.readLong();
            Optional<FluidUnit> unit = ByteBufCodecs.optional(FluidUnit.STREAM_CODEC).decode(buf);
            boolean canFill = buf.readBoolean();
            return new CreatePotionPouringRecipe(container, output, amount, unit, canFill);
        }

        public static void toNetwork(RegistryFriendlyByteBuf buf, CreatePotionPouringRecipe recipe) {
            ByteBufCodecs.optional(ItemStack.STREAM_CODEC).encode(buf, recipe.getRawContainer());
            ItemStack.STREAM_CODEC.encode(buf, recipe.getOutput());
            buf.writeLong(recipe.getRawFluid().amount());
            ByteBufCodecs.optional(FluidUnit.STREAM_CODEC).encode(buf, recipe.getRawUnit());
            ByteBufCodecs.BOOL.encode(buf, recipe.canFill());
        }

        @Override
        public MapCodec<CreatePotionPouringRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CreatePotionPouringRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
