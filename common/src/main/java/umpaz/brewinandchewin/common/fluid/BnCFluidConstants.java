package umpaz.brewinandchewin.common.fluid;

import net.minecraft.resources.Identifier;
import umpaz.brewinandchewin.BrewinAndChewin;

public class BnCFluidConstants {
    public static class Textures {
        public static final Identifier FLUID_STILL_TEXTURE = Identifier.withDefaultNamespace("block/water_still");
        public static final Identifier FLUID_FLOWING_TEXTURE = Identifier.withDefaultNamespace("block/water_flow");

        public static final Identifier FLAXEN_STILL_TEXTURE = BrewinAndChewin.asResource("block/flaxen_cheese_still");
        public static final Identifier FLAXEN_FLOWING_TEXTURE = BrewinAndChewin.asResource("block/flaxen_cheese_flow");

        public static final Identifier SCARLET_STILL_TEXTURE = BrewinAndChewin.asResource("block/scarlet_cheese_still");
        public static final Identifier SCARLET_FLOWING_TEXTURE = BrewinAndChewin.asResource("block/scarlet_cheese_flow");

        public static final Identifier HONEY_FLUID_STILL_TEXTURE = Identifier.withDefaultNamespace("block/honey_block_top");
        public static final Identifier HONEY_FLUID_FLOWING_TEXTURE = Identifier.withDefaultNamespace("block/honey_block_top");
    }

    public static class Colors {
        public static final int DEFAULT = 0xFFFFFFFF;

        public static final int BEER = 0xFFFBB117;
        public static final int VODKA = 0xFFE7FDF6;
        public static final int MEAD = 0xFFFFD32D;
        public static final int EGG_GROG = 0xFFFFFFFF;
        public static final int STRONGROOT_ALE = 0xFFBC4A4F;
        public static final int RICE_WINE = 0xFFFFFFFF;
        public static final int GLITTERING_GRENADINE = 0xFFF5A55E;
        public static final int STEEL_TOE_STOUT = 0xFF978B8C;
        public static final int DREAD_NOG = 0xFF25DAB7;
        public static final int KOMBUCHA = 0xFF929238;
        public static final int SACCHARINE_RUM = 0xFFCD4A7A;
        public static final int PALE_JANE = 0xFFD8BEAB;
        public static final int SALTY_FOLLY = 0xFF38672D;
        public static final int BLOODY_MARY = 0xFF84160D;
        public static final int RED_RUM = 0xFF521810;
        public static final int WITHERING_DROSS = 0xFF191411;
    }
}
