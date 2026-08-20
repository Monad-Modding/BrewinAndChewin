package umpaz.brewinandchewin.fabric.registry;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.loot.BnCInnardsDrops;

import java.util.Set;

public class BnCLootModificationEvents {
    private static final ResourceLocation OLD_WINE_TABLE = BrewinAndChewin.asResource("chests/old_wine");

    private static final Set<ResourceLocation> OLD_WINE_CHESTS = Set.of(
            ResourceLocation.withDefaultNamespace("chests/simple_dungeon"),
            ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft"),
            ResourceLocation.withDefaultNamespace("chests/stronghold_corridor"),
            ResourceLocation.withDefaultNamespace("chests/stronghold_crossing"),
            ResourceLocation.withDefaultNamespace("chests/woodland_mansion"));

    public static void init() {
        LootTableEvents.MODIFY.register(BnCLootModificationEvents::modifyTable);
    }

    private static void modifyTable(ResourceKey<LootTable> key, LootTable.Builder tableBuilder, LootTableSource source, HolderLookup.Provider registries) {
        if (source.isBuiltin() && OLD_WINE_CHESTS.contains(key.location())) {
            tableBuilder.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(NestedLootTable.lootTableReference(ResourceKey.create(key.registryKey(), OLD_WINE_TABLE))));
        }

        float[] innards = BnCInnardsDrops.DROPS.get(key.location());
        if (source.isBuiltin() && innards != null) {
            tableBuilder.withPool(BnCInnardsDrops.pool(innards[0], innards[1]));
        }
    }
}
