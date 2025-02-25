package umpaz.brewinandchewin.common.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class KegPouringRecipe implements Recipe<KegRecipeWrapper> {
    private final ResourceLocation id;
    private final Fluid fluid;
    private final int amount;
    private final Optional<ItemStack> container;
    private final ItemStack output;
    private final boolean strict;
    private final boolean filling;

    public KegPouringRecipe(ResourceLocation id, Fluid fluid, Optional<ItemStack> container, ItemStack output, int amount, boolean strict, boolean filling) {
        this.id = id;
        this.amount = amount;
        this.fluid = fluid;
        this.container = container;
        this.output = output;
        this.strict = strict;
        this.filling = filling;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
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

    @Override
    public ItemStack assemble(KegRecipeWrapper recipeWrapper, RegistryAccess registryAccess) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    public ItemStack getContainer(){
        return this.container.orElse(output.getCraftingRemainingItem());
    }

    public Optional<ItemStack> getRawContainer(){
        return this.container;
    }

    public ItemStack getOutput(){
        return this.output;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return this.output;
    }

    public int getAmount() {
        return this.amount;
    }

    public FluidStack getFluid(ItemStack container) {
        return new FluidStack(fluid, amount);
    }

    public Fluid getRawFluid() {
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
        return BnCRecipeSerializers.KEG_POURING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return BnCRecipeTypes.KEG_POURING.get();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(BnCItems.KEG.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fluid, amount, container, output, strict, filling);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KegPouringRecipe that = (KegPouringRecipe) o;

        if (!getId().equals(that.getId())) return false;
        if (!output.equals(that.output)) return false;
        if (amount != that.amount) return false;
        if (!fluid.equals(that.fluid)) return false;
        if (!container.equals(that.container)) return false;
        if (strict != that.strict) return false;
        return filling == that.filling;
    }

    public static class Serializer implements RecipeSerializer<KegPouringRecipe> {
        public Serializer() {
        }

        @Override
        public KegPouringRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            final Fluid fluidIn = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "fluid")));
            final int amountIn = GsonHelper.getAsInt(json, "amount", 250);
            Optional<ItemStack> container = GsonHelper.isValidNode(json, "container") ? Optional.of(CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "container"), true)) : Optional.empty();
            final ItemStack outputIn = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "output"), true);
            final boolean strictIn = GsonHelper.getAsBoolean(json, "strict", false);
            final boolean fillingIn = GsonHelper.getAsBoolean(json, "filling", true);
            if (!outputIn.hasCraftingRemainingItem() && container.isEmpty())
                throw new JsonParseException("\"container\" field must be specified if the output doesn't have a crafting remainder item.");
            return new KegPouringRecipe(recipeId, fluidIn, container, outputIn, amountIn, strictIn, fillingIn);
        }

        @Nullable
        @Override
        public KegPouringRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Fluid fluidIn = buffer.readFluidStack().getFluid();
            int amountIn = buffer.readVarInt();
            Optional<ItemStack> containerIn = buffer.readOptional(FriendlyByteBuf::readItem);
            ItemStack outputIn = buffer.readItem();
            boolean strictIn  = buffer.readBoolean();
            boolean fillingIn = buffer.readBoolean();
            return new KegPouringRecipe(recipeId, fluidIn, containerIn, outputIn, amountIn, strictIn, fillingIn);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, KegPouringRecipe recipe) {
            buffer.writeFluidStack(new FluidStack(recipe.fluid, 1000));
            buffer.writeVarInt(recipe.amount);
            buffer.writeOptional(recipe.container, FriendlyByteBuf::writeItem);
            buffer.writeItem(recipe.output);
            buffer.writeBoolean(recipe.strict);
            buffer.writeBoolean(recipe.filling);
        }
    }
}
