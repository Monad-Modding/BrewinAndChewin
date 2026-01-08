package umpaz.brewinandchewin.common.crafting;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.common.utility.KegContainer;

import java.util.Objects;
import java.util.Optional;

public class KegPouringRecipe implements Recipe<KegContainer> {
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
    public boolean matches(KegContainer container, Level level) {
        return Ingredient.of(getContainer()).test(container.getItem(4));
    }

    public ItemStack assemble(KegContainer container, RegistryAccess registryAccess) {
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
    public ItemStack getResultItem(RegistryAccess provider) {
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

    @Override
    public boolean isSpecial() {
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
    public ResourceLocation getId() {
        return null;
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
        @Override
        public KegPouringRecipe fromJson(ResourceLocation resourceLocation, JsonObject json) {

            AbstractedFluidStack fluid = AbstractedFluidStack.fromJson(
                    GsonHelper.getAsJsonObject(json, "fluid")
            );

            // Optional container
            Optional<ItemStack> container = Optional.empty();
            if (json.has("container")) {
                container = Optional.of(
                        ShapedRecipe.itemStackFromJson(
                                GsonHelper.getAsJsonObject(json, "container")
                        )
                );
            }

            // Required output
            ItemStack output = ShapedRecipe.itemStackFromJson(
                    GsonHelper.getAsJsonObject(json, "output")
            );

            // Optional unit
            Optional<FluidUnit> unit = Optional.empty();
            if (json.has("unit")) {
                unit = Optional.of(FluidUnit.fromJson(json.get("unit")));
            }

            // Optional flags
            boolean strict = GsonHelper.getAsBoolean(json, "strict", false);
            boolean canFill = GsonHelper.getAsBoolean(json, "can_fill", true);

            return new KegPouringRecipe(fluid, container, output, unit, strict, canFill);
        }

        @Override
        public KegPouringRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf buf) {
            AbstractedFluidStack fluid = AbstractedFluidStack.fromNetwork(buf);

            Optional<ItemStack> container =
                    buf.readBoolean() ? Optional.of(buf.readItem()) : Optional.empty();

            ItemStack output = buf.readItem();

            Optional<FluidUnit> unit =
                    buf.readBoolean() ? Optional.of(FluidUnit.fromNetwork(buf)) : Optional.empty();

            boolean strict = buf.readBoolean();
            boolean canFill = buf.readBoolean();

            return new KegPouringRecipe(fluid, container, output, unit, strict, canFill);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, KegPouringRecipe recipe) {
            // Fluid (required)
            recipe.getRawFluid().toNetwork(buf);

            // Optional container
            buf.writeBoolean(recipe.getRawContainer().isPresent());
            recipe.getRawContainer().ifPresent(buf::writeItem);

            // Output
            buf.writeItem(recipe.getOutput());

            // Optional unit
            buf.writeBoolean(recipe.getRawUnit().isPresent());
            recipe.getRawUnit().ifPresent(u -> u.toNetwork(buf));

            // Flags
            buf.writeBoolean(recipe.isStrict());
            buf.writeBoolean(recipe.canFill());
        }
    }
}
