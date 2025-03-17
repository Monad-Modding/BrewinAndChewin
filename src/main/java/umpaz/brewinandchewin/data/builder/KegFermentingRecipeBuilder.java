package umpaz.brewinandchewin.data.builder;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.recipebook.FermentingRecipeBookTab;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class KegFermentingRecipeBuilder {
    private final List<Ingredient> ingredients = Lists.newArrayList();

    private Optional<FluidStack> fluidIngredient = Optional.empty();

    private Optional<Fluid> resultFluid = Optional.empty();
    private Optional<Item> resultItem = Optional.empty();
    private Optional<FermentingRecipeBookTab> tab = Optional.empty();

    private final int fermentingTime;
    private final float experience;
    private final int temperature;
    private final int amount;

    private final Advancement.Builder advancement = Advancement.Builder.advancement();

    private KegFermentingRecipeBuilder(int amount, int fermentingTime, float experience, int temperature) {
        this.fermentingTime = fermentingTime;
        this.experience = experience;
        this.temperature = temperature;
        this.amount = amount;
    }

    public static KegFermentingRecipeBuilder kegFermentingRecipe(Item item, int amount, int fermentingTime, float experience, int temperature) {
        KegFermentingRecipeBuilder i = new KegFermentingRecipeBuilder(amount, fermentingTime, experience, temperature);
        i.setResult(item);
        return i;
    }

    public static KegFermentingRecipeBuilder kegFermentingRecipe(Fluid fluid, int amount, int fermentingTime, float experience, int temperature) {
        KegFermentingRecipeBuilder i = new KegFermentingRecipeBuilder(amount, fermentingTime, experience, temperature);
        i.setResult(fluid);
        return i;
    }

    public static KegFermentingRecipeBuilder kegFermentingRecipe(Item item, int amount, int fermentingTime, float experience) {
        KegFermentingRecipeBuilder i = new KegFermentingRecipeBuilder(amount, fermentingTime, experience, 3);
        i.setResult(item);
        return i;
    }

    public static KegFermentingRecipeBuilder kegFermentingRecipe(Fluid fluid, int amount, int fermentingTime, float experience) {
        KegFermentingRecipeBuilder i = new KegFermentingRecipeBuilder(amount, fermentingTime, experience, 3);
        i.setResult(fluid);
        return i;
    }

    private void setResult(Fluid fluid) {
        resultFluid = Optional.of(fluid);
    }

    private void setResult(Item item) {
        resultItem = Optional.of(item);
    }


    public KegFermentingRecipeBuilder addIngredient(TagKey<Item> tagIn) {
        return addIngredient(Ingredient.of(tagIn));
    }

    public KegFermentingRecipeBuilder addIngredient(ItemLike itemIn) {
        return addIngredient(itemIn, 1);
    }

    public KegFermentingRecipeBuilder addIngredient(ItemLike itemIn, int quantity) {
        for (int i = 0; i < quantity; ++i) {
            addIngredient(Ingredient.of(itemIn));
        }
        return this;
    }

    public KegFermentingRecipeBuilder addIngredient(Ingredient ingredientIn) {
        return addIngredient(ingredientIn, 1);
    }

    public KegFermentingRecipeBuilder addIngredient(Ingredient ingredientIn, int quantity) {
        for (int i = 0; i < quantity; ++i) {
            ingredients.add(ingredientIn);
        }
        return this;
    }

    public KegFermentingRecipeBuilder setRecipeBookTab(FermentingRecipeBookTab tab) {
        this.tab = Optional.of(tab);
        return this;
    }

    public KegFermentingRecipeBuilder unlockedBy(String criterionName, CriterionTriggerInstance criterionTrigger) {
        advancement.addCriterion(criterionName, criterionTrigger);
        return this;
    }

    public KegFermentingRecipeBuilder unlockedByItems(String criterionName, ItemLike... items) {
        return this.unlockedBy(criterionName, InventoryChangeTrigger.TriggerInstance.hasItems(items));
    }

    public KegFermentingRecipeBuilder unlockedByAnyIngredient(ItemLike... items) {
        this.advancement.addCriterion("has_any_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items).build()));
        return this;
    }

    public void build(Consumer<FinishedRecipe> consumerIn) {
        if (!resultFluid.isPresent() && !resultItem.isPresent()) {
            throw new IllegalStateException("No result fluid or item");
        }

        if (resultItem.isPresent()) {
            ResourceLocation resultItemLocation = ForgeRegistries.ITEMS.getKey(resultItem.get());
            build(consumerIn, BrewinAndChewin.MODID + ":fermenting/" + resultItemLocation.getPath());
        } else if (!fluidIngredient.isPresent()) {
            ResourceLocation resultFluidLocation = ForgeRegistries.FLUIDS.getKey(resultFluid.get());
            build(consumerIn, BrewinAndChewin.MODID + ":fermenting/" + resultFluidLocation.getPath());
        } else {
            ResourceLocation baseFluidLocation = ForgeRegistries.FLUIDS.getKey(fluidIngredient.get().getFluid());
            ResourceLocation resultFluidLocation = ForgeRegistries.FLUIDS.getKey(resultFluid.get());
            build(consumerIn, BrewinAndChewin.MODID + ":fermenting/" + resultFluidLocation.getPath() + "_from_" + baseFluidLocation.getPath());
        }
    }

    public void build(Consumer<FinishedRecipe> consumerIn, String save) {
        if (!resultFluid.isPresent() && !resultItem.isPresent()) {
            throw new IllegalStateException("No result fluid or item");
        }
        ResourceLocation resourcelocation = (resultItem.isPresent()) ? ForgeRegistries.ITEMS.getKey(resultItem.get()) : ForgeRegistries.FLUIDS.getKey(resultFluid.get());
        if ((new ResourceLocation(save)).equals(resourcelocation)) {
            throw new IllegalStateException("Fermenting Recipe " + save + " should remove its 'save' argument");
        } else {
            build(consumerIn, new ResourceLocation(save));
        }
    }

    public KegFermentingRecipeBuilder addFluidIngredient(Fluid flowingFluid, int i) {
        fluidIngredient = Optional.of(new FluidStack(flowingFluid, i));
        return this;
    }

    public void build(Consumer<FinishedRecipe> consumerIn, ResourceLocation id) {
        ResourceLocation advancementId = null;
        if (!advancement.getCriteria().isEmpty()) {
            advancement.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                    .rewards(AdvancementRewards.Builder.recipe(id))
                    .requirements(RequirementsStrategy.OR);
            advancementId = id.withPath(path -> "recipes/" + path);
        }
        consumerIn.accept(new KegFermentingRecipeBuilder.Result(id, fluidIngredient, tab, resultItem, resultFluid, amount, ingredients, fermentingTime, experience, temperature, advancement, advancementId));
    }


    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final List<Ingredient> ingredients;
        private final Optional<FermentingRecipeBookTab> tab;
        private final Optional<FluidStack> fluidIngredient;

        private final Optional<Item> resultItem;
        private final Optional<Fluid> resultFluid;
        private final int fermentingTime;
        private final float experience;
        private final int temperature;
        private final int count;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;


        public Result(ResourceLocation idIn, Optional<FluidStack> fluidIngredient, Optional<FermentingRecipeBookTab> tab, Optional<Item> resultItemIn, Optional<Fluid> resultFluidIn, int count, List<Ingredient> ingredientsIn, int fermentingTimeIn, float experienceIn, int temperatureIn, @Nullable Advancement.Builder advancement, @Nullable ResourceLocation advancementId) {
            this.id = idIn;
            this.fluidIngredient = fluidIngredient;
            //this.fluidIngredientTag = fluidIngredientTag;
            this.tab = tab;
            this.resultItem = resultItemIn;
            this.resultFluid = resultFluidIn;
            this.count = count;
            this.ingredients = ingredientsIn;
            this.fermentingTime = fermentingTimeIn;
            this.experience = experienceIn;
            this.temperature = temperatureIn;
            this.advancement = advancement;
            this.advancementId = advancementId;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            JsonArray arrayIngredients = new JsonArray();

            for (Ingredient ingredient : ingredients) {
                arrayIngredients.add(ingredient.toJson());
            }
            json.add("ingredients", arrayIngredients);

            JsonObject result = new JsonObject();
            if (resultItem.isPresent()) {
                result.addProperty("item", ForgeRegistries.ITEMS.getKey(resultItem.get()).toString());
            } else if (resultFluid.isPresent()){
                result.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(resultFluid.get()).toString());
            }
            result.addProperty("count", count);
            json.add("result", result);


            if (fluidIngredient.isPresent()) {
                JsonObject basefluid = new JsonObject();
                basefluid.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(fluidIngredient.get().getFluid()).toString());
                basefluid.addProperty("count", fluidIngredient.get().getAmount());
                json.add("basefluid", basefluid);
            }

            tab.ifPresent(t -> json.addProperty("recipe_book_tab", t.name));

            if (experience > 0) {
                json.addProperty("experience", experience);
            }
            json.addProperty("fermentingtime", fermentingTime);
            json.addProperty("temperature", temperature);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return BnCRecipeSerializers.FERMENTING.get();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return advancement != null ? advancement.serializeToJson() : null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return advancementId;
        }
    }
}