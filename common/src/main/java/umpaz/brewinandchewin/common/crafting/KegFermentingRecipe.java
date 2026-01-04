package umpaz.brewinandchewin.common.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import umpaz.brewinandchewin.client.recipebook.FermentingBookCategory;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.BnCRecipeUtils;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.common.utility.KegContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class KegFermentingRecipe implements Recipe<KegContainer> {
    public static final int INPUT_SLOTS = 4;

    private final NonNullList<Ingredient> inputItems;

    private final Optional<FluidIngredientWithAmount> fluidIngredient;
    private final Optional<FluidUnit> unit;

    private final FermentingBookCategory tab;

    private final Either<AbstractedFluidStack, ItemStack> result;

    private final float experience;
    private final int fermentTime;
    private final int temperature;

    public KegFermentingRecipe(NonNullList<Ingredient> inputItems, FermentingBookCategory tab, Optional<FluidIngredientWithAmount> fluidIngredient, Optional<FluidUnit> unit, Either<AbstractedFluidStack, ItemStack> result, float experience, int fermentTime, int temperature) {
        this.inputItems = inputItems;
        this.tab = tab;
        this.fluidIngredient = fluidIngredient;
        this.unit = unit;
        if (unit.isPresent() && result.left().isPresent())
            this.result = Either.left(new AbstractedFluidStack(result.left().get().fluid(), result.left().get().amount(), unit.get(), result.left().get().loaderSpecific()));
        else
            this.result = result;
        this.experience = experience;
        this.fermentTime = fermentTime;
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

    public ItemStack assemble(KegContainer inv, RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    public float getExperience() {
        return this.experience;
    }

    public int getFermentTime() {
        return this.fermentTime;
    }

    public int getTemperature() {
        return this.temperature;
    }

    public boolean matches(KegContainer container, Level level) {
        List<ItemStack> inputs = new ArrayList<>();

        for (int i = 0; i < INPUT_SLOTS; ++i) {
            ItemStack itemstack = container.getItem(i);
            if (!itemstack.isEmpty()) {
                inputs.add(itemstack);
            } else
                inputs.add(ItemStack.EMPTY);
        }

        if (inputItems.size() == 1) {
            Ingredient ingredient = inputItems.get(0);
            return ingredient.test(inputs.get(0)) && fluidMatches(container);
        }

        // For multiple-item recipes, manually check each ingredient
        if (inputItems.size() == inputs.size()) {
            for (int i = 0; i < inputs.size(); i++) {
                if (!inputItems.get(i).test(inputs.get(i))) return false;
            }
            return fluidMatches(container);
        }
        return false;
    }

    private boolean fluidMatches(KegContainer container) {
        if (fluidIngredient.isEmpty() && container.getFluid().isEmpty()) return true;
        if (fluidIngredient.isPresent() && !container.getFluid().isEmpty()) {
            var fluid = container.getFluid();
            var ingredient = fluidIngredient.get();
            return ingredient.ingredient().matches(fluid) && fluid.amount() % ingredient.amount() == 0;
        }
        return false;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.inputItems.size();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        if (result.right().isPresent())
            return result.right().get().copy();
        if (result.left().isPresent())
            return BnCRecipeUtils.getPouredItemFromFluid(new AbstractedFluidStack(result.left().get().fluid(), 81000L, FluidUnit.DROPLET, null));
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
    public ResourceLocation getId() {
        return null;
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
        if (getFermentTime() != that.getFermentTime()) return false;
        if (getTemperature() != that.getTemperature()) return false;
        if (getResult() != (that.getResult())) return false;
        if (getFluidIngredient() != (that.getFluidIngredient())) return false;

        return inputItems.equals(that.inputItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputItems, fluidIngredient, result, experience, fermentTime, temperature);
    }

    public static class Serializer implements RecipeSerializer<KegFermentingRecipe> {
        public Serializer() {
        }

        @Override
        public KegFermentingRecipe fromJson(ResourceLocation id, JsonObject json) {
            // parse ingredients
            // parse optional fields (category, fluidIngredient, etc.)
            // return new KegFermentingRecipe(...)

            JsonArray ingredientsArray = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientsArray.size(), Ingredient.EMPTY);
            for (int i = 0; i < ingredientsArray.size(); i++) {
                ingredients.set(i, Ingredient.fromJson(ingredientsArray.get(i)));
            }

            // Optional category
            FermentingBookCategory category = FermentingBookCategory.DRINKS;
            if (json.has("category")) {
                category = FermentingBookCategory.fromString(json.get("category").getAsString());
            }

            // Optional fluid ingredient
            Optional<FluidIngredientWithAmount> fluidIngredient = Optional.empty();
            if (json.has("base_fluid")) {
                fluidIngredient = Optional.of(FluidIngredientWithAmount.fromJson(json.get("base_fluid")));
            }

            // Optional unit
            Optional<FluidUnit> fluidUnit = Optional.empty();
            if (json.has("unit")) {
                fluidUnit = Optional.of(FluidUnit.fromJson(json.get("unit")));
            }

            // Result: either fluid stack or item stack
            Either<AbstractedFluidStack, ItemStack> result;
            if (json.has("result")) {
                JsonElement resultElement = json.get("result");
                if (resultElement.isJsonObject() && resultElement.getAsJsonObject().has("fluid")) {
                    result = Either.left(AbstractedFluidStack.fromJson(resultElement));
                } else {
                    result = Either.right(ShapedRecipe.itemStackFromJson(resultElement.getAsJsonObject()));
                }
            } else {
                result = Either.right(ItemStack.EMPTY);
            }

            // Optional fields
            float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
            int fermentTime = GsonHelper.getAsInt(json, "fermenting_time", 9600);
            int temperature = GsonHelper.getAsInt(json, "temperature", 3);

            return new KegFermentingRecipe(
                    ingredients, category, fluidIngredient, fluidUnit,
                    result, experience, fermentTime, temperature
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, KegFermentingRecipe recipe) {
            // Write ingredients
            buf.writeVarInt(recipe.getIngredients().size());
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buf);
            }

            // Category
            buf.writeUtf(recipe.getRecipeBookCategory().name());

            // Optional fluid ingredient
            buf.writeBoolean(recipe.getFluidIngredient().isPresent());
            recipe.getFluidIngredient().ifPresent(f -> f.toNetwork(buf));

            // Optional fluid unit
            buf.writeBoolean(recipe.getRawUnit().isPresent());
            recipe.getRawUnit().ifPresent(f -> f.toNetwork(buf));

            // Result
            if (recipe.getResult().left().isPresent()) {
                buf.writeBoolean(true);
                recipe.getResult().left().get().toNetwork(buf);
            } else {
                buf.writeBoolean(false);
                buf.writeItem(recipe.getResult().right().orElse(ItemStack.EMPTY));
            }

            // Experience, fermentTime, temperature
            buf.writeFloat(recipe.getExperience());
            buf.writeInt(recipe.getFermentTime());
            buf.writeInt(recipe.getTemperature());
        }

        @Override
        public KegFermentingRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf buf) {
            // Ingredients
            int size = buf.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
            for (int i = 0; i < size; i++) {
                ingredients.set(i, Ingredient.fromNetwork(buf));
            }

            // Category
            FermentingBookCategory category = FermentingBookCategory.fromString(buf.readUtf());

            // Optional fluid ingredient
            Optional<FluidIngredientWithAmount> fluidIngredient = buf.readBoolean() ? Optional.of(FluidIngredientWithAmount.fromNetwork(buf)) : Optional.empty();

            // Optional fluid unit
            Optional<FluidUnit> fluidUnit = buf.readBoolean() ? Optional.of(FluidUnit.fromNetwork(buf)) : Optional.empty();

            // Result
            Either<AbstractedFluidStack, ItemStack> result;
            if (buf.readBoolean()) {
                result = Either.left(AbstractedFluidStack.fromNetwork(buf));
            } else {
                result = Either.right(buf.readItem());
            }

            // Experience, fermentTime, temperature
            float experience = buf.readFloat();
            int fermentTime = buf.readInt();
            int temperature = buf.readInt();

            return new KegFermentingRecipe(
                    ingredients, category, fluidIngredient, fluidUnit,
                    result, experience, fermentTime, temperature
            );
        }
    }
}