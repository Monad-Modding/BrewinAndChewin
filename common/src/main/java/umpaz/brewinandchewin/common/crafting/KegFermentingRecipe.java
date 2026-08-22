package umpaz.brewinandchewin.common.crafting;

import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import umpaz.brewinandchewin.client.recipebook.FermentingBookCategory;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.BnCRecipeUtils;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class KegFermentingRecipe implements Recipe<KegRecipeWrapper> {
    public static final int INPUT_SLOTS = 4;
    public static final int FERMENTING_TIME_PER_BUCKET = 18000;
    public static final long BUCKET = 1000L;

    private final NonNullList<Ingredient> inputItems;

    private final Optional<FluidIngredientWithAmount> fluidIngredient;
    private final Optional<FluidUnit> unit;

    private final FermentingBookCategory tab;

    private final Either<AbstractedFluidStack, ItemStack> result;

    private final float experience;
    private final int temperature;

    public KegFermentingRecipe(NonNullList<Ingredient> inputItems, FermentingBookCategory tab, Optional<FluidIngredientWithAmount> fluidIngredient, Optional<FluidUnit> unit, Either<AbstractedFluidStack, ItemStack> result, float experience, int temperature) {
        this.inputItems = inputItems;
        this.tab = tab;
        this.fluidIngredient = fluidIngredient;
        this.unit = unit;
        if (unit.isPresent() && result.left().isPresent())
            this.result = Either.left(new AbstractedFluidStack(result.left().get().fluid(), result.left().get().amount(), result.left().get().components(), unit.get(), result.left().get().loaderSpecific()));
        else
            this.result = result;
        this.experience = experience;
        this.temperature = temperature;
    }

    public FermentingBookCategory getRecipeBookCategory() {
        return this.tab;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.inputItems;
    }

    public Optional<FluidIngredientWithAmount> getFluidIngredient() {
        return fluidIngredient;
    }

    public Optional<FluidUnit> getRawUnit() {
        return unit;
    }

    public FluidUnit getUnit() {
        return unit.orElse(FluidUnit.getLoaderUnit());
    }

    public Either<AbstractedFluidStack, ItemStack> getResult() {
        return result;
    }

    public long getLoaderFluidAmount() {
        if (this.fluidIngredient.isEmpty())
            return 0L;
        FluidIngredientWithAmount ingredient = this.fluidIngredient.get();
        return FluidUnit.convertToLoader(ingredient.amount(), ingredient.unit().orElse(getUnit()));
    }

    public long getBatchCount(AbstractedFluidStack tankFluid) {
        long required = getLoaderFluidAmount();
        if (required <= 0L)
            return 1L;
        return Math.max(1L, FluidUnit.convertToLoader(tankFluid.amount(), tankFluid.unit()) / required);
    }

    @Override
    public ItemStack assemble(KegRecipeWrapper inv, HolderLookup.Provider access) {
        return ItemStack.EMPTY;
    }

    public float getExperience() {
        return this.experience;
    }

    public long getProcessedFluidAmount() {
        if (this.fluidIngredient.isPresent()) {
            FluidIngredientWithAmount ingredient = this.fluidIngredient.get();
            return FluidUnit.convert(ingredient.amount(), ingredient.unit().orElse(getUnit()), FluidUnit.MILLIBUCKET);
        }
        if (this.result.left().isPresent()) {
            AbstractedFluidStack output = this.result.left().get();
            return FluidUnit.convert(output.amount(), output.unit(), FluidUnit.MILLIBUCKET);
        }
        return 0L;
    }

    public int getFermentTime() {
        return getFermentTime(1);
    }

    public int getFermentTime(int batches) {
        long processed = getProcessedFluidAmount();
        if (processed <= 0L)
            return FERMENTING_TIME_PER_BUCKET * batches;
        return (int) (FERMENTING_TIME_PER_BUCKET * processed * batches / BUCKET);
    }

    public int getTemperature() {
        return this.temperature;
    }

    @Override
    public boolean matches(KegRecipeWrapper inv, Level level) {
        List<ItemStack> inputs = new ArrayList<>();

        for (int j = 0; j < INPUT_SLOTS; ++j) {
            ItemStack itemstack = inv.getItem(j);
            if (!itemstack.isEmpty()) {
                inputs.add(itemstack);
            } else
                inputs.add(ItemStack.EMPTY);
        }
        CraftingInput input = CraftingInput.of(2, 2, inputs);
        return input.size() == 1 && inputItems.size() == 1 ? inputItems.getFirst().test(input.getItem(0)) : input.stackedContents().canCraft(this, null) &&
                (fluidIngredient.isEmpty() && inv.getFluid().isEmpty() || fluidIngredient.isPresent() && !inv.getFluid().isEmpty() && fluidIngredient.get().ingredient().matches(inv.getFluid()) && FluidUnit.convertToLoader(inv.getFluid().amount(), inv.getFluid().unit()) % getLoaderFluidAmount() == 0);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.inputItems.size();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        if (result.right().isPresent())
            return result.right().get().copy();
        if (result.left().isPresent())
            return BnCRecipeUtils.getPouredItemFromFluid(new AbstractedFluidStack(result.left().get().fluid(), KegBlockEntity.localizedCapacity(), result.left().get().components(), FluidUnit.MILLIBUCKET, null));
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BnCRecipeSerializers.FERMENTING;
    }

    @Override
    public RecipeType<?> getType() {
        return BnCRecipeTypes.FERMENTING;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(BnCItems.KEG);
    }

    @Override
    public boolean isIncomplete() {
        NonNullList<Ingredient> nonnulllist = getIngredients();
        return nonnulllist.isEmpty() || nonnulllist.stream().allMatch(Ingredient::isEmpty);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KegFermentingRecipe that = (KegFermentingRecipe) o;

        if (Float.compare(that.getExperience(), getExperience()) != 0) return false;
        if (getTemperature() != that.getTemperature()) return false;
        if (getResult() != (that.getResult())) return false;
        if (getFluidIngredient() != (that.getFluidIngredient())) return false;

        return inputItems.equals(that.inputItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputItems, fluidIngredient, result, experience, temperature);
    }

    public static class Serializer implements RecipeSerializer<KegFermentingRecipe> {
        public static final MapCodec<KegFermentingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.listOf(1, 4).xmap(ingredients -> NonNullList.of(Ingredient.EMPTY, ingredients.toArray(Ingredient[]::new)), Function.identity()).fieldOf("ingredients").forGetter(KegFermentingRecipe::getIngredients),
                FermentingBookCategory.CODEC.optionalFieldOf("category", FermentingBookCategory.DRINKS).forGetter(KegFermentingRecipe::getRecipeBookCategory),
                FluidIngredientWithAmount.CODEC.optionalFieldOf("base_fluid").forGetter(KegFermentingRecipe::getFluidIngredient),
                FluidUnit.CODEC.optionalFieldOf("unit").forGetter(KegFermentingRecipe::getRawUnit),
                Codec.either(AbstractedFluidStack.CODEC, ItemStack.CODEC).fieldOf("result").forGetter(KegFermentingRecipe::getResult),
                Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(KegFermentingRecipe::getExperience),
                Codec.INT.optionalFieldOf("temperature", 3).forGetter(KegFermentingRecipe::getTemperature)
        ).apply(inst, KegFermentingRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, KegFermentingRecipe> STREAM_CODEC = StreamCodec.of(KegFermentingRecipe.Serializer::toNetwork, KegFermentingRecipe.Serializer::fromNetwork);

        public Serializer() {
        }

        public MapCodec<KegFermentingRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, KegFermentingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static void toNetwork(RegistryFriendlyByteBuf buf, KegFermentingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(4)).encode(buf, recipe.getIngredients());
            FermentingBookCategory.STREAM_CODEC.encode(buf, recipe.getRecipeBookCategory());
            ByteBufCodecs.optional(FluidIngredientWithAmount.STREAM_CODEC).encode(buf, recipe.getFluidIngredient());
            ByteBufCodecs.optional(FluidUnit.STREAM_CODEC).encode(buf, recipe.getRawUnit());
            ByteBufCodecs.either(AbstractedFluidStack.STREAM_CODEC, ItemStack.STREAM_CODEC).encode(buf, recipe.getResult());
            buf.writeFloat(recipe.getExperience());
            buf.writeInt(recipe.getTemperature());
        }

        public static KegFermentingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            NonNullList<Ingredient> ingredients = NonNullList.of(Ingredient.EMPTY, Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(4)).decode(buf).toArray(Ingredient[]::new));
            FermentingBookCategory category = FermentingBookCategory.STREAM_CODEC.decode(buf);
            Optional<FluidIngredientWithAmount> fluidIngredient = ByteBufCodecs.optional(FluidIngredientWithAmount.STREAM_CODEC).decode(buf);
            Optional<FluidUnit> fluidUnit = ByteBufCodecs.optional(FluidUnit.STREAM_CODEC).decode(buf);
            Either<AbstractedFluidStack, ItemStack> result = ByteBufCodecs.either(AbstractedFluidStack.STREAM_CODEC, ItemStack.STREAM_CODEC).decode(buf);
            float experience = buf.readFloat();
            int temperature = buf.readInt();

            return new KegFermentingRecipe(ingredients, category, fluidIngredient, fluidUnit, result, experience, temperature);
        }
    }
}