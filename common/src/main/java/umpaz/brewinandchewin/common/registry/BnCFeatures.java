package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.world.feature.WildGrapesFeature;

public class BnCFeatures {
    public static final Feature<NoneFeatureConfiguration> WILD_GRAPES =
            new WildGrapesFeature(NoneFeatureConfiguration.CODEC);

    public static void registerAll() {
        Registry.register(BuiltInRegistries.FEATURE, BrewinAndChewin.asResource("wild_grapes"), WILD_GRAPES);
    }
}
