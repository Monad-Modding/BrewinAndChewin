package umpaz.brewinandchewin.data;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.internal.NeoForgeAdvancementProvider;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.loot.condition.AreaLocationCheckCondition;
import umpaz.brewinandchewin.common.loot.condition.NullTrueBlockStateCondition;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.tag.BnCTags;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

public class BnCAdvancements implements NeoForgeAdvancementProvider.AdvancementGenerator {
    // Make sure to exclude compatibility items such as Kombucha.
    private static final Item[] DRINKS = new Item[]{
            BnCItems.BEER,
            BnCItems.VODKA,
            BnCItems.MEAD,
            BnCItems.EGG_GROG,
            BnCItems.STRONGROOT_ALE,
            BnCItems.SACCHARINE_RUM,
            BnCItems.PALE_JANE,
            BnCItems.SALTY_FOLLY,
            BnCItems.STEEL_TOE_STOUT,
            BnCItems.GLITTERING_GRENADINE,
            BnCItems.BLOODY_MARY,
            BnCItems.RED_RUM,
            BnCItems.WITHERING_DROSS,
            BnCItems.DREAD_NOG
    };
    private static final Item[] MEALS = new Item[]{
            BnCItems.KIMCHI,
            BnCItems.JERKY,
            BnCItems.PICKLED_PICKLES,
            BnCItems.KIPPERS,
            BnCItems.COCOA_FUDGE,
            BnCItems.VEGETABLE_OMELET,
            BnCItems.CHEESY_PASTA,
            BnCItems.SCARLET_PIEROGI,
            BnCItems.HORROR_LASAGNA,
            BnCItems.PIZZA_SLICE,
            BnCItems.FIERY_FONDUE,
            BnCItems.HAM_AND_CHEESE_SANDWICH,
            BnCItems.SWEET_BERRY_JAM,
            BnCItems.GLOW_BERRY_MARMALADE,
            BnCItems.APPLE_JELLY
    };

    public BnCAdvancements() {
    }

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
        AdvancementHolder brewinAndChewin = Advancement.Builder.advancement().
                display(BnCItems.BEER, Component.translatable("brewinandchewin.advancement.root"), Component.translatable("brewinandchewin.advancement.root.desc"), ResourceLocation.withDefaultNamespace("textures/block/spruce_planks.png"), AdvancementType.TASK, false, false, false).addCriterion("beer", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[0]))
                .save(saver, BrewinAndChewin.asResource("main/root").toString());
        AdvancementHolder placeKeg = getAdvancement(brewinAndChewin, BnCItems.KEG, Component.translatable("brewinandchewin.advancement.place_keg"), Component.translatable("brewinandchewin.advancement.place_keg.desc"), AdvancementType.TASK, true, true, false)
                .addCriterion("placed_keg", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(BnCBlocks.KEG))
                .save(saver, BrewinAndChewin.asResource("main/place_keg").toString());
        AdvancementHolder placeTemperatureBlockNearKeg = getAdvancement(placeKeg, BnCItems.ICE_CRATE, Component.translatable("brewinandchewin.advancement.place_temperature_block_near_keg"), Component.translatable("brewinandchewin.advancement.place_temperature_block_near_keg.desc"), AdvancementType.TASK, true, true, false)
                .addCriterion("placed_heat_source_near_keg", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(ModTags.Blocks.HEAT_SOURCES))), NullTrueBlockStateCondition.checkState(NullTrueBlockStateCondition.PropertyMatcher.exact("lit", "true")), AreaLocationCheckCondition.checkArea(KegBlockEntity.RANGE, LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BnCBlocks.KEG))))))
                .addCriterion("placed_freeze_source_near_keg", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BnCTags.Blocks.FREEZE_SOURCES))), NullTrueBlockStateCondition.checkState(NullTrueBlockStateCondition.PropertyMatcher.exact("lit", "true")), AreaLocationCheckCondition.checkArea(KegBlockEntity.RANGE, LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BnCBlocks.KEG))))))
                .addCriterion("updated_heat_source_near_keg", itemUsedOnBlock(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(ModTags.Blocks.HEAT_SOURCES))), NullTrueBlockStateCondition.checkState(NullTrueBlockStateCondition.PropertyMatcher.exact("lit", "true")), AreaLocationCheckCondition.checkArea(KegBlockEntity.RANGE, LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BnCBlocks.KEG))))))
                .addCriterion("updated_freeze_source_near_keg", itemUsedOnBlock(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BnCTags.Blocks.FREEZE_SOURCES))), NullTrueBlockStateCondition.checkState(NullTrueBlockStateCondition.PropertyMatcher.exact("lit", "true")), AreaLocationCheckCondition.checkArea(KegBlockEntity.RANGE, LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BnCBlocks.KEG))))))
                .addCriterion("placed_keg_near_source", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BnCBlocks.KEG))), AreaLocationCheckCondition.checkArea(KegBlockEntity.RANGE, AnyOfCondition.anyOf(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(ModTags.Blocks.HEAT_SOURCES))), LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BnCTags.Blocks.FREEZE_SOURCES)))), NullTrueBlockStateCondition.checkState(NullTrueBlockStateCondition.PropertyMatcher.exact("lit", "true")))))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(saver, BrewinAndChewin.asResource("main/place_temperature_block_near_keg").toString());
        AdvancementHolder brewDrink = getAdvancement(placeKeg, BnCItems.VODKA, Component.translatable("brewinandchewin.advancement.brew_drink"), Component.translatable("brewinandchewin.advancement.brew_drink.desc"), AdvancementType.TASK, true, true, false)
                .addCriterion("has_drink", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(BnCTags.Items.FERMENTED_DRINKS).build()))
                .save(saver, BrewinAndChewin.asResource("main/brew_drink").toString());
        AdvancementHolder craftingProblem = getCraftingProblemAdvancement(getAdvancement(brewDrink, BnCItems.STEEL_TOE_STOUT, Component.translatable("brewinandchewin.advancement.crafting_problem"), Component.translatable("brewinandchewin.advancement.crafting_problem.desc"), AdvancementType.CHALLENGE, true, true, false))
                .rewards(AdvancementRewards.Builder.experience(100))
                .save(saver, BrewinAndChewin.asResource("main/crafting_problem").toString());
        AdvancementHolder fermentCheese = getAdvancement(placeKeg, BnCItems.UNRIPE_FLAXEN_CHEESE_WHEEL, Component.translatable("brewinandchewin.advancement.ferment_cheese"), Component.translatable("brewinandchewin.advancement.ferment_cheese.desc"), AdvancementType.TASK, true, true, false)
                .addCriterion("has_unripe_flaxen_cheese_wheel", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.UNRIPE_FLAXEN_CHEESE_WHEEL))
                .addCriterion("has_unripe_scarlet_cheese_wheel", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.UNRIPE_SCARLET_CHEESE_WHEEL))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(saver, BrewinAndChewin.asResource("main/ferment_cheese").toString());
        AdvancementHolder cookFieryFondue = getAdvancement(fermentCheese, BnCItems.FIERY_FONDUE_POT, Component.translatable("brewinandchewin.advancement.cook_fiery_fondue"), Component.translatable("brewinandchewin.advancement.cook_fiery_fondue.desc"), AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("has_fiery_fondue_pot", InventoryChangeTrigger.TriggerInstance.hasItems(BnCItems.FIERY_FONDUE_POT))
                .requirements(AdvancementRequirements.Strategy.OR)
                .rewards(AdvancementRewards.Builder.experience(50))
                .save(saver, BrewinAndChewin.asResource("main/cook_fiery_fondue").toString());
        AdvancementHolder chefOfTheAges = getChefOfTheAgesAdvancement(getAdvancement(cookFieryFondue, BnCItems.PIZZA, Component.translatable("brewinandchewin.advancement.chef_of_the_ages"), Component.translatable("brewinandchewin.advancement.chef_of_the_ages.desc"), AdvancementType.CHALLENGE,true, true, false))
                .rewards(AdvancementRewards.Builder.experience(100))
                .save(saver, BrewinAndChewin.asResource("main/chef_of_the_ages").toString());
    }

    protected static Advancement.Builder getAdvancement(AdvancementHolder parent, ItemLike item, Component name, Component description, AdvancementType frameType, boolean showToast, boolean announceToChat, boolean hidden) {
        return Advancement.Builder.advancement().parent(parent).display(item.asItem().getDefaultInstance(), name, description, null, frameType, showToast, announceToChat, hidden);
    }

    protected static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> itemUsedOnBlock(LootItemCondition.Builder... pConditions) {
        ContextAwarePredicate predicate = ContextAwarePredicate.create(Arrays.stream(pConditions).map(LootItemCondition.Builder::build).toArray(LootItemCondition[]::new));
        return CriteriaTriggers.ITEM_USED_ON_BLOCK.createCriterion(new ItemUsedOnLocationTrigger.TriggerInstance(Optional.empty(), Optional.of(predicate)));
    }

    protected static Advancement.Builder getCraftingProblemAdvancement(Advancement.Builder builder) {
        for (Item drink : DRINKS) {
            builder.addCriterion(drink.builtInRegistryHolder().key().location().toString(), ConsumeItemTrigger.TriggerInstance.usedItem(drink));
        }
        return builder;
    }

    protected static Advancement.Builder getChefOfTheAgesAdvancement(Advancement.Builder builder) {
        for (Item meal : MEALS) {
            builder.addCriterion(meal.builtInRegistryHolder().key().location().toString(), ConsumeItemTrigger.TriggerInstance.usedItem(meal));
        }
        return builder;
    }
}
