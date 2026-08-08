package umpaz.brewinandchewin.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;

public class DistillingRecipe implements Recipe<SingleRecipeInput> {
    public static final int DEFAULT_DISTILLING_TIME = 400;
    public static final int DEFAULT_WATER_COST = 1;

    private final Ingredient ingredient;
    private final ItemStack result;
    private final int distillingTime;
    private final int waterCost;
    private final float experience;

    public DistillingRecipe(Ingredient ingredient, ItemStack result, int distillingTime, int waterCost, float experience) {
        this.ingredient = ingredient;
        this.result = result;
        this.distillingTime = distillingTime;
        this.waterCost = waterCost;
        this.experience = experience;
    }

    public Ingredient getIngredient() {
        return this.ingredient;
    }

    public int getDistillingTime() {
        return this.distillingTime;
    }

    public int getWaterCost() {
        return this.waterCost;
    }

    public float getExperience() {
        return this.experience;
    }

    public ItemStack getRawResult() {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(this.ingredient);
        return ingredients;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(BnCItems.DISTILLERY);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BnCRecipeSerializers.DISTILLING;
    }

    @Override
    public RecipeType<?> getType() {
        return BnCRecipeTypes.DISTILLING;
    }

    public static class Serializer implements RecipeSerializer<DistillingRecipe> {
        public static final MapCodec<DistillingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(DistillingRecipe::getIngredient),
                ItemStack.CODEC.fieldOf("result").forGetter(DistillingRecipe::getRawResult),
                Codec.INT.optionalFieldOf("distilling_time", DEFAULT_DISTILLING_TIME).forGetter(DistillingRecipe::getDistillingTime),
                Codec.INT.optionalFieldOf("water_cost", DEFAULT_WATER_COST).forGetter(DistillingRecipe::getWaterCost),
                Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(DistillingRecipe::getExperience)
        ).apply(inst, DistillingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, DistillingRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<DistillingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DistillingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static void toNetwork(RegistryFriendlyByteBuf buf, DistillingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getIngredient());
            ItemStack.STREAM_CODEC.encode(buf, recipe.getRawResult());
            buf.writeVarInt(recipe.getDistillingTime());
            buf.writeVarInt(recipe.getWaterCost());
            buf.writeFloat(recipe.getExperience());
        }

        public static DistillingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            int distillingTime = buf.readVarInt();
            int waterCost = buf.readVarInt();
            float experience = buf.readFloat();
            return new DistillingRecipe(ingredient, result, distillingTime, waterCost, experience);
        }
    }
}
