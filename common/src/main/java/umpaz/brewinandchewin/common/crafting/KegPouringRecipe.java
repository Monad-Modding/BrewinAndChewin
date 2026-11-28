package umpaz.brewinandchewin.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCCommonRecipeBookCategories;
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
    private final Optional<ItemStackTemplate> container;
    private final ItemStackTemplate output;
    private final Optional<FluidUnit> unit;
    private final boolean strict;
    private final boolean filling;

    public KegPouringRecipe(AbstractedFluidStack fluid, Optional<ItemStackTemplate> container, ItemStackTemplate output, Optional<FluidUnit> unit, boolean strict, boolean filling) {
        if (container.isEmpty() && output.item().value().getCraftingRemainder() == null)
            throw new UnsupportedOperationException("'container' field must be specified as the output item stack doesn't have a crafting remainder item.");
        this.fluid = fluid;
        this.container = container;
        this.output = output;
        this.unit = unit;
        this.strict = strict;
        this.filling = filling;
    }

    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredient = NonNullList.create();
        ingredient.add(Ingredient.of(getContainer().getItem()));
        return ingredient;
    }

    @Override
    public boolean matches(KegRecipeWrapper inv, Level level) {
        return strict ? ItemStack.isSameItemSameComponents(getContainer(), inv.getItem(4)) : ItemStack.isSameItem(getContainer(), inv.getItem(4));
    }

    @Override
    public ItemStack assemble(KegRecipeWrapper recipeWrapper) {
        return this.output.create();
    }

    public ItemStack assemble(KegRecipeWrapper recipeWrapper, HolderLookup.Provider provider) {
        return assemble(recipeWrapper);
    }

    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    public ItemStack getContainer() {
        return this.container.map(ItemStackTemplate::create).orElseGet(() -> BrewinAndChewin.getHelper().getCraftingRemainingItem(getOutput()));
    }

    public ItemStack getContainer(ItemStack stack) {
        return this.container.map(ItemStackTemplate::create).orElseGet(() -> BrewinAndChewin.getHelper().getCraftingRemainingItem(stack));
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
        return this.container.map(ItemStackTemplate::create);
    }

    public Optional<ItemStackTemplate> getRawContainerTemplate() {
        return this.container;
    }

    public ItemStack getOutput(){
        return this.output.create();
    }

    public ItemStackTemplate getOutputTemplate() {
        return this.output;
    }

    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.output.create();
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
    public RecipeSerializer<? extends Recipe<KegRecipeWrapper>> getSerializer() {
        return BnCRecipeSerializers.KEG_POURING;
    }

    @Override
    public RecipeType<? extends Recipe<KegRecipeWrapper>> getType() {
        return BnCRecipeTypes.KEG_POURING;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return BnCCommonRecipeBookCategories.KEG_POURING;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

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

    public static class Serializer {
        public static final MapCodec<KegPouringRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                AbstractedFluidStack.CODEC.fieldOf("fluid").forGetter(KegPouringRecipe::getRawFluid),
                ItemStackTemplate.CODEC.optionalFieldOf("container").forGetter(KegPouringRecipe::getRawContainerTemplate),
                ItemStackTemplate.CODEC.fieldOf("output").forGetter(KegPouringRecipe::getOutputTemplate),
                FluidUnit.CODEC.optionalFieldOf("unit").forGetter(KegPouringRecipe::getRawUnit),
                Codec.BOOL.optionalFieldOf("strict", false).forGetter(KegPouringRecipe::isStrict),
                Codec.BOOL.optionalFieldOf("can_fill", true).forGetter(KegPouringRecipe::canFill)
        ).apply(inst, KegPouringRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, KegPouringRecipe> STREAM_CODEC = StreamCodec.of(KegPouringRecipe.Serializer::toNetwork, KegPouringRecipe.Serializer::fromNetwork);

        public static void toNetwork(RegistryFriendlyByteBuf buf, KegPouringRecipe recipe) {
            AbstractedFluidStack.STREAM_CODEC.encode(buf, recipe.getRawFluid());
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC).encode(buf, recipe.getRawContainerTemplate());
            ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.getOutputTemplate());
            ByteBufCodecs.optional(FluidUnit.STREAM_CODEC).encode(buf, recipe.getRawUnit());
            ByteBufCodecs.BOOL.encode(buf, recipe.isStrict());
            ByteBufCodecs.BOOL.encode(buf, recipe.canFill());
        }

        public static KegPouringRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            AbstractedFluidStack fluid = AbstractedFluidStack.STREAM_CODEC.decode(buf);
            Optional<ItemStackTemplate> container = ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC).decode(buf);
            ItemStackTemplate output = ItemStackTemplate.STREAM_CODEC.decode(buf);
            Optional<FluidUnit> unit = ByteBufCodecs.optional(FluidUnit.STREAM_CODEC).decode(buf);
            boolean strict = buf.readBoolean();
            boolean canFill = buf.readBoolean();

            return new KegPouringRecipe(fluid, container, output, unit, strict, canFill);
        }
    }
}
