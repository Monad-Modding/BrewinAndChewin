
package umpaz.brewinandchewin.fabric.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCFluids;
import umpaz.brewinandchewin.fabric.fluid.BnCFluidFabric;

public class BnCFluidsImpl {
    public static FlowingFluid MILK = new BnCFluidFabric.Source(() -> BnCFluidsImpl.FLOWING_MILK);
    public static FlowingFluid FLOWING_MILK = new BnCFluidFabric.Flowing(() -> BnCFluidsImpl.MILK);

    public static void init() {
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("milk"), MILK);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_milk"), FLOWING_MILK);

        BnCFluids.HONEY = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_HONEY);
        BnCFluids.FLOWING_HONEY = new BnCFluidFabric.Flowing(() -> BnCFluids.HONEY);
        BnCFluids.BEER = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_BEER);
        BnCFluids.FLOWING_BEER = new BnCFluidFabric.Flowing(() -> BnCFluids.BEER);
        BnCFluids.VODKA = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_VODKA);
        BnCFluids.FLOWING_VODKA = new BnCFluidFabric.Flowing(() -> BnCFluids.VODKA);
        BnCFluids.MEAD = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_MEAD);
        BnCFluids.FLOWING_MEAD = new BnCFluidFabric.Flowing(() -> BnCFluids.MEAD);
        BnCFluids.EGG_GROG = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_EGG_GROG);
        BnCFluids.FLOWING_EGG_GROG = new BnCFluidFabric.Flowing(() -> BnCFluids.EGG_GROG);
        BnCFluids.STRONGROOT_ALE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_STRONGROOT_ALE);
        BnCFluids.FLOWING_STRONGROOT_ALE = new BnCFluidFabric.Flowing(() -> BnCFluids.STRONGROOT_ALE);
        BnCFluids.RICE_WINE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_RICE_WINE);
        BnCFluids.FLOWING_RICE_WINE = new BnCFluidFabric.Flowing(() -> BnCFluids.RICE_WINE);
        BnCFluids.GLITTERING_GRENADINE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_GLITTERING_GRENADINE);
        BnCFluids.FLOWING_GLITTERING_GRENADINE = new BnCFluidFabric.Flowing(() -> BnCFluids.GLITTERING_GRENADINE);
        BnCFluids.STEEL_TOE_STOUT = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_STEEL_TOE_STOUT);
        BnCFluids.FLOWING_STEEL_TOE_STOUT = new BnCFluidFabric.Flowing(() -> BnCFluids.STEEL_TOE_STOUT);
        BnCFluids.DREAD_NOG = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_DREAD_NOG);
        BnCFluids.FLOWING_DREAD_NOG = new BnCFluidFabric.Flowing(() -> BnCFluids.DREAD_NOG);
        BnCFluids.SACCHARINE_RUM = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_SACCHARINE_RUM);
        BnCFluids.FLOWING_SACCHARINE_RUM = new BnCFluidFabric.Flowing(() -> BnCFluids.SACCHARINE_RUM);
        BnCFluids.PALE_JANE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_PALE_JANE);
        BnCFluids.FLOWING_PALE_JANE = new BnCFluidFabric.Flowing(() -> BnCFluids.PALE_JANE);
        BnCFluids.SALTY_FOLLY = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_SALTY_FOLLY);
        BnCFluids.FLOWING_SALTY_FOLLY = new BnCFluidFabric.Flowing(() -> BnCFluids.SALTY_FOLLY);
        BnCFluids.BLOODY_MARY = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_BLOODY_MARY);
        BnCFluids.FLOWING_BLOODY_MARY = new BnCFluidFabric.Flowing(() -> BnCFluids.BLOODY_MARY);
        BnCFluids.RED_RUM = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_RED_RUM);
        BnCFluids.FLOWING_RED_RUM = new BnCFluidFabric.Flowing(() -> BnCFluids.RED_RUM);
        BnCFluids.WITHERING_DROSS = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_WITHERING_DROSS);
        BnCFluids.FLOWING_WITHERING_DROSS = new BnCFluidFabric.Flowing(() -> BnCFluids.WITHERING_DROSS);
        BnCFluids.KOMBUCHA = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_KOMBUCHA);
        BnCFluids.FLOWING_KOMBUCHA = new BnCFluidFabric.Flowing(() -> BnCFluids.KOMBUCHA);
        BnCFluids.RED_WINE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_RED_WINE);
        BnCFluids.FLOWING_RED_WINE = new BnCFluidFabric.Flowing(() -> BnCFluids.RED_WINE);
        BnCFluids.WHITE_WINE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_WHITE_WINE);
        BnCFluids.FLOWING_WHITE_WINE = new BnCFluidFabric.Flowing(() -> BnCFluids.WHITE_WINE);
        BnCFluids.CURRANT_WINE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_CURRANT_WINE);
        BnCFluids.FLOWING_CURRANT_WINE = new BnCFluidFabric.Flowing(() -> BnCFluids.CURRANT_WINE);
        BnCFluids.VERRUCA_WINE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_VERRUCA_WINE);
        BnCFluids.FLOWING_VERRUCA_WINE = new BnCFluidFabric.Flowing(() -> BnCFluids.VERRUCA_WINE);
        BnCFluids.TWISTED_WINE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_TWISTED_WINE);
        BnCFluids.FLOWING_TWISTED_WINE = new BnCFluidFabric.Flowing(() -> BnCFluids.TWISTED_WINE);
        BnCFluids.FLAXEN_CHEESE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_FLAXEN_CHEESE);
        BnCFluids.FLOWING_FLAXEN_CHEESE = new BnCFluidFabric.Flowing(() -> BnCFluids.FLAXEN_CHEESE);
        BnCFluids.SCARLET_CHEESE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_SCARLET_CHEESE);
        BnCFluids.FLOWING_SCARLET_CHEESE = new BnCFluidFabric.Flowing(() -> BnCFluids.SCARLET_CHEESE);
    }
}