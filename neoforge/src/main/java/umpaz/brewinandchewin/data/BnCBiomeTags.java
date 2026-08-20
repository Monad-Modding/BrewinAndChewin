package umpaz.brewinandchewin.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.neoforged.neoforge.common.Tags;
import umpaz.brewinandchewin.common.compat.nomansland.NMLIntegration;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.world.BnCBiomeFeatures;

import java.util.concurrent.CompletableFuture;

public class BnCBiomeTags extends BiomeTagsProvider {
    public BnCBiomeTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, BrewinAndChewin.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BnCBiomeFeatures.HAS_WILD_CORN)
                .addTag(Tags.Biomes.IS_PLAINS)
                .addOptional(NMLIntegration.PRAIRIE);
    }
}
