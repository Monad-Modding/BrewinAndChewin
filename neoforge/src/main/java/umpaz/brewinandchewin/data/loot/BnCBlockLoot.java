package umpaz.brewinandchewin.data.loot;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import umpaz.brewinandchewin.common.block.CoasterBlock;
import umpaz.brewinandchewin.common.block.DistilleryBlock;
import umpaz.brewinandchewin.common.block.DistilleryPart;
import umpaz.brewinandchewin.common.block.GrapeVineBlock;
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
        dropSelf(BnCBlocks.HEATING_CASK);
        dropSelf(BnCBlocks.ICE_CRATE);
        dropSelf(BnCBlocks.UNRIPE_FLAXEN_CHEESE_WHEEL);
        dropSelf(BnCBlocks.UNRIPE_SCARLET_CHEESE_WHEEL);
        add(BnCBlocks.COASTER, (block) -> LootTable.lootTable().withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CoasterBlock.INVISIBLE, false))))));

        dropOther(BnCBlocks.FIERY_FONDUE_POT, Blocks.CAULDRON);

        dropSelf(BnCBlocks.AGING_CASK);
        dropSelf(BnCBlocks.BOTTLE_RACK);
        add(BnCBlocks.DISTILLERY, (block) -> LootTable.lootTable().withPool(this.applyExplosionCondition(block,
                LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(BnCItems.DISTILLERY))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DistilleryBlock.PART, DistilleryPart.BURNER))))));
        add(BnCBlocks.RED_GRAPE_VINE, (block) -> this.createGrapeVineDrops(block, BnCItems.RED_GRAPES, BnCItems.RED_GRAPE_SEEDS));
        add(BnCBlocks.WHITE_GRAPE_VINE, (block) -> this.createGrapeVineDrops(block, BnCItems.WHITE_GRAPES, BnCItems.WHITE_GRAPE_SEEDS));
    }

    private LootTable.Builder createGrapeVineDrops(Block block, Item grapes, Item seeds) {
        return LootTable.lootTable()
                .withPool(this.applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(seeds))))
                .withPool(this.applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(grapes).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(GrapeVineBlock.AGE, GrapeVineBlock.MAX_AGE)))));
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
