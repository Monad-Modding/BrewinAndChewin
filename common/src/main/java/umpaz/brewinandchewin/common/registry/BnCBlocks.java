package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.*;
import vectorwing.farmersdelight.common.block.PieBlock;

public class BnCBlocks {
    public static final Block KEG = new KegBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));

    public static final Block HEATING_CASK = new HeatingCaskBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));

    public static final Block ICE_CRATE = new IceCrateBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));

    public static final Block COASTER = new CoasterBlock();

    public static final Block AGING_CASK = new AgingCaskBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion());

    public static final Block BOTTLE_RACK = new BottleRackBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.CHISELED_BOOKSHELF).noOcclusion());

    public static final Block DISTILLERY = new DistilleryBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.BLAST_FURNACE).noOcclusion()
                    .lightLevel(state -> state.getValue(DistilleryBlock.LIT) ? 13 : 0));

    public static final Block RED_GRAPE_VINE = new GrapeVineBlock(
            () -> BnCItems.RED_GRAPES, BlockBehaviour.Properties.ofFullCopy(Blocks.VINE).randomTicks());

    public static final Block WHITE_GRAPE_VINE = new GrapeVineBlock(
            () -> BnCItems.WHITE_GRAPES, BlockBehaviour.Properties.ofFullCopy(Blocks.VINE).randomTicks());

    //Cheese
    public static final Block UNRIPE_FLAXEN_CHEESE_WHEEL = new
            UnripeCheeseWheelBlock(() -> BnCBlocks.FLAXEN_CHEESE_WHEEL, Block.Properties.ofFullCopy(Blocks.CAKE));

    public static final Block FLAXEN_CHEESE_WHEEL = new
            CheeseWheelBlock(() -> BnCItems.FLAXEN_CHEESE_WEDGE, Block.Properties.ofFullCopy(Blocks.CAKE));

    public static final Block UNRIPE_SCARLET_CHEESE_WHEEL = new
            UnripeCheeseWheelBlock(() -> BnCBlocks.SCARLET_CHEESE_WHEEL, Block.Properties.ofFullCopy(Blocks.CAKE));

    public static final Block SCARLET_CHEESE_WHEEL = new
            CheeseWheelBlock(() -> BnCItems.SCARLET_CHEESE_WEDGE, Block.Properties.ofFullCopy(Blocks.CAKE));

    // Feasts
    public static final Block FIERY_FONDUE_POT = new
            FieryFonduePotBlock(Block.Properties.ofFullCopy(Blocks.CAULDRON));

    public static final Block PIZZA = new
            PizzaBlock(Block.Properties.ofFullCopy(Blocks.CAKE));

    public static final Block QUICHE = new
            PieBlock(Block.Properties.ofFullCopy(Blocks.CAKE), () -> BnCItems.QUICHE_SLICE);


    public static void registerAll() {
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("keg"), KEG);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("heating_cask"), HEATING_CASK);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("ice_crate"), ICE_CRATE);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("coaster"), COASTER);

        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("aging_cask"), AGING_CASK);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("bottle_rack"), BOTTLE_RACK);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("distillery"), DISTILLERY);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("red_grape_vine"), RED_GRAPE_VINE);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("white_grape_vine"), WHITE_GRAPE_VINE);

        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("unripe_flaxen_cheese_wheel"), UNRIPE_FLAXEN_CHEESE_WHEEL);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("flaxen_cheese_wheel"), FLAXEN_CHEESE_WHEEL);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("unripe_scarlet_cheese_wheel"), UNRIPE_SCARLET_CHEESE_WHEEL);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("scarlet_cheese_wheel"), SCARLET_CHEESE_WHEEL);

        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("fiery_fondue_pot"), FIERY_FONDUE_POT);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("pizza"), PIZZA);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("quiche"), QUICHE);
    }
}
