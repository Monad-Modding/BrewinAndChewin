package umpaz.brewinandchewin.neoforge.registry;

import net.minecraft.core.Registry;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.neoforge.fluid.BnCFluidType;

public class BnCFluidTypes {
    public static final FluidType HONEY = new BnCFluidType();
    
    public static final FluidType BEER = new BnCFluidType();
    public static final FluidType MEAD = new BnCFluidType();
    public static final FluidType EGG_GROG = new BnCFluidType();
    public static final FluidType STRONGROOT_ALE = new BnCFluidType();
    public static final FluidType RICE_WINE = new BnCFluidType();
    public static final FluidType GLITTERING_GRENADINE = new BnCFluidType();
    public static final FluidType STEEL_TOE_STOUT = new BnCFluidType();
    public static final FluidType DREAD_NOG = new BnCFluidType();
    public static final FluidType KOMBUCHA = new BnCFluidType();
    public static final FluidType SACCHARINE_RUM = new BnCFluidType();
    public static final FluidType PALE_JANE = new BnCFluidType();
    public static final FluidType SALTY_FOLLY = new BnCFluidType();
    public static final FluidType BLOODY_MARY = new BnCFluidType();
    public static final FluidType RED_RUM = new BnCFluidType();
    public static final FluidType WITHERING_DROSS = new BnCFluidType();

    public static final FluidType RED_WINE = new BnCFluidType();
    public static final FluidType WHITE_WINE = new BnCFluidType();
    public static final FluidType CURRANT_WINE = new BnCFluidType();
    public static final FluidType VERRUCA_WINE = new BnCFluidType();
    public static final FluidType TWISTED_WINE = new BnCFluidType();
    public static final FluidType VODKA = new BnCFluidType();
    public static final FluidType FLAXEN_CHEESE = new BnCFluidType();
    public static final FluidType SCARLET_CHEESE = new BnCFluidType();

    public static void registerAll() {
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("honey"), HONEY);
        
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("beer"), BEER);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("mead"), MEAD);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("egg_grog"), EGG_GROG);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("strongroot_ale"), STRONGROOT_ALE);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("rice_wine"), RICE_WINE);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("glittering_grenadine"), GLITTERING_GRENADINE);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("steel_toe_stout"), STEEL_TOE_STOUT);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("dread_nog"), DREAD_NOG);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("kombucha"), KOMBUCHA);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("saccharine_rum"), SACCHARINE_RUM);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("pale_jane"), PALE_JANE);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("salty_folly"), SALTY_FOLLY);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("bloody_mary"), BLOODY_MARY);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("red_rum"), RED_RUM);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("withering_dross"), WITHERING_DROSS);
        
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("red_wine"), RED_WINE);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("white_wine"), WHITE_WINE);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("currant_wine"), CURRANT_WINE);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("verruca_wine"), VERRUCA_WINE);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("twisted_wine"), TWISTED_WINE);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("vodka"), VODKA);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("flaxen_cheese"), FLAXEN_CHEESE);
        Registry.register(NeoForgeRegistries.FLUID_TYPES, BrewinAndChewin.asResource("scarlet_cheese"), SCARLET_CHEESE);
    }

}
