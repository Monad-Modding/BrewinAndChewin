package umpaz.brewinandchewin.data.builder;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import umpaz.brewinandchewin.BrewinAndChewin;
import vectorwing.farmersdelight.client.recipebook.CookingPotRecipeBookTab;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import javax.annotation.Nullable;

public class BnCCookingPotRecipeBuilder{
    private CookingPotRecipeBookTab tab;
    private final NonNullList<Ingredient> ingredients = NonNullList.create();
    private final ItemStack result;
    private final int cookingTime;
    private final float experience;
    private final ItemStack container;
    private final Advancement.Builder advancement = Advancement.Builder.advancement();

    private BnCCookingPotRecipeBuilder(ItemLike resultIn, int count, int cookingTime, float experience, @Nullable ItemLike container) {
        this.result = new ItemStack(resultIn, count);
        this.cookingTime = cookingTime;
        this.experience = experience;
        this.container = container != null ? new ItemStack(container) : ItemStack.EMPTY;
        this.tab = null;
    }

    public static BnCCookingPotRecipeBuilder cookingPotRecipe(ItemLike mainResult, int count, int cookingTime, float experience) {
        return new BnCCookingPotRecipeBuilder(mainResult, count, cookingTime, experience, null);
    }

    public static BnCCookingPotRecipeBuilder cookingPotRecipe(ItemLike mainResult, int count, int cookingTime, float experience, ItemLike container) {
        return new BnCCookingPotRecipeBuilder(mainResult, count, cookingTime, experience, container);
    }

    public BnCCookingPotRecipeBuilder addIngredient(TagKey<Item> tagIn) {
        return addIngredient(Ingredient.of(tagIn));
    }

    public BnCCookingPotRecipeBuilder addIngredient(ItemLike itemIn) {
        return addIngredient(itemIn, 1);
    }

    public BnCCookingPotRecipeBuilder addIngredient(ItemLike itemIn, int quantity) {
        for (int i = 0; i < quantity; ++i) {
            addIngredient(Ingredient.of(itemIn));
        }
        return this;
    }

    public BnCCookingPotRecipeBuilder addIngredient(Ingredient ingredientIn) {
        return addIngredient(ingredientIn, 1);
    }

    public BnCCookingPotRecipeBuilder addIngredient(Ingredient ingredientIn, int quantity) {
        for (int i = 0; i < quantity; ++i) {
            ingredients.add(ingredientIn);
        }
        return this;
    }

    public BnCCookingPotRecipeBuilder unlockedBy(String criterionName, Criterion<?> criterionTrigger) {
        advancement.addCriterion(criterionName, criterionTrigger);
        return this;
    }

    public BnCCookingPotRecipeBuilder unlockedByItems(String criterionName, ItemLike... items) {
        return unlockedBy(criterionName, InventoryChangeTrigger.TriggerInstance.hasItems(items));
    }

    public BnCCookingPotRecipeBuilder unlockedByAnyIngredient(ItemLike... items) {
        advancement.addCriterion("has_any_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items).build()));
        return this;
    }

    public BnCCookingPotRecipeBuilder setRecipeBookTab(CookingPotRecipeBookTab tab) {
        this.tab = tab;
        return this;
    }

    public void build(RecipeOutput output) {
        ResourceLocation location = BuiltInRegistries.ITEM.getKey(result.getItem());
        build(output, BrewinAndChewin.MODID + ":cooking/" + location.getPath());
    }

    public void build(RecipeOutput output, String save) {
        ResourceLocation location = BuiltInRegistries.ITEM.getKey(result.getItem());
        if (location.equals(ResourceLocation.tryParse(save))) {
            throw new IllegalStateException("Cooking Recipe " + save + " should remove its 'save' argument");
        } else {
            build(output, ResourceLocation.tryParse(save));
        }
    }

    public void build(RecipeOutput output, ResourceLocation id) {
        ResourceLocation advancementId = id.withPath(path -> "recipes/" + path);
        AdvancementHolder builtAdvancement = advancement.build(advancementId);
        if (!builtAdvancement.value().criteria().isEmpty()) {
            advancement.parent(ResourceLocation.withDefaultNamespace("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                    .rewards(AdvancementRewards.Builder.recipe(id))
                    .requirements(AdvancementRequirements.Strategy.OR);
            advancement.rewards(AdvancementRewards.Builder.recipe(id));
            builtAdvancement = advancement.build(advancementId);
        } else
            builtAdvancement = null;
        output.accept(id, new CookingPotRecipe("", tab, ingredients, result, container,  experience, cookingTime), builtAdvancement);
    }

}
