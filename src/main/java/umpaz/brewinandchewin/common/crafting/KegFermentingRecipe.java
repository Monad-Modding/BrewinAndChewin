package umpaz.brewinandchewin.common.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.recipebook.FermentingRecipeBookTab;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.BnCRecipeUtils;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;
import vectorwing.farmersdelight.client.recipebook.CookingPotRecipeBookTab;

import javax.annotation.Nullable;
import java.util.*;

public class KegFermentingRecipe implements Recipe<KegRecipeWrapper> {
    public static final int INPUT_SLOTS = 4;

    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputItems;
    @Nullable
    private final FermentingRecipeBookTab tab;
    @Nullable
    private final FluidStack fluidIngredient;
    @Nullable
    private final ITag<Fluid> fluidIngredientTag;
    @Nullable
    private final Fluid resultFluid;
    @Nullable
    private final Item resultItem;

    private final float experience;
    private final int fermentTime;
    private final int temperature;

    private final int amount;

    public KegFermentingRecipe(ResourceLocation id, NonNullList<Ingredient> inputItems, FermentingRecipeBookTab tab, @Nullable FluidStack fluidIngredient, @Nullable ITag<Fluid> fluidIngredientTag, @Nullable Fluid resultFluid, @Nullable Item resultItem, int amount, float experience, int fermentTime, int temperature) {
        this.id = id;
        this.inputItems = inputItems;
        this.tab = tab;
        this.fluidIngredient = fluidIngredient;
        this.fluidIngredientTag = fluidIngredientTag;
        this.resultFluid = resultFluid;
        this.resultItem = resultItem;
        this.amount = amount;
        this.experience = experience;
        this.fermentTime = fermentTime;
        this.temperature = temperature;
    }

    @Nullable
    public FermentingRecipeBookTab getRecipeBookTab() {
        return this.tab;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.inputItems;
    }

    @Nullable
    public FluidStack getFluidIngredient() {
        return this.fluidIngredient;
    }

    @Nullable
    public ITag<Fluid> getFluidIngredientTag() {
        return this.fluidIngredientTag;
    }

    @Nullable
    public Fluid getResultFluid() {
        return this.resultFluid;
    }

    @Nullable
    public Item getResultItem() {
        return this.resultItem;
    }

    public int getAmount() {
        return this.amount;
    }

    @Override
    public ItemStack assemble(KegRecipeWrapper inv, RegistryAccess access) {
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

    @Override
    public boolean matches(KegRecipeWrapper inv, Level level) {
        List<ItemStack> inputs = new ArrayList<>();
        int i = 0;

        for (int j = 0; j < INPUT_SLOTS; ++j) {
            ItemStack itemstack = inv.getItem(j);
            if (!itemstack.isEmpty()) {
                ++i;
                inputs.add(itemstack);
            }
        }
        return i == this.inputItems.size() && RecipeMatcher.findMatches(inputs, this.inputItems) != null && (fluidIngredient == null && inv.getFluid(0).isEmpty() || fluidIngredient != null && !inv.getFluid(0).isEmpty() && inv.getFluid(0).isFluidEqual(fluidIngredient) && inv.getFluid(0).getAmount() % amount == 0);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.inputItems.size();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        if (resultItem != null)
            return resultItem.getDefaultInstance().copyWithCount(amount);
        if (resultFluid != null)
            return BnCRecipeUtils.getPouredItemFromFluid(new FluidStack(resultFluid, BnCConfiguration.KEG_CAPACITY.get()));
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BnCRecipeSerializers.FERMENTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return BnCRecipeTypes.FERMENTING.get();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(BnCItems.KEG.get());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KegFermentingRecipe that = (KegFermentingRecipe) o;

        if (Float.compare(that.getExperience(), getExperience()) != 0) return false;
        if (getFermentTime() != that.getFermentTime()) return false;
        if (getTemperature() != that.getTemperature()) return false;
        if (!getId().equals(that.getId())) return false;
        if (getResultFluid() != (that.getResultFluid())) return false;
        if (getResultItem() != (that.getResultItem())) return false;
        if (getFluidIngredient() != (that.getFluidIngredient())) return false;
        if (getFluidIngredientTag() != that.getFluidIngredientTag()) return false;
        if (getAmount() != (that.getAmount())) return false;

        return inputItems.equals(that.inputItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), inputItems, fluidIngredient, resultFluid, resultFluid, experience, fermentTime, temperature);
    }

    public static class Serializer implements RecipeSerializer<KegFermentingRecipe> {
        public Serializer() {
        }

        @Override
        public KegFermentingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            final NonNullList<Ingredient> inputItemsIn = readIngredients(GsonHelper.getAsJsonArray(json, "ingredients"));
            if (inputItemsIn.isEmpty()) {
                throw new JsonParseException("No ingredients for cooking recipe");
            } else if (inputItemsIn.size() > KegFermentingRecipe.INPUT_SLOTS) {
                throw new JsonParseException("Too many ingredients for cooking recipe! The max is " + KegFermentingRecipe.INPUT_SLOTS);
            } else {
                String tabKeyIn = GsonHelper.getAsString(json, "recipe_book_tab", null);
                FermentingRecipeBookTab tabIn = FermentingRecipeBookTab.findByName(tabKeyIn);
                if (tabKeyIn != null && tabIn == null) {
                    BrewinAndChewin.LOG.warn("Optional field 'recipe_book_tab' does not match any valid tab. If defined, must be one of the following: " + EnumSet.allOf(CookingPotRecipeBookTab.class));
                }

                FluidStack baseFluidStackIn = null;
                ITag<Fluid> baseFluidTagIn = null;
                if (json.has("basefluid")) {
                    JsonObject baseFluid = json.getAsJsonObject("basefluid");
                    String fluidString = GsonHelper.getAsString(baseFluid, "fluidString");

                    baseFluidStackIn = new FluidStack(
                            ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidString)),
                            GsonHelper.getAsInt(baseFluid, "count", 200)
                    );
                    if (fluidString.startsWith("#")) {
                        TagKey<Fluid> fluidTagKey = TagKey.create(ForgeRegistries.Keys.FLUIDS, new ResourceLocation(fluidString.substring(1)));
                        baseFluidTagIn = ForgeRegistries.FLUIDS.tags().getTag(fluidTagKey); // I love Lex Manos and his awesome code.
                    }
                }


                float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
                int fermentingTime = GsonHelper.getAsInt(json, "fermentingtime", 200);
                int temperature = GsonHelper.getAsInt(json, "temperature", 3);

                JsonObject result = GsonHelper.getAsJsonObject(json, "result");
                int count = GsonHelper.getAsInt(result, "count");

                Fluid resultFluid = null;
                if (result.has("fluid")) {
                    String fluidString = GsonHelper.getAsString(result, "fluid");
                    resultFluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidString));
                }

                Item resultItem = null;
                if (result.has("item")) {
                    resultItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(GsonHelper.getAsString(result, "item")));
                }

                return new KegFermentingRecipe(recipeId, inputItemsIn, tabIn, baseFluidStackIn, baseFluidTagIn, resultFluid, resultItem, count, experience, fermentingTime, temperature);
            }
        }

        private static NonNullList<Ingredient> readIngredients(JsonArray ingredientArray) {
            NonNullList<Ingredient> nonnulllist = NonNullList.create();

            for (int i = 0; i < ingredientArray.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(ingredientArray.get(i));
                if (!ingredient.isEmpty()) {
                    nonnulllist.add(ingredient);
                }
            }

            return nonnulllist;
        }

        @Nullable
        @Override
        public KegFermentingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int i = buffer.readVarInt();

            NonNullList<Ingredient> inputItemsIn = NonNullList.withSize(i, Ingredient.EMPTY);
            inputItemsIn.replaceAll(ignored -> Ingredient.fromNetwork(buffer));

            Optional<FermentingRecipeBookTab> tabIn = buffer.readOptional(friendlyByteBuf -> friendlyByteBuf.readEnum(FermentingRecipeBookTab.class));
            Optional<FluidStack> baseFluidStackIn = buffer.readOptional(FriendlyByteBuf::readFluidStack);
            Optional<ITag<Fluid>> fluidTagIn = buffer.readOptional(FriendlyByteBuf::readResourceLocation).map(resourceLocation -> ForgeRegistries.FLUIDS.tags().getTag(TagKey.create(ForgeRegistries.Keys.FLUIDS, resourceLocation)));
            Optional<Item> itemResult = buffer.readOptional(FriendlyByteBuf::readItem).map(ItemStack::getItem);

            Optional<Fluid> fluidResult = buffer.readOptional(FriendlyByteBuf::readFluidStack).map(FluidStack::getFluid);

            int amount = buffer.readInt();
            float experienceIn = buffer.readFloat();
            int fermentTimeIn = buffer.readVarInt();
            int temperatureIn = buffer.readVarInt();
            return new KegFermentingRecipe(recipeId, inputItemsIn, tabIn.orElse(null), baseFluidStackIn.orElse(null), fluidTagIn.orElse(null), fluidResult.orElse(null), itemResult.orElse(null), amount, experienceIn, fermentTimeIn, temperatureIn);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, KegFermentingRecipe recipe) {
            buffer.writeVarInt(recipe.inputItems.size());

            for (Ingredient ingredient : recipe.inputItems) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeOptional(Optional.ofNullable(recipe.tab), FriendlyByteBuf::writeEnum);
            buffer.writeOptional(Optional.ofNullable(recipe.fluidIngredient), FriendlyByteBuf::writeFluidStack);
            buffer.writeOptional(Optional.ofNullable(recipe.fluidIngredientTag).map(fluids -> fluids.getKey().location()),  FriendlyByteBuf::writeResourceLocation);
            buffer.writeOptional(Optional.ofNullable(recipe.resultItem.getDefaultInstance()), FriendlyByteBuf::writeItem);
            buffer.writeOptional(Optional.ofNullable(new FluidStack(recipe.resultFluid, 1)), FriendlyByteBuf::writeFluidStack);
            buffer.writeInt(recipe.amount);
            buffer.writeFloat(recipe.experience);
            buffer.writeVarInt(recipe.fermentTime);
            buffer.writeVarInt(recipe.temperature);
        }
    }
}