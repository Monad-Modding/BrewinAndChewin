package umpaz.brewinandchewin.neoforge.registry;

import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import umpaz.brewinandchewin.neoforge.loot.modifier.BnCAddTableModifier;
import umpaz.brewinandchewin.neoforge.loot.modifier.BnCSlicingModifier;

public class BnCLootModifiers {
    public static void registerAll() {
        Registry.register(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, BnCSlicingModifier.ID, BnCSlicingModifier.CODEC);
        Registry.register(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, BnCAddTableModifier.ID, BnCAddTableModifier.CODEC);
    }
}
