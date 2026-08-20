package umpaz.brewinandchewin.common.compat.nomansland;

import net.minecraft.resources.ResourceLocation;
import umpaz.brewinandchewin.BrewinAndChewin;

public class NMLIntegration {
    public static final String MOD_ID = "nomansland";

    public static final ResourceLocation WALNUTS = ResourceLocation.fromNamespaceAndPath(MOD_ID, "walnuts");
    public static final ResourceLocation MAPLE_SYRUP_BOTTLE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "maple_syrup_bottle");
    public static final ResourceLocation THISTLE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "thistle");

    public static final ResourceLocation PRAIRIE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "prairie");

    public static final ResourceLocation RICE_PUDDING_ITEM = BrewinAndChewin.asResource("rice_pudding");
    public static final ResourceLocation MAPLE_FUDGE_ITEM = BrewinAndChewin.asResource("maple_fudge");

    public static boolean isLoaded() {
        return BrewinAndChewin.getHelper().isModLoadedEarly(MOD_ID);
    }
}
