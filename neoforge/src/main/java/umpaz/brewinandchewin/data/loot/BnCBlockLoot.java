package umpaz.brewinandchewin.data.loot;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.neoforged.neoforge.common.Tags;
import umpaz.brewinandchewin.common.block.CornCropBlock;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.ModTags;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import umpaz.brewinandchewin.common.block.CoasterBlock;
import umpaz.brewinandchewin.common.block.GrapeBushBlock;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.loot.function.CopyDrinkFunction;

import java.util.HashSet;
import java.util.Set;

public class BnCBlockLoot extends BlockLootSubProvider {

    private final Set<Block> generatedLootTables = new HashSet<>();

    public BnCBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        add(BnCBlocks.KEG, (block) -> LootTable.lootTable().withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block)
                .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY)).apply(CopyDrinkFunction.builder())))));
        dropSelf(BnCBlocks.TRELLIS);
        add(BnCBlocks.TRELLIS_GRAPE, noDrop());
        dropSelf(BnCBlocks.HEATING_CASK);
        dropSelf(BnCBlocks.ICE_CRATE);
        dropSelf(BnCBlocks.UNRIPE_FLAXEN_CHEESE_WHEEL);
        dropSelf(BnCBlocks.UNRIPE_SCARLET_CHEESE_WHEEL);
        add(BnCBlocks.COASTER, (block) -> LootTable.lootTable().withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CoasterBlock.INVISIBLE, false))))));

        dropOther(BnCBlocks.FIERY_FONDUE_POT, Blocks.CAULDRON);

        dropSelf(BnCBlocks.AGING_CASK);
        dropSelf(BnCBlocks.BOTTLE_RACK);
        add(BnCBlocks.CORN_CROP, this::createCornCropDrops);
        add(BnCBlocks.WILD_CORN, this::createWildCornDrops);
        add(BnCBlocks.WILD_GRAPES, this::createWildGrapesDrops);
        add(BnCBlocks.RED_GRAPE_BUSH, (block) -> this.createGrapeBushDrops(block, BnCItems.RED_GRAPE_SEEDS));
        add(BnCBlocks.WHITE_GRAPE_BUSH, (block) -> this.createGrapeBushDrops(block, BnCItems.WHITE_GRAPE_SEEDS));
        add(BnCBlocks.RED_ROPE_GRAPE, noDrop());
        add(BnCBlocks.WHITE_ROPE_GRAPE, noDrop());
        add(BnCBlocks.RED_GRAPE_STEM, noDrop());
        add(BnCBlocks.WHITE_GRAPE_STEM, noDrop());
    }

    private static final int CORN_HARVESTABLE_AGE = 5;
    private static final float[] CORN_SECOND_DROP_CHANCE = {1.0F / 3.0F, 2.0F / 3.0F, 1.0F};

    private LootItemCondition.Builder cornAgeIs(Block block, int age) {
        return LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(CornCropBlock.AGE, age)
                        .hasProperty(CornCropBlock.SECTION, 0));
    }

    private static LootItemCondition.Builder shears() {
        return AnyOfCondition.anyOf(
                MatchTool.toolMatches(ItemPredicate.Builder.item().of(Tags.Items.TOOLS_SHEAR)),
                MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS)));
    }

    private LootTable.Builder createWildGrapesDrops(Block block) {
        return LootTable.lootTable()
                .withPool(this.applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(AlternativesEntry.alternatives(
                                LootItem.lootTableItem(block).when(shears()),
                                LootItem.lootTableItem(BnCItems.RED_GRAPES)))));
    }

    private LootTable.Builder createWildCornDrops(Block block) {
        LootItemCondition.Builder lowerHalf = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        return LootTable.lootTable()
                .withPool(this.applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(AlternativesEntry.alternatives(
                                LootItem.lootTableItem(block).when(shears()),
                                LootItem.lootTableItem(BnCItems.CORN)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))))
                        .when(lowerHalf)));
    }

    private LootTable.Builder createCornCropDrops(Block block) {
        AnyOfCondition.Builder harvestable = AnyOfCondition.anyOf();
        for (int age = CORN_HARVESTABLE_AGE; age <= CornCropBlock.MAX_AGE; ++age)
            harvestable = harvestable.or(cornAgeIs(block, age));

        LootPool.Builder secondCorn = LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F));
        for (int age = CORN_HARVESTABLE_AGE; age <= CornCropBlock.MAX_AGE; ++age) {
            float chance = CORN_SECOND_DROP_CHANCE[age - CORN_HARVESTABLE_AGE];
            LootPoolSingletonContainer.Builder<?> entry = LootItem.lootTableItem(BnCItems.CORN).when(cornAgeIs(block, age));
            if (chance < 1.0F)
                entry = entry.when(LootItemRandomChanceCondition.randomChance(chance));
            secondCorn = secondCorn.add(entry);
        }

        return LootTable.lootTable()
                .withPool(this.applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(BnCItems.CORN_KERNELS))
                        .when(cornAgeIs(block, 0))))
                .withPool(this.applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(BnCItems.CORN))
                        .when(harvestable)))
                .withPool(this.applyExplosionDecay(block, secondCorn))
                .withPool(this.applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModItems.STRAW.get()))
                        .when(cornAgeIs(block, CornCropBlock.MAX_AGE))
                        .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ModTags.Items.STRAW_HARVESTERS)))));
    }

    private LootTable.Builder createGrapeBushDrops(Block block, Item seeds) {
        return LootTable.lootTable()
                .withPool(this.applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(seeds))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(GrapeBushBlock.AGE, 0)))));
    }

    @Override
    protected void add(Block block, LootTable.Builder builder) {
        this.generatedLootTables.add(block);
        this.map.put(block.getLootTable(), builder);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return generatedLootTables;
    }
}
