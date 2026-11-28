package umpaz.brewinandchewin.common.crafting.display;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.crafting.FluidIngredientWithAmount;

import java.util.List;
import java.util.Optional;

public record KegFermentingRecipeDisplay(
        List<SlotDisplay> ingredients,
        SlotDisplay result,
        SlotDisplay craftingStation,
        int temperature,
        float experience,
        Optional<FluidIngredientWithAmount> fluidIngredient
) implements RecipeDisplay {
    public static final MapCodec<KegFermentingRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SlotDisplay.CODEC.listOf().fieldOf("ingredients").forGetter(KegFermentingRecipeDisplay::ingredients),
            SlotDisplay.CODEC.fieldOf("result").forGetter(KegFermentingRecipeDisplay::result),
            SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(KegFermentingRecipeDisplay::craftingStation),
            Codec.INT.fieldOf("temperature").forGetter(KegFermentingRecipeDisplay::temperature),
            Codec.FLOAT.fieldOf("experience").forGetter(KegFermentingRecipeDisplay::experience),
            FluidIngredientWithAmount.CODEC.optionalFieldOf("base_fluid").forGetter(KegFermentingRecipeDisplay::fluidIngredient)
    ).apply(inst, KegFermentingRecipeDisplay::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, KegFermentingRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
            SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), KegFermentingRecipeDisplay::ingredients,
            SlotDisplay.STREAM_CODEC, KegFermentingRecipeDisplay::result,
            SlotDisplay.STREAM_CODEC, KegFermentingRecipeDisplay::craftingStation,
            ByteBufCodecs.INT, KegFermentingRecipeDisplay::temperature,
            ByteBufCodecs.FLOAT, KegFermentingRecipeDisplay::experience,
            ByteBufCodecs.optional(FluidIngredientWithAmount.STREAM_CODEC), KegFermentingRecipeDisplay::fluidIngredient,
            KegFermentingRecipeDisplay::new
    );

    public static final RecipeDisplay.Type<KegFermentingRecipeDisplay> TYPE = new RecipeDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

    public static void registerAll() {
        Registry.register(BuiltInRegistries.RECIPE_DISPLAY, BrewinAndChewin.asResource("keg_fermenting"), TYPE);
    }

    @Override
    public RecipeDisplay.Type<KegFermentingRecipeDisplay> type() {
        return TYPE;
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return RecipeDisplay.super.isEnabled(enabledFeatures) && ingredients.stream().allMatch(slot -> slot.isEnabled(enabledFeatures));
    }
}
