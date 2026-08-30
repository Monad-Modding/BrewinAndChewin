package umpaz.brewinandchewin.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import net.minecraft.resources.ResourceLocation;
import umpaz.brewinandchewin.common.compat.nomansland.NMLIntegration;
import umpaz.brewinandchewin.common.tag.BnCTags;
import vectorwing.farmersdelight.common.tag.ModTags;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class BnCBlockTags extends BlockTagsProvider
{
    public BnCBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, BrewinAndChewin.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.registerModTags();
        this.registerBlockMineables();
        this.registerWildGrapeSupports();
        this.registerFarmersDelightTags();
    }

    protected void registerFarmersDelightTags() {
        tag(ModTags.Blocks.PIES).add(BnCBlocks.GLOW_BERRY_MERINGUE_PIE);
        tag(BlockTags.CROPS).add(BnCBlocks.CORN_CROP);
        tag(BlockTags.MAINTAINS_FARMLAND).add(BnCBlocks.CORN_CROP);
    }

    protected void registerWildGrapeSupports() {
        tag(BnCTags.Blocks.WILD_GRAPES_GROW_ON)
                .addTag(BlockTags.SPRUCE_LOGS)
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath(NMLIntegration.MOD_ID, "coniferous_logs"));
    }

    protected void registerBlockMineables() {
        tag(BlockTags.MINEABLE_WITH_AXE).add(
                BnCBlocks.TRELLIS,
                BnCBlocks.TRELLIS_GRAPE,
                BnCBlocks.KEG,
                BnCBlocks.HEATING_CASK,
                BnCBlocks.ICE_CRATE,
                BnCBlocks.COASTER,
                BnCBlocks.AGING_CASK,
                BnCBlocks.BOTTLE_RACK
        );
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                BnCBlocks.FIERY_FONDUE_POT
        );
        tag(ModTags.Blocks.MINEABLE_WITH_KNIFE).add(
                BnCBlocks.COASTER,
                BnCBlocks.RICH_CHOCOLATE_CAKE,
                BnCBlocks.PUMPKIN_ROLL,
                BnCBlocks.GLOW_BERRY_MERINGUE_PIE,
                BnCBlocks.UNRIPE_FLAXEN_CHEESE_WHEEL,
                BnCBlocks.FLAXEN_CHEESE_WHEEL,
                BnCBlocks.UNRIPE_SCARLET_CHEESE_WHEEL,
                BnCBlocks.SCARLET_CHEESE_WHEEL,
                BnCBlocks.PIZZA,
                BnCBlocks.QUICHE
        );
    }

    protected void registerModTags() {
        tag(BnCTags.Blocks.CHEESE_WHEELS_UNRIPE)
                .add(BnCBlocks.UNRIPE_FLAXEN_CHEESE_WHEEL)
                .add(BnCBlocks.UNRIPE_SCARLET_CHEESE_WHEEL);
        tag(BnCTags.Blocks.CHEESE_WHEELS_RIPE)
                .add(BnCBlocks.FLAXEN_CHEESE_WHEEL)
                .add(BnCBlocks.SCARLET_CHEESE_WHEEL);

        tag(BnCTags.Blocks.FREEZE_SOURCES).add(
                BnCBlocks.ICE_CRATE,
                Blocks.ICE,
                Blocks.PACKED_ICE,
                Blocks.BLUE_ICE
        );

        tag(BnCTags.Blocks.PLAYER_WORKSTATIONS_KEGS)
                .add(BnCBlocks.KEG);

        tag(ModTags.Blocks.HEAT_SOURCES).add(
                BnCBlocks.FIERY_FONDUE_POT,
                BnCBlocks.HEATING_CASK
        );
    }
}
