package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import umpaz.brewinandchewin.BrewinAndChewin;

public class BnCFluids {
    public static FlowingFluid HONEY;
    public static FlowingFluid FLOWING_HONEY;
    
    public static FlowingFluid BEER;
    public static FlowingFluid FLOWING_BEER;

    public static FlowingFluid MEAD;
    public static FlowingFluid FLOWING_MEAD;

    public static FlowingFluid RICE_WINE;
    public static FlowingFluid FLOWING_RICE_WINE;

    public static FlowingFluid PALE_JANE;
    public static FlowingFluid FLOWING_PALE_JANE;
    
    public static FlowingFluid EGG_GROG;
    public static FlowingFluid FLOWING_EGG_GROG;

    public static FlowingFluid GLITTERING_GRENADINE;
    public static FlowingFluid FLOWING_GLITTERING_GRENADINE;
    
    public static FlowingFluid SACCHARINE_RUM;
    public static FlowingFluid FLOWING_SACCHARINE_RUM;
    
    public static FlowingFluid SALTY_FOLLY;
    public static FlowingFluid FLOWING_SALTY_FOLLY;
    
    public static FlowingFluid BLOODY_MARY;
    public static FlowingFluid FLOWING_BLOODY_MARY;
  
    public static FlowingFluid RED_RUM;
    public static FlowingFluid FLOWING_RED_RUM;

    public static FlowingFluid STRONGROOT_ALE;
    public static FlowingFluid FLOWING_STRONGROOT_ALE;

    public static FlowingFluid STEEL_TOE_STOUT;
    public static FlowingFluid FLOWING_STEEL_TOE_STOUT;

    public static FlowingFluid DREAD_NOG;
    public static FlowingFluid FLOWING_DREAD_NOG;
    
    public static FlowingFluid WITHERING_DROSS;
    public static FlowingFluid FLOWING_WITHERING_DROSS;
    
    public static FlowingFluid KOMBUCHA;
    public static FlowingFluid FLOWING_KOMBUCHA;
    

    public static FlowingFluid RED_WINE;
    public static FlowingFluid FLOWING_RED_WINE;

    public static FlowingFluid WHITE_WINE;
    public static FlowingFluid FLOWING_WHITE_WINE;

    public static FlowingFluid CURRANT_WINE;
    public static FlowingFluid FLOWING_CURRANT_WINE;

    public static FlowingFluid VERRUCA_WINE;
    public static FlowingFluid FLOWING_VERRUCA_WINE;

    public static FlowingFluid TWISTED_WINE;
    public static FlowingFluid FLOWING_TWISTED_WINE;

    public static FlowingFluid VODKA;
    public static FlowingFluid FLOWING_VODKA;

    public static FlowingFluid FLAXEN_CHEESE;
    public static FlowingFluid FLOWING_FLAXEN_CHEESE;
  
    public static FlowingFluid SCARLET_CHEESE;
    public static FlowingFluid FLOWING_SCARLET_CHEESE;

    public static void registerAll() {
        BrewinAndChewin.getHelper(). initFluids();

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("honey"), HONEY);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_honey"), FLOWING_HONEY);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("beer"), BEER);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_beer"), FLOWING_BEER);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("vodka"), VODKA);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_vodka"), FLOWING_VODKA);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("mead"), MEAD);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_mead"), FLOWING_MEAD);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("rice_wine"), RICE_WINE);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_rice_wine"), FLOWING_RICE_WINE);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("pale_jane"), PALE_JANE);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_pale_jane"), FLOWING_PALE_JANE);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("egg_grog"), EGG_GROG);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_egg_grog"), FLOWING_EGG_GROG);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("glittering_grenadine"), GLITTERING_GRENADINE);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_glittering_grenadine"), FLOWING_GLITTERING_GRENADINE);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("saccharine_rum"), SACCHARINE_RUM);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_saccharine_rum"), FLOWING_SACCHARINE_RUM);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("salty_folly"), SALTY_FOLLY);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_salty_folly"), FLOWING_SALTY_FOLLY);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("bloody_mary"), BLOODY_MARY);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_bloody_mary"), FLOWING_BLOODY_MARY);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("red_rum"), RED_RUM);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_red_rum"), FLOWING_RED_RUM);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("strongroot_ale"), STRONGROOT_ALE);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_strongroot_ale"), FLOWING_STRONGROOT_ALE);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("steel_toe_stout"), STEEL_TOE_STOUT);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_steel_toe_stout"), FLOWING_STEEL_TOE_STOUT);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("dread_nog"), DREAD_NOG);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_dread_nog"), FLOWING_DREAD_NOG);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("withering_dross"), WITHERING_DROSS);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_withering_dross"), FLOWING_WITHERING_DROSS);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("kombucha"), KOMBUCHA);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_kombucha"), FLOWING_KOMBUCHA);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("red_wine"), RED_WINE);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_red_wine"), FLOWING_RED_WINE);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("white_wine"), WHITE_WINE);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_white_wine"), FLOWING_WHITE_WINE);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("currant_wine"), CURRANT_WINE);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_currant_wine"), FLOWING_CURRANT_WINE);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("verruca_wine"), VERRUCA_WINE);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_verruca_wine"), FLOWING_VERRUCA_WINE);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("twisted_wine"), TWISTED_WINE);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_twisted_wine"), FLOWING_TWISTED_WINE);

        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flaxen_cheese"), FLAXEN_CHEESE);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_flaxen_cheese"), FLOWING_FLAXEN_CHEESE);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("scarlet_cheese"), SCARLET_CHEESE);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_scarlet_cheese"), FLOWING_SCARLET_CHEESE);
    }
}
