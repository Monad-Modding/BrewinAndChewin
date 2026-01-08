package umpaz.brewinandchewin.common.registry;

import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.loot.function.BnCCopyMealFunction;
import umpaz.brewinandchewin.common.loot.function.CopyDrinkFunction;
import vectorwing.farmersdelight.common.loot.function.CopyMealFunction;
import vectorwing.farmersdelight.common.loot.function.CopySkilletFunction;
import vectorwing.farmersdelight.common.loot.function.SmokerCookFunction;

import java.util.function.Supplier;

public class BnCLootFunctions {
    public static final LazyRegistrar<LootItemFunctionType> LOOT_FUNCTIONS;
    public static final Supplier<LootItemFunctionType> COPY_MEAL;
    public static final Supplier<LootItemFunctionType> COPY_DRINK;

    public static void registerAll() {
    }

    static {
        LOOT_FUNCTIONS = LazyRegistrar.create(BuiltInRegistries.LOOT_FUNCTION_TYPE.key(), BrewinAndChewin.MODID);
        COPY_MEAL = LOOT_FUNCTIONS.register("copy_meal", () -> new LootItemFunctionType(new BnCCopyMealFunction.Serializer()));
        COPY_DRINK = LOOT_FUNCTIONS.register("copy_drink", () -> new LootItemFunctionType(new CopyDrinkFunction.Serializer()));
    }

}
