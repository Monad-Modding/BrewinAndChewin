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
    private static final ResourceLocation WHITE_GRAPES_TABLE = BrewinAndChewin.asResource("chests/white_grapes");

    private static final Set<ResourceLocation> OLD_WINE_CHESTS = Set.of(
            ResourceLocation.withDefaultNamespace("chests/simple_dungeon"),
            ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft"),
            ResourceLocation.withDefaultNamespace("chests/stronghold_corridor"),
            ResourceLocation.withDefaultNamespace("chests/stronghold_crossing"),
            ResourceLocation.withDefaultNamespace("chests/woodland_mansion"),
            ResourceLocation.withDefaultNamespace("chests/ancient_city"),
            ResourceLocation.withDefaultNamespace("chests/ancient_city_ice_box"),
            ResourceLocation.withDefaultNamespace("chests/trial_chambers/corridor"),
            ResourceLocation.withDefaultNamespace("chests/trial_chambers/entrance"),
            ResourceLocation.withDefaultNamespace("chests/trial_chambers/intersection"),
            ResourceLocation.withDefaultNamespace("chests/trial_chambers/intersection_barrel"),
            ResourceLocation.withDefaultNamespace("chests/trial_chambers/supply"));

    private static final Set<ResourceLocation> WHITE_GRAPES_CHESTS = Set.of(
            ResourceLocation.withDefaultNamespace("chests/woodland_mansion"),
            ResourceLocation.withDefaultNamespace("chests/pillager_outpost"));

    public static void init() {
        LootTableEvents.MODIFY.register(BnCLootModificationEvents::modifyTable);
    }

    private static void modifyTable(ResourceKey<LootTable> key, LootTable.Builder tableBuilder, LootTableSource source, HolderLookup.Provider registries) {
        if (source.isBuiltin() && OLD_WINE_CHESTS.contains(key.location()))
            addTable(key, tableBuilder, OLD_WINE_TABLE);

        if (source.isBuiltin() && WHITE_GRAPES_CHESTS.contains(key.location()))
            addTable(key, tableBuilder, WHITE_GRAPES_TABLE);

        float[] innards = BnCInnardsDrops.DROPS.get(key.location());
        if (source.isBuiltin() && innards != null) {
            tableBuilder.withPool(BnCInnardsDrops.pool(innards[0], innards[1]));
        }
    }

    private static void addTable(ResourceKey<LootTable> key, LootTable.Builder tableBuilder, ResourceLocation table) {
        tableBuilder.withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(NestedLootTable.lootTableReference(ResourceKey.create(key.registryKey(), table))));
    }
}
