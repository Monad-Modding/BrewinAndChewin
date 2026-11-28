package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import umpaz.brewinandchewin.common.loot.condition.AreaLocationCheckCondition;
import umpaz.brewinandchewin.common.loot.condition.NullTrueBlockStateCondition;

public class BnCLootConditions {
    public static void registerAll() {
        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, AreaLocationCheckCondition.ID, AreaLocationCheckCondition.CODEC);
        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, NullTrueBlockStateCondition.ID, NullTrueBlockStateCondition.CODEC);
    }
}
