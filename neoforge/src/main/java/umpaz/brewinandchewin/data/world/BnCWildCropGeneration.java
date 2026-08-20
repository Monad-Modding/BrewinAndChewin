package umpaz.brewinandchewin.data.world;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.world.BnCBiomeFeatures;

import java.util.List;

public class BnCWildCropGeneration {
    public static final ResourceKey<BiomeModifier> ADD_WILD_CORN =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, BrewinAndChewin.asResource("add_wild_corn"));

    public static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(BnCBiomeFeatures.PATCH_WILD_CORN, new ConfiguredFeature<>(Feature.RANDOM_PATCH,
                new RandomPatchConfiguration(BnCBiomeFeatures.WILD_CORN_TRIES, 6, 2,
                        PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                                new SimpleBlockConfiguration(BlockStateProvider.simple(BnCBlocks.WILD_CORN))))));
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);
        context.register(BnCBiomeFeatures.PLACED_WILD_CORN, new PlacedFeature(
                features.getOrThrow(BnCBiomeFeatures.PATCH_WILD_CORN),
                List.of(RarityFilter.onAverageOnceEvery(BnCBiomeFeatures.WILD_CORN_RARITY),
                        InSquarePlacement.spread(),
                        HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING),
                        BiomeFilter.biome())));
    }

    public static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        context.register(ADD_WILD_CORN, new BiomeModifiers.AddFeaturesBiomeModifier(
                context.lookup(Registries.BIOME).getOrThrow(BnCBiomeFeatures.HAS_WILD_CORN),
                net.minecraft.core.HolderSet.direct(
                        (Holder<PlacedFeature>) context.lookup(Registries.PLACED_FEATURE).getOrThrow(BnCBiomeFeatures.PLACED_WILD_CORN)),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.VEGETAL_DECORATION));
    }
}
