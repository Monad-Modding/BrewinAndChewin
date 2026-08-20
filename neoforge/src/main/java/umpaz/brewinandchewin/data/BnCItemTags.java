package umpaz.brewinandchewin.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.tag.BnCCompatTags;
import umpaz.brewinandchewin.common.tag.BnCTags;
import vectorwing.farmersdelight.common.registry.ModItems;
import umpaz.brewinandchewin.common.compat.nomansland.NMLIntegration;
import vectorwing.farmersdelight.common.tag.CommonTags;
import vectorwing.farmersdelight.common.tag.ModTags;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class BnCItemTags extends ItemTagsProvider {

    public BnCItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTagProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, blockTagProvider, BrewinAndChewin.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        registerModTags();
        registerFarmersDelightTags();
        registerConventionalTags();
        registerCompatibilityTags();
    }

    private void registerModTags() {
        copy(BnCTags.Blocks.CHEESE_WHEELS_UNRIPE, BnCTags.Items.CHEESE_WHEELS_UNRIPE);
        copy(BnCTags.Blocks.CHEESE_WHEELS_RIPE, BnCTags.Items.CHEESE_WHEELS_RIPE);
        tag(BnCTags.Items.FERMENTED_DRINKS)
                .add(BnCItems.BEER)
                .add(BnCItems.MEAD)
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
        tag(BnCTags.Items.JAMS)
                .add(BnCItems.SWEET_BERRY_JAM)
                .add(BnCItems.GLOW_BERRY_MARMALADE)
                .add(BnCItems.APPLE_JELLY);
        tag(BnCTags.Items.HAGGIS_MEAT)
                .add(BnCItems.COOKED_SAUSAGE)
                .add(BnCItems.RAW_SAUSAGE)
                .addOptionalTag(CommonTags.Items.FOODS_RAW_MUTTON);
        tag(BnCTags.Items.HAGGIS_VEGETABLE)
                .addOptionalTag(CommonTags.Items.FOODS_ONION)
                .addOptionalTag(Tags.Items.CROPS_POTATO);
        tag(BnCTags.Items.GRAPES)
                .add(BnCItems.RED_GRAPES)
                .add(BnCItems.WHITE_GRAPES);
        tag(BnCTags.Items.WINES)
                .add(BnCItems.RED_WINE)
                .add(BnCItems.WHITE_WINE)
                .add(BnCItems.CURRANT_WINE)
                .add(BnCItems.VERRUCA_WINE)
                .add(BnCItems.TWISTED_WINE)
                .add(BnCItems.RICE_WINE)
                .add(BnCItems.OLD_WINE);
        tag(BnCTags.Items.DISTILLATES)
                .add(BnCItems.BRANDY)
                .add(BnCItems.AQUA_VITAE)
                .add(BnCItems.SICKENING_TINCTURE)
                .add(BnCItems.DELICIOUS_TINCTURE)
                .add(Items.POTION)
                .add(Items.SPLASH_POTION)
                .add(Items.LINGERING_POTION);
    }

    private void registerFarmersDelightTags() {
        tag(ModTags.Items.PIES)
                .add(BnCItems.GLOW_BERRY_MERINGUE_PIE);
        tag(ModTags.Items.SWEETS)
                .add(BnCItems.RICH_CHOCOLATE_CAKE)
                .add(BnCItems.PUMPKIN_ROLL)
                .add(BnCItems.SLICE_OF_RICH_CHOCOLATE_CAKE)
                .add(BnCItems.SLICE_OF_GLOW_BERRY_MERINGUE_PIE)
                .add(BnCItems.SLICE_OF_PUMPKIN_ROLL)
                .add(BnCItems.GLOW_BROWNIE)
                .add(BnCItems.APPLE_TURNOVER)
                .add(BnCItems.BUTTERSCOTCH_CANDY)
                .add(BnCItems.CANDIED_CORN)
                .add(BnCItems.COCOA_FUDGE);
        tag(ModTags.Items.SNACKS)
                .add(BnCItems.JAM_SANDWICH)
                .add(BnCItems.CROISSANT)
                .add(BnCItems.TORTILLA)
                .add(BnCItems.HAM_AND_CHEESE_SANDWICH);
        tag(ModTags.Items.MEALS)
                .add(BnCItems.CHOPPED_LIVER)
                .add(BnCItems.GRITS)
                .add(BnCItems.HAGGIS);
    }

    private void registerConventionalTags() {
        tag(ItemTags.DYEABLE)
                .add(BnCItems.LABEL);
        tag(Tags.Items.FOODS_FRUIT)
                .add(BnCItems.RED_GRAPES)
                .add(BnCItems.WHITE_GRAPES);
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
                .addTag(BnCTags.Items.FOOD_CHEESE_WEDGE)
                .add(BnCItems.SLICE_OF_RICH_CHOCOLATE_CAKE)
                .add(BnCItems.SLICE_OF_GLOW_BERRY_MERINGUE_PIE)
                .add(BnCItems.SLICE_OF_PUMPKIN_ROLL)
                .add(BnCItems.ASPIC_CUBE)
                .add(BnCItems.CHOPPED_LIVER)
                .add(BnCItems.GRITS)
                .add(BnCItems.GLOW_BROWNIE)
                .add(BnCItems.APPLE_TURNOVER)
                .add(BnCItems.JAM_SANDWICH)
                .add(BnCItems.BUTTERSCOTCH_CANDY)
                .add(BnCItems.HAGGIS)
                .add(BnCItems.POPPED_CORN)
                .add(BnCItems.CANDIED_CORN)
                .add(BnCItems.CORN_MUFFIN)
                .add(BnCItems.RENNET);
        tag(Tags.Items.FOODS_EDIBLE_WHEN_PLACED)
                .addTag(BnCTags.Items.CHEESE_WHEELS_RIPE)
                .add(BnCItems.FIERY_FONDUE_POT)
                .add(BnCItems.PIZZA)
                .add(BnCItems.QUICHE)
                .add(BnCItems.RICH_CHOCOLATE_CAKE)
                .add(BnCItems.PUMPKIN_ROLL)
                .add(BnCItems.GLOW_BERRY_MERINGUE_PIE);
        tag(Tags.Items.FOODS)
                .addOptional(NMLIntegration.RICE_PUDDING_ITEM)
                .addOptional(NMLIntegration.MAPLE_FUDGE_ITEM);
        tag(Tags.Items.FOODS_PIE)
                .add(BnCItems.GLOW_BERRY_MERINGUE_PIE);
        tag(Tags.Items.FOODS_CANDY)
                .add(BnCItems.BUTTERSCOTCH_CANDY)
                .add(BnCItems.CANDIED_CORN);
        tag(Tags.Items.FOODS_BREAD)
                .add(BnCItems.CORN_BREAD)
                .add(BnCItems.CROISSANT);
        tag(Tags.Items.FOODS_DOUGH)
                .add(BnCItems.CORN_DOUGH);
        tag(Tags.Items.CROPS)
                .add(BnCItems.CORN);
        tag(Tags.Items.FOODS_VEGETABLE)
                .add(BnCItems.CORN)
                .add(BnCItems.COOKED_CORN);
        tag(Tags.Items.FOODS_RAW_MEAT)
                .add(BnCItems.RAW_SAUSAGE)
                .add(BnCItems.INNARDS);
        tag(Tags.Items.FOODS_COOKED_MEAT)
                .add(BnCItems.COOKED_SAUSAGE);
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