package umpaz.brewinandchewin.integration.emi;

import dev.emi.emi.api.stack.EmiStack;
import umpaz.brewinandchewin.common.registry.BnCItems;

public class BnCRecipeWorkstations {
    public static final EmiStack KEG = EmiStack.of(BnCItems.KEG.get());
    public static final EmiStack AGING = EmiStack.of(BnCItems.FLAXEN_CHEESE_WHEEL.get());
}
