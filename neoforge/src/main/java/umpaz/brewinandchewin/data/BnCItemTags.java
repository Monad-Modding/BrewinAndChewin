package umpaz.brewinandchewin.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.tag.BnCCompatTags;
import umpaz.brewinandchewin.common.tag.BnCTags;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.CommonTags;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class BnCItemTags extends ItemTagsProvider {

    public BnCItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTagProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, blockTagProvider, BrewinAndChewin.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        registerModTags();
        registerConventionalTags();
        registerCompatibilityTags();
    }

    private void registerModTags() {
        copy(BnCTags.Blocks.CHEESE_WHEELS_UNRIPE, BnCTags.Items.CHEESE_WHEELS_UNRIPE);
        copy(BnCTags.Blocks.CHEESE_WHEELS_RIPE, BnCTags.Items.CHEESE_WHEELS_RIPE);
        tag(BnCTags.Items.FERMENTED_DRINKS)
                .add(BnCItems.BEER)
                .add(BnCItems.VODKA)
                .add(BnCItems.MEAD)
                .add(BnCItems.RICE_WINE)
                .add(BnCItems.EGG_GROG)
                .add(BnCItems.STRONGROOT_ALE)
                .add(BnCItems.SACCHARINE_RUM)
                .add(BnCItems.PALE_JANE)
                .add(BnCItems.SALTY_FOLLY)
                .add(BnCItems.STEEL_TOE_STOUT)
                .add(BnCItems.GLITTERING_GRENADINE)
                .add(BnCItems.BLOODY_MARY)
                .add(BnCItems.RED_RUM)
                .add(BnCItems.WITHERING_DROSS)
                .add(BnCItems.KOMBUCHA)
                .add(BnCItems.DREAD_NOG);
        tag(BnCTags.Items.FOOD_CHEESE_WEDGE)
                .add(BnCItems.FLAXEN_CHEESE_WEDGE)
                .add(BnCItems.SCARLET_CHEESE_WEDGE);
        tag(BnCTags.Items.FOOD_PIZZA_TOPPING)
                .add(Items.BROWN_MUSHROOM).add(Items.RED_MUSHROOM)
                .add(Items.CARROT).add(Items.BEETROOT).add(ModItems.CABBAGE_LEAF.get()).add(ModItems.ONION.get())
                .addOptionalTag(CommonTags.Items.FOODS_COOKED_BACON).addOptionalTag(CommonTags.Items.FOODS_COOKED_BEEF).addOptionalTag(CommonTags.Items.FOODS_COOKED_COD).addOptionalTag(CommonTags.Items.FOODS_COOKED_MUTTON).addOptionalTag(CommonTags.Items.FOODS_COOKED_PORK);
        tag(BnCTags.Items.FOOD_HORROR_MEAT).addOptionalTag(CommonTags.Items.FOODS_RAW_BEEF).addOptionalTag(CommonTags.Items.FOODS_RAW_CHICKEN);
        tag(BnCTags.Items.FOOD_JERKY_MEAT).add(Items.ROTTEN_FLESH).addTag(Tags.Items.FOODS_RAW_MEAT);
        copy(BnCTags.Blocks.PLAYER_WORKSTATIONS_KEGS, BnCTags.Items.PLAYER_WORKSTATIONS_KEGS);
    }

    private void registerConventionalTags() {
        tag(Tags.Items.FOODS)
                .add(BnCItems.KIMCHI)
                .add(BnCItems.JERKY)
                .add(BnCItems.PICKLED_PICKLES)
                .add(BnCItems.KIPPERS)
                .add(BnCItems.COCOA_FUDGE)
                .add(BnCItems.VEGETABLE_OMELET)
                .add(BnCItems.CHEESY_PASTA)
                .add(BnCItems.CREAMY_ONION_SOUP)
                .add(BnCItems.SCARLET_PIEROGI)
                .add(BnCItems.HORROR_LASAGNA)
                .add(BnCItems.PIZZA_SLICE)
                .add(BnCItems.FIERY_FONDUE)
                .add(BnCItems.HAM_AND_CHEESE_SANDWICH)
                .add(BnCItems.SWEET_BERRY_JAM)
                .add(BnCItems.GLOW_BERRY_MARMALADE)
                .add(BnCItems.APPLE_JELLY)
                .addTag(BnCTags.Items.FOOD_CHEESE_WEDGE);
        tag(Tags.Items.FOODS_EDIBLE_WHEN_PLACED)
                .addTag(BnCTags.Items.CHEESE_WHEELS_RIPE)
                .add(BnCItems.FIERY_FONDUE_POT)
                .add(BnCItems.PIZZA)
                .add(BnCItems.QUICHE);
        tag(Tags.Items.FOODS_SOUP)
                .add(BnCItems.CREAMY_ONION_SOUP)
                .add(BnCItems.FIERY_FONDUE);
    }

    public void registerCompatibilityTags() {
        tag(BnCCompatTags.ORIGINS_MEAT)
                .add(BnCItems.JERKY)
                .add(BnCItems.KIPPERS)
                .add(BnCItems.CHEESY_PASTA)
                .add(BnCItems.HORROR_LASAGNA)
                .add(BnCItems.FIERY_FONDUE)
                .add(BnCItems.HAM_AND_CHEESE_SANDWICH);
        tag(BnCCompatTags.ORIGINS_IGNORE_DIET)
                .addTag(BnCTags.Items.FERMENTED_DRINKS);
    }
}