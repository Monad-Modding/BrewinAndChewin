package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import umpaz.brewinandchewin.common.loot.function.BnCCopyMealFunction;
import umpaz.brewinandchewin.common.loot.function.CopyDrinkFunction;

public class BnCLootFunctions {
    public static void registerAll() {
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, CopyDrinkFunction.ID, CopyDrinkFunction.CODEC);
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, BnCCopyMealFunction.ID, BnCCopyMealFunction.CODEC);
    }
}
