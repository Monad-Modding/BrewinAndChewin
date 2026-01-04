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
            BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS));

    public static final Block HEATING_CASK = new HeatingCaskBlock(
            BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS));

    public static final Block ICE_CRATE = new IceCrateBlock(
            BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS));

    public static final Block COASTER = new CoasterBlock();

    //Cheese
    public static final Block UNRIPE_FLAXEN_CHEESE_WHEEL = new
            UnripeCheeseWheelBlock(() -> BnCBlocks.FLAXEN_CHEESE_WHEEL, Block.Properties.copy(Blocks.CAKE));

    public static final Block FLAXEN_CHEESE_WHEEL = new
            CheeseWheelBlock(() -> BnCItems.FLAXEN_CHEESE_WEDGE, Block.Properties.copy(Blocks.CAKE));

    public static final Block UNRIPE_SCARLET_CHEESE_WHEEL = new
            UnripeCheeseWheelBlock(() -> BnCBlocks.SCARLET_CHEESE_WHEEL, Block.Properties.copy(Blocks.CAKE));

    public static final Block SCARLET_CHEESE_WHEEL = new
            CheeseWheelBlock(() -> BnCItems.SCARLET_CHEESE_WEDGE, Block.Properties.copy(Blocks.CAKE));

    // Feasts
    public static final Block FIERY_FONDUE_POT = new
            FieryFonduePotBlock(Block.Properties.copy(Blocks.CAULDRON));

    public static final Block PIZZA = new
            PizzaBlock(Block.Properties.copy(Blocks.CAKE));

    public static final Block QUICHE = new
            PieBlock(Block.Properties.copy(Blocks.CAKE), () -> BnCItems.QUICHE_SLICE);


    public static void registerAll() {
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("keg"), KEG);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("heating_cask"), HEATING_CASK);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("ice_crate"), ICE_CRATE);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("coaster"), COASTER);

        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("unripe_flaxen_cheese_wheel"), UNRIPE_FLAXEN_CHEESE_WHEEL);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("flaxen_cheese_wheel"), FLAXEN_CHEESE_WHEEL);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("unripe_scarlet_cheese_wheel"), UNRIPE_SCARLET_CHEESE_WHEEL);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("scarlet_cheese_wheel"), SCARLET_CHEESE_WHEEL);

        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("fiery_fondue_pot"), FIERY_FONDUE_POT);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("pizza"), PIZZA);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("quiche"), QUICHE);
    }
}
