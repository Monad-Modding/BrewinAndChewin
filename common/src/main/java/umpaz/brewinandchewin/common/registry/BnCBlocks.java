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

    public static final Block RED_GRAPE_BUSH = new GrapeBushBlock(
            GrapeColour.RED, BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT));
    public static final Block WHITE_GRAPE_BUSH = new GrapeBushBlock(
            GrapeColour.WHITE, BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT));

    public static final Block WILD_GRAPES = new WildGrapesBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.VINE).noOcclusion());

    public static final Block RED_GRAPE_STEM = new GrapeStemBlock(
            GrapeColour.RED, BlockBehaviour.Properties.ofFullCopy(Blocks.VINE).noOcclusion());
    public static final Block WHITE_GRAPE_STEM = new GrapeStemBlock(
            GrapeColour.WHITE, BlockBehaviour.Properties.ofFullCopy(Blocks.VINE).noOcclusion());

    public static final Block RED_ROPE_GRAPE = new RopeGrapeBlock(
            GrapeColour.RED, BlockBehaviour.Properties.ofFullCopy(Blocks.VINE).randomTicks());
    public static final Block WHITE_ROPE_GRAPE = new RopeGrapeBlock(
            GrapeColour.WHITE, BlockBehaviour.Properties.ofFullCopy(Blocks.VINE).randomTicks());

    public static final Block TRELLIS = new TrellisBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR).noOcclusion());

    public static final Block TRELLIS_GRAPE = new TrellisGrapeBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR).noOcclusion().randomTicks());

    public static final Block WILD_CORN = new WildCornBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_GRASS));

    public static final Block CORN_CROP = new CornCropBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT));



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

    public static final Block RICH_CHOCOLATE_CAKE = new
            SlicedCakeBlock(Block.Properties.ofFullCopy(Blocks.CAKE), () -> BnCItems.SLICE_OF_RICH_CHOCOLATE_CAKE,
            2.0D, 14.0D, 8.0D, 2.0D, 14.0D);

    public static final Block PUMPKIN_ROLL = new
            SlicedCakeBlock(Block.Properties.ofFullCopy(Blocks.CAKE), () -> BnCItems.SLICE_OF_PUMPKIN_ROLL,
            4.0D, 12.0D, 6.0D, 2.0D, 14.0D);

    public static final Block GLOW_BERRY_MERINGUE_PIE = new
            PieBlock(Block.Properties.ofFullCopy(Blocks.CAKE).lightLevel(state -> 7),
            () -> BnCItems.SLICE_OF_GLOW_BERRY_MERINGUE_PIE);


    public static void registerAll() {
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("keg"), KEG);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("heating_cask"), HEATING_CASK);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("ice_crate"), ICE_CRATE);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("coaster"), COASTER);

        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("aging_cask"), AGING_CASK);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("bottle_rack"), BOTTLE_RACK);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("red_grape_bush"), RED_GRAPE_BUSH);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("white_grape_bush"), WHITE_GRAPE_BUSH);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("wild_grapes"), WILD_GRAPES);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("red_grape_stem"), RED_GRAPE_STEM);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("white_grape_stem"), WHITE_GRAPE_STEM);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("red_rope_grape"), RED_ROPE_GRAPE);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("white_rope_grape"), WHITE_ROPE_GRAPE);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("trellis"), TRELLIS);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("trellis_grape"), TRELLIS_GRAPE);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("corn_crop"), CORN_CROP);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("wild_corn"), WILD_CORN);

        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("unripe_flaxen_cheese_wheel"), UNRIPE_FLAXEN_CHEESE_WHEEL);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("flaxen_cheese_wheel"), FLAXEN_CHEESE_WHEEL);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("unripe_scarlet_cheese_wheel"), UNRIPE_SCARLET_CHEESE_WHEEL);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("scarlet_cheese_wheel"), SCARLET_CHEESE_WHEEL);

        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("fiery_fondue_pot"), FIERY_FONDUE_POT);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("pizza"), PIZZA);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("quiche"), QUICHE);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("rich_chocolate_cake"), RICH_CHOCOLATE_CAKE);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("pumpkin_roll"), PUMPKIN_ROLL);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("glow_berry_meringue_pie"), GLOW_BERRY_MERINGUE_PIE);
    }
}
