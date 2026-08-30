package umpaz.brewinandchewin.common.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import umpaz.brewinandchewin.BrewinAndChewin;

public class BnCBiomeFeatures {
    public static final int WILD_CORN_RARITY = 40;
    public static final int WILD_CORN_TRIES = 24;
    public static final int WILD_GRAPES_RARITY = 5;

    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_WILD_CORN =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, BrewinAndChewin.asResource("patch_wild_corn"));
    public static final ResourceKey<PlacedFeature> PLACED_WILD_CORN =
            ResourceKey.create(Registries.PLACED_FEATURE, BrewinAndChewin.asResource("patch_wild_corn"));

    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_WILD_GRAPES =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, BrewinAndChewin.asResource("patch_wild_grapes"));
    public static final ResourceKey<PlacedFeature> PLACED_WILD_GRAPES =
            ResourceKey.create(Registries.PLACED_FEATURE, BrewinAndChewin.asResource("patch_wild_grapes"));

    public static final TagKey<Biome> HAS_WILD_CORN =
            TagKey.create(Registries.BIOME, BrewinAndChewin.asResource("has_wild_corn"));
    public static final TagKey<Biome> HAS_WILD_GRAPES =
            TagKey.create(Registries.BIOME, BrewinAndChewin.asResource("has_wild_grapes"));
    public static final TagKey<Biome> IS_SNOWY =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "is_snowy"));
}
