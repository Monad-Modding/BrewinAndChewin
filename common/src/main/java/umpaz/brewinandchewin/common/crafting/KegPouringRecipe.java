package umpaz.brewinandchewin.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

import java.util.Objects;
import java.util.Optional;

public class KegPouringRecipe implements Recipe<KegRecipeWrapper> {
    private final AbstractedFluidStack fluid;
    private final Optional<ItemStack> container;
    private final ItemStack output;
    private final Optional<FluidUnit> unit;
    private final boolean strict;
    private final boolean filling;

    public KegPouringRecipe(AbstractedFluidStack fluid, Optional<ItemStack> container, ItemStack output, Optional<FluidUnit> unit, boolean strict, boolean filling) {
        if (container.isEmpty() && BrewinAndChewin.getHelper().getCraftingRemainingItem(output).isEmpty())
            throw new UnsupportedOperationException("'container' field must be specified as the output item stack doesn't have a crafting remainder item.");
        this.fluid = fluid;
        this.container = container;
        this.output = output;
        this.unit = unit;
        this.strict = strict;
        this.filling = filling;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredient = NonNullList.create();
        ingredient.add(Ingredient.of(getContainer()));
        return ingredient;
    }

    @Override
    public boolean matches(KegRecipeWrapper inv, Level level) {
        return Ingredient.of(getContainer()).test(inv.getItem(4));
    }

    public ItemStack assemble(KegRecipeWrapper recipeWrapper, RegistryAccess registryAccess) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    public ItemStack getContainer() {
        return this.container.orElse(BrewinAndChewin.getHelper().getCraftingRemainingItem(output));
    }

    public ItemStack getContainer(ItemStack stack) {
        return this.container.orElse(BrewinAndChewin.getHelper().getCraftingRemainingItem(stack));
    }

    public Optional<FluidUnit> getRawUnit() {
        return unit;
    }

    public FluidUnit getUnit() {
        return unit.orElse(FluidUnit.getLoaderUnit());
    }

    public long getLoaderAmount() {
        return getUnit().convertToLoader(fluid.amount());
    }

    public Optional<ItemStack> getRawContainer(){
        return this.container;
    }

    public ItemStack getOutput(){
        return this.output;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.output;
    }

    public AbstractedFluidStack getFluid(ItemStack container) {
        return fluid;
    }

    public AbstractedFluidStack getRawFluid() {
        return this.fluid;
    }

    public boolean hasSpecialFluid() {
        return false;
    }

    public boolean isStrict() {
        return strict;
    }

    public boolean canFill() {
        return filling;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BnCRecipeSerializers.KEG_POURING;
    }

    @Override
    public RecipeType<?> getType() {
        return BnCRecipeTypes.KEG_POURING;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(BnCItems.KEG);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluid, container, output, strict, filling);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KegPouringRecipe that = (KegPouringRecipe) o;

        if (!output.equals(that.output)) return false;
        if (!fluid.equals(that.fluid)) return false;
        if (!container.equals(that.container)) return false;
        if (strict != that.strict) return false;
        return filling == that.filling;
    }

    public static class Serializer implements RecipeSerializer<KegPouringRecipe> {
        public static final MapCodec<KegPouringRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                AbstractedFluidStack.CODEC.fieldOf("fluid").forGetter(KegPouringRecipe::getRawFluid),
                ItemStack.CODEC.optionalFieldOf("container").forGetter(KegPouringRecipe::getRawContainer),
                ItemStack.CODEC.fieldOf("output").forGetter(KegPouringRecipe::getOutput),
                FluidUnit.CODEC.optionalFieldOf("unit").forGetter(KegPouringRecipe::getRawUnit),
                Codec.BOOL.optionalFieldOf("strict", false).forGetter(KegPouringRecipe::isStrict),
                Codec.BOOL.optionalFieldOf("can_fill", true).forGetter(KegPouringRecipe::canFill)
        ).apply(inst, KegPouringRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, KegPouringRecipe> STREAM_CODEC = StreamCodec.of(KegPouringRecipe.Serializer::toNetwork, KegPouringRecipe.Serializer::fromNetwork);

        public Serializer() {}

        public MapCodec<KegPouringRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, KegPouringRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static void toNetwork(RegistryFriendlyByteBuf buf, KegPouringRecipe recipe) {
            AbstractedFluidStack.STREAM_CODEC.encode(buf, recipe.getRawFluid());
            ByteBufCodecs.optional(ItemStack.STREAM_CODEC).encode(buf, recipe.getRawContainer());
            ItemStack.STREAM_CODEC.encode(buf, recipe.getOutput());
            ByteBufCodecs.optional(FluidUnit.STREAM_CODEC).encode(buf, recipe.getRawUnit());
            ByteBufCodecs.BOOL.encode(buf, recipe.isStrict());
            ByteBufCodecs.BOOL.encode(buf, recipe.canFill());
        }

        public static KegPouringRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            AbstractedFluidStack fluid = AbstractedFluidStack.STREAM_CODEC.decode(buf);
            Optional<ItemStack> container = ByteBufCodecs.optional(ItemStack.STREAM_CODEC).decode(buf);
            ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
            Optional<FluidUnit> unit = ByteBufCodecs.optional(FluidUnit.STREAM_CODEC).decode(buf);
            boolean strict = buf.readBoolean();
            boolean canFill = buf.readBoolean();

            return new KegPouringRecipe(fluid, container, output, unit, strict, canFill);
        }
    }
}
