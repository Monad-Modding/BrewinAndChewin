package umpaz.brewinandchewin.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCFluids;
import umpaz.brewinandchewin.common.tag.BnCTags;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class BnCFluidTags extends FluidTagsProvider {

    public BnCFluidTags(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, BrewinAndChewin.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        registerModTags();
        registerConventionalTags();
    }

    private void registerModTags() {
        tag(BnCTags.Fluids.BEER)
                .add(BnCFluids.BEER, BnCFluids.FLOWING_BEER);
        tag(BnCTags.Fluids.VODKA)
                .add(BnCFluids.VODKA, BnCFluids.FLOWING_VODKA);
        tag(BnCTags.Fluids.MEAD)
                .add(BnCFluids.MEAD, BnCFluids.FLOWING_MEAD);
        tag(BnCTags.Fluids.RED_WINE)
                .add(BnCFluids.RED_WINE, BnCFluids.FLOWING_RED_WINE);
        tag(BnCTags.Fluids.WHITE_WINE)
                .add(BnCFluids.WHITE_WINE, BnCFluids.FLOWING_WHITE_WINE);
        tag(BnCTags.Fluids.CURRANT_WINE)
                .add(BnCFluids.CURRANT_WINE, BnCFluids.FLOWING_CURRANT_WINE);
        tag(BnCTags.Fluids.VERRUCA_WINE)
                .add(BnCFluids.VERRUCA_WINE, BnCFluids.FLOWING_VERRUCA_WINE);
        tag(BnCTags.Fluids.TWISTED_WINE)
                .add(BnCFluids.TWISTED_WINE, BnCFluids.FLOWING_TWISTED_WINE);
        tag(BnCTags.Fluids.RICE_WINE)
                .add(BnCFluids.RICE_WINE, BnCFluids.FLOWING_RICE_WINE);
        tag(BnCTags.Fluids.PALE_JANE)
                .add(BnCFluids.PALE_JANE, BnCFluids.FLOWING_PALE_JANE);
        tag(BnCTags.Fluids.EGG_GROG)
                .add(BnCFluids.EGG_GROG, BnCFluids.FLOWING_EGG_GROG);
        tag(BnCTags.Fluids.GLITTERING_GRENADINE)
                .add(BnCFluids.GLITTERING_GRENADINE, BnCFluids.GLITTERING_GRENADINE);
        tag(BnCTags.Fluids.SACCHARINE_RUM)
                .add(BnCFluids.SACCHARINE_RUM, BnCFluids.SACCHARINE_RUM);
        tag(BnCTags.Fluids.SALTY_FOLLY)
                .add(BnCFluids.SALTY_FOLLY, BnCFluids.FLOWING_SALTY_FOLLY);
        tag(BnCTags.Fluids.BLOODY_MARY)
                .add(BnCFluids.BLOODY_MARY, BnCFluids.FLOWING_BLOODY_MARY);
        tag(BnCTags.Fluids.RED_RUM)
                .add(BnCFluids.RED_RUM, BnCFluids.FLOWING_RED_RUM);
        tag(BnCTags.Fluids.STRONGROOT_ALE)
                .add(BnCFluids.STRONGROOT_ALE, BnCFluids.FLOWING_STRONGROOT_ALE);
        tag(BnCTags.Fluids.STEEL_TOE_STOUT)
                .add(BnCFluids.STEEL_TOE_STOUT, BnCFluids.FLOWING_STEEL_TOE_STOUT);
        tag(BnCTags.Fluids.DREAD_NOG)
                .add(BnCFluids.DREAD_NOG, BnCFluids.FLOWING_DREAD_NOG);
        tag(BnCTags.Fluids.WITHERING_DROSS)
                .add(BnCFluids.WITHERING_DROSS, BnCFluids.FLOWING_WITHERING_DROSS);

        tag(BnCTags.Fluids.FLAXEN_CHEESE)
                .add(BnCFluids.FLAXEN_CHEESE, BnCFluids.FLOWING_FLAXEN_CHEESE);
        tag(BnCTags.Fluids.SCARLET_CHEESE)
                .add(BnCFluids.SCARLET_CHEESE, BnCFluids.FLOWING_FLAXEN_CHEESE);
    }

    private void registerConventionalTags() {
        tag(Tags.Fluids.HONEY)
                .add(BnCFluids.HONEY, BnCFluids.FLOWING_HONEY);
    }
}