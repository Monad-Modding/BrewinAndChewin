package umpaz.brewinandchewin.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import umpaz.brewinandchewin.data.world.BnCWildCropGeneration;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.internal.NeoForgeAdvancementProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCDamageTypes;
import umpaz.brewinandchewin.data.recipe.BnCEntityTypeTags;
import umpaz.brewinandchewin.neoforge.BrewinAndChewinNeoForge;
import umpaz.brewinandchewin.data.loot.BnCBlockLoot;
import vectorwing.farmersdelight.data.BlockTags;
import vectorwing.farmersdelight.data.ItemTags;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = BrewinAndChewin.MODID, bus = EventBusSubscriber.Bus.MOD)
public class BnCDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper helper = event.getExistingFileHelper();

        BnCBlockTags blockTags = new BnCBlockTags(output, lookupProvider, helper);
        BnCBuiltInEntries builtInEntries = new BnCBuiltInEntries(output, lookupProvider, new RegistrySetBuilder()
                .add(Registries.DAMAGE_TYPE, bootstrap ->
                        bootstrap.register(BnCDamageTypes.CARDIAC_ARREST, new DamageType(
                                "brewinandchewin.cardiacArrest",
                                DamageScaling.NEVER,
                                0.1F
                        ))
                )
                .add(Registries.CONFIGURED_FEATURE, BnCWildCropGeneration::bootstrapConfiguredFeatures)
                .add(Registries.PLACED_FEATURE, BnCWildCropGeneration::bootstrapPlacedFeatures)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, BnCWildCropGeneration::bootstrapBiomeModifiers)
        );
        generator.addProvider(event.includeServer(), builtInEntries);
        lookupProvider = builtInEntries.getRegistryProvider();
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new BnCItemTags(output, lookupProvider, blockTags.contentsGetter(), helper));
        generator.addProvider(event.includeServer(), new BnCFluidTags(output, lookupProvider, helper));
        generator.addProvider(event.includeServer(), new BnCMobEffectTags(output, lookupProvider, helper));
        generator.addProvider(event.includeServer(), new BnCEntityTypeTags(output, lookupProvider, helper));
        generator.addProvider(event.includeServer(), new BnCDamageTypeTags(output, lookupProvider, helper));
        generator.addProvider(event.includeServer(), new BnCBiomeTags(output, lookupProvider, helper));
        generator.addProvider(event.includeServer(), new BnCRecipes(output, lookupProvider));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Collections.emptySet(), List.of(
                new LootTableProvider.SubProviderEntry(BnCBlockLoot::new, LootContextParamSets.BLOCK)
        ), lookupProvider));
        generator.addProvider(event.includeServer(), new AdvancementProvider(output, lookupProvider, helper, List.of(new BnCAdvancements())));
    }
}
