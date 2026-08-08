package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import umpaz.brewinandchewin.common.loot.function.BnCCopyMealFunction;
import umpaz.brewinandchewin.common.loot.function.CopyDrinkFunction;
import umpaz.brewinandchewin.common.loot.function.RandomiseOldWineFunction;

public class BnCLootFunctions {
    public static void registerAll() {
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, CopyDrinkFunction.ID, CopyDrinkFunction.TYPE);
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, BnCCopyMealFunction.ID, BnCCopyMealFunction.TYPE);
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, RandomiseOldWineFunction.ID, RandomiseOldWineFunction.TYPE);
    }
}
