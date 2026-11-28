package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.*;
import vectorwing.farmersdelight.common.block.PieBlock;

public class BnCBlocks {
    private static BlockBehaviour.Properties props(String name, BlockBehaviour.Properties base) {
        return base.setId(ResourceKey.create(Registries.BLOCK, BrewinAndChewin.asResource(name)));
    }

    public static final Block KEG = new KegBlock(
            props("keg", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));

    public static final Block HEATING_CASK = new HeatingCaskBlock(
            props("heating_cask", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));

    public static final Block ICE_CRATE = new IceCrateBlock(
            props("ice_crate", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));

    public static final Block COASTER = new CoasterBlock(
            props("coaster", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).sound(SoundType.WOOD).instabreak().noOcclusion()));

    //Cheese
    public static final Block UNRIPE_FLAXEN_CHEESE_WHEEL = new
            UnripeCheeseWheelBlock(() -> BnCBlocks.FLAXEN_CHEESE_WHEEL, props("unripe_flaxen_cheese_wheel", BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)));

    public static final Block FLAXEN_CHEESE_WHEEL = new
            CheeseWheelBlock(() -> BnCItems.FLAXEN_CHEESE_WEDGE, props("flaxen_cheese_wheel", BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)));

    public static final Block UNRIPE_SCARLET_CHEESE_WHEEL = new
            UnripeCheeseWheelBlock(() -> BnCBlocks.SCARLET_CHEESE_WHEEL, props("unripe_scarlet_cheese_wheel", BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)));

    public static final Block SCARLET_CHEESE_WHEEL = new
            CheeseWheelBlock(() -> BnCItems.SCARLET_CHEESE_WEDGE, props("scarlet_cheese_wheel", BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)));

    // Feasts
    public static final Block FIERY_FONDUE_POT = new
            FieryFonduePotBlock(props("fiery_fondue_pot", BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)));

    public static final Block PIZZA = new
            PizzaBlock(props("pizza", BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)));

    public static final Block QUICHE = new
            PieBlock(props("quiche", BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)), () -> BnCItems.QUICHE_SLICE);

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
