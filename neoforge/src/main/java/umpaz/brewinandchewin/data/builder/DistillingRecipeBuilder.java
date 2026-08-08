package umpaz.brewinandchewin.data.builder;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.crafting.DistillingRecipe;

public class DistillingRecipeBuilder {
    private final Ingredient ingredient;
    private final ItemStack result;
    private final int distillingTime;
    private final int waterCost;
    private final float experience;

    private final Advancement.Builder advancement = Advancement.Builder.advancement();

    private DistillingRecipeBuilder(Ingredient ingredient, ItemStack result, int distillingTime, int waterCost, float experience) {
        this.ingredient = ingredient;
        this.result = result;
        this.distillingTime = distillingTime;
        this.waterCost = waterCost;
        this.experience = experience;
    }

    public static DistillingRecipeBuilder distilling(Ingredient ingredient, ItemLike result, int count, int distillingTime, int waterCost, float experience) {
        return new DistillingRecipeBuilder(ingredient, new ItemStack(result, count), distillingTime, waterCost, experience);
    }

    public static DistillingRecipeBuilder distilling(ItemLike ingredient, ItemLike result, int distillingTime, int waterCost, float experience) {
        return distilling(Ingredient.of(ingredient), result, 1, distillingTime, waterCost, experience);
    }

    public static DistillingRecipeBuilder distilling(TagKey<Item> ingredient, ItemLike result, int distillingTime, int waterCost, float experience) {
        return distilling(Ingredient.of(ingredient), result, 1, distillingTime, waterCost, experience);
    }

    public DistillingRecipeBuilder unlockedBy(String criterionName, Criterion<?> criterion) {
        this.advancement.addCriterion(criterionName, criterion);
        return this;
    }

    public DistillingRecipeBuilder unlockedByItems(String criterionName, ItemLike... items) {
        return this.unlockedBy(criterionName, InventoryChangeTrigger.TriggerInstance.hasItems(items));
    }

    public void build(RecipeOutput output) {
        ResourceLocation resultLocation = BuiltInRegistries.ITEM.getKey(this.result.getItem());
        this.build(output, BrewinAndChewin.asResource("distilling/" + resultLocation.getPath()));
    }

    public void build(RecipeOutput output, ResourceLocation id) {
        this.advancement
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        AdvancementHolder holder = this.advancement.build(id.withPrefix("recipes/distilling/"));
        output.accept(id, new DistillingRecipe(this.ingredient, this.result, this.distillingTime, this.waterCost, this.experience), holder);
    }
}
