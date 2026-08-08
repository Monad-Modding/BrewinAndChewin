package umpaz.brewinandchewin.common.registry;

import com.google.common.collect.Sets;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.item.BoozeItem;
import umpaz.brewinandchewin.common.item.DistillateItem;
import umpaz.brewinandchewin.common.item.DreadNogItem;
import umpaz.brewinandchewin.common.item.JamJarItem;
import umpaz.brewinandchewin.common.item.KegItem;
import umpaz.brewinandchewin.common.item.LabelItem;
import umpaz.brewinandchewin.common.item.WineItem;
import umpaz.brewinandchewin.common.item.WineType;
import vectorwing.farmersdelight.common.item.ConsumableItem;

import java.util.LinkedHashSet;

public class BnCItems {
    public static LinkedHashSet<Item> CREATIVE_TAB_ITEMS = Sets.newLinkedHashSet();

    public static void registerWithTab(String name, Item item) {
        registerWithTab(name, item, null);
    }

    public static void registerWithTab(String name, Item item, @Nullable String requiredMod) {
        Registry.register(BuiltInRegistries.ITEM, BrewinAndChewin.asResource(name), item);
        if (requiredMod == null || BrewinAndChewin.getHelper().isModLoaded(requiredMod))
            CREATIVE_TAB_ITEMS.add(item);
    }
    
    public static final Item KEG = new KegItem(BnCBlocks.KEG, new Item.Properties().stacksTo(1));
    public static final Item HEATING_CASK = new BlockItem(BnCBlocks.HEATING_CASK, new Item.Properties());
    public static final Item ICE_CRATE = new BlockItem(BnCBlocks.ICE_CRATE, new Item.Properties());
    public static final Item COASTER = new BlockItem(BnCBlocks.COASTER, new Item.Properties());

    public static final Item AGING_CASK = new BlockItem(BnCBlocks.AGING_CASK, new Item.Properties());
    public static final Item BOTTLE_RACK = new BlockItem(BnCBlocks.BOTTLE_RACK, new Item.Properties());
    public static final Item DISTILLERY = new BlockItem(BnCBlocks.DISTILLERY, new Item.Properties());

    public static final Item TANKARD = new Item(new Item.Properties());

    public static final Item RED_GRAPES = new Item(new Item.Properties().food(BnCFoods.GRAPES));
    public static final Item WHITE_GRAPES = new Item(new Item.Properties().food(BnCFoods.GRAPES));
    public static final Item RED_GRAPE_SEEDS = new ItemNameBlockItem(BnCBlocks.RED_GRAPE_VINE, new Item.Properties());
    public static final Item WHITE_GRAPE_SEEDS = new ItemNameBlockItem(BnCBlocks.WHITE_GRAPE_VINE, new Item.Properties());

    public static final Item RED_WINE = new WineItem(WineType.RED, new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE));
    public static final Item WHITE_WINE = new WineItem(WineType.WHITE, new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE));
    public static final Item CURRANT_WINE = new WineItem(WineType.CURRANT, new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE));
    public static final Item VERRUCA_WINE = new WineItem(WineType.VERRUCA, new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE));
    public static final Item TWISTED_WINE = new WineItem(WineType.TWISTED, new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE));
    public static final Item RICE_WINE = new WineItem(WineType.RICE, new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE));
    public static final Item OLD_WINE = new WineItem(WineType.OLD, new Item.Properties().stacksTo(1).craftRemainder(Items.GLASS_BOTTLE).rarity(Rarity.UNCOMMON));

    public static final Item BRANDY = new DistillateItem(2, 4800, false, false, new Item.Properties().stacksTo(16));
    public static final Item AQUA_VITAE = new DistillateItem(5, 6000, false, false, new Item.Properties().stacksTo(16));
    public static final Item SICKENING_TINCTURE = new DistillateItem(0, 0, true, false, new Item.Properties().stacksTo(16));
    public static final Item DELICIOUS_TINCTURE = new DistillateItem(0, 0, false, true, new Item.Properties().stacksTo(16));

    public static final Item LABEL = new LabelItem(new Item.Properties());

    public static final Item BEER = new BoozeItem(() -> BnCFluids.BEER, new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.BEER));
    public static final Item VODKA = new BoozeItem(() -> BnCFluids.VODKA, new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.VODKA));
    public static final Item MEAD = new BoozeItem(() -> BnCFluids.MEAD, new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.MEAD));
    public static final Item PALE_JANE = new BoozeItem(() -> BnCFluids.PALE_JANE, new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.PALE_JANE));
    public static final Item EGG_GROG = new BoozeItem(() -> BnCFluids.EGG_GROG, new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.EGG_GROG));
    public static final Item GLITTERING_GRENADINE = new BoozeItem(() -> BnCFluids.GLITTERING_GRENADINE, new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.GLITTERING_GRENADINE));
    public static final Item SACCHARINE_RUM = new BoozeItem(() -> BnCFluids.SACCHARINE_RUM, new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.SACCHARINE_RUM));
    public static final Item SALTY_FOLLY = new BoozeItem(() -> BnCFluids.SALTY_FOLLY, new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.SALTY_FOLLY));
    public static final Item BLOODY_MARY = new BoozeItem(() -> BnCFluids.BLOODY_MARY,  new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.BLOODY_MARY));
    public static final Item RED_RUM = new BoozeItem(() -> BnCFluids.RED_RUM, new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.RED_RUM));
    public static final Item STRONGROOT_ALE = new BoozeItem(() -> BnCFluids.STRONGROOT_ALE, new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.STRONGROOT_ALE));
    public static final Item STEEL_TOE_STOUT = new BoozeItem(() -> BnCFluids.STEEL_TOE_STOUT, new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.STEEL_TOE_STOUT));
    public static final Item DREAD_NOG = new DreadNogItem(() -> BnCFluids.DREAD_NOG, new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.DREAD_NOG));
    public static final Item WITHERING_DROSS = new BoozeItem(() -> BnCFluids.WITHERING_DROSS, new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.WITHERING_DROSS));

    public static final Item KOMBUCHA = new BoozeItem(() -> BnCFluids.KOMBUCHA, new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.KOMBUCHA));

    public static final Item UNRIPE_FLAXEN_CHEESE_WHEEL = new BlockItem(BnCBlocks.UNRIPE_FLAXEN_CHEESE_WHEEL, new Item.Properties().stacksTo(16));
    public static final Item FLAXEN_CHEESE_WHEEL = new BlockItem(BnCBlocks.FLAXEN_CHEESE_WHEEL, new Item.Properties().stacksTo(16));
    public static final Item FLAXEN_CHEESE_WEDGE = new Item(new Item.Properties().food(BnCFoods.FLAXEN_CHEESE));

    public static final Item UNRIPE_SCARLET_CHEESE_WHEEL = new BlockItem(BnCBlocks.UNRIPE_SCARLET_CHEESE_WHEEL, new Item.Properties().stacksTo(16));
    public static final Item SCARLET_CHEESE_WHEEL = new BlockItem(BnCBlocks.SCARLET_CHEESE_WHEEL, new Item.Properties().stacksTo(16));
    public static final Item SCARLET_CHEESE_WEDGE = new Item(new Item.Properties().food(BnCFoods.SCARLET_CHEESE));

    public static final Item VEGETABLE_OMELET = new ConsumableItem(new Item.Properties().stacksTo(16).food(BnCFoods.VEGETABLE_OMELET).craftRemainder(Items.BOWL), true);
    public static final Item CREAMY_ONION_SOUP = new ConsumableItem(new Item.Properties().stacksTo(16).food(BnCFoods.CREAMY_ONION_SOUP).craftRemainder(Items.BOWL), true);
    public static final Item CHEESY_PASTA = new ConsumableItem(new Item.Properties().stacksTo(16).food(BnCFoods.CHEESY_PASTA).craftRemainder(Items.BOWL), true);
    public static final Item HORROR_LASAGNA = new ConsumableItem(new Item.Properties().stacksTo(16).food(BnCFoods.HORROR_LASAGNA).craftRemainder(Items.BOWL), true);
    public static final Item SCARLET_PIEROGI = new ConsumableItem(new Item.Properties().stacksTo(16).food(BnCFoods.SCARLET_PIEROGI).craftRemainder(Items.BOWL), true);

    public static final Item FIERY_FONDUE_POT = new BlockItem(BnCBlocks.FIERY_FONDUE_POT, new Item.Properties().stacksTo(1));
    public static final Item FIERY_FONDUE = new ConsumableItem(new Item.Properties().stacksTo(16).food(BnCFoods.FIERY_FONDUE).craftRemainder(Items.BOWL), true);

    public static final Item PIZZA = new BlockItem(BnCBlocks.PIZZA, new Item.Properties().stacksTo(1));
    public static final Item QUICHE = new BlockItem(BnCBlocks.QUICHE, new Item.Properties());

    public static final Item PIZZA_SLICE = new Item(new Item.Properties().food(BnCFoods.PIZZA_SLICE));
    public static final Item QUICHE_SLICE = new Item(new Item.Properties().food(BnCFoods.QUICHE_SLICE));

    public static final Item HAM_AND_CHEESE_SANDWICH = new Item(new Item.Properties().food(BnCFoods.HAM_AND_CHEESE_SANDWICH));

    public static final Item KIMCHI = new ConsumableItem(new Item.Properties().food(BnCFoods.KIMCHI));
    public static final Item JERKY = new ConsumableItem(new Item.Properties().food(BnCFoods.JERKY));
    public static final Item PICKLED_PICKLES = new ConsumableItem(new Item.Properties().food(BnCFoods.PICKLED_PICKLES));
    public static final Item KIPPERS = new ConsumableItem(new Item.Properties().food(BnCFoods.KIPPERS));
    public static final Item COCOA_FUDGE = new ConsumableItem(new Item.Properties().food(BnCFoods.COCOA_FUDGE));

    public static final Item SWEET_BERRY_JAM = new JamJarItem(new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE).food(BnCFoods.SWEET_BERRY_JAM));
    public static final Item GLOW_BERRY_MARMALADE = new JamJarItem(new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE).food(BnCFoods.GLOW_BERRY_MARMALADE));
    public static final Item APPLE_JELLY = new JamJarItem(new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE).food(BnCFoods.APPLE_JELLY));

    public static void registerAll() {
        registerWithTab("keg", KEG);
        registerWithTab("heating_cask", HEATING_CASK);
        registerWithTab("ice_crate", ICE_CRATE);
        registerWithTab("coaster", COASTER);
        registerWithTab("aging_cask", AGING_CASK);
        registerWithTab("bottle_rack", BOTTLE_RACK);
        registerWithTab("distillery", DISTILLERY);

        registerWithTab("tankard", TANKARD);

        registerWithTab("red_grapes", RED_GRAPES);
        registerWithTab("white_grapes", WHITE_GRAPES);
        registerWithTab("red_grape_seeds", RED_GRAPE_SEEDS);
        registerWithTab("white_grape_seeds", WHITE_GRAPE_SEEDS);

        registerWithTab("red_wine", RED_WINE);
        registerWithTab("white_wine", WHITE_WINE);
        registerWithTab("currant_wine", CURRANT_WINE);
        registerWithTab("verruca_wine", VERRUCA_WINE);
        registerWithTab("twisted_wine", TWISTED_WINE);
        registerWithTab("rice_wine", RICE_WINE);
        registerWithTab("old_wine", OLD_WINE);

        registerWithTab("brandy", BRANDY);
        registerWithTab("aqua_vitae", AQUA_VITAE);
        registerWithTab("sickening_tincture", SICKENING_TINCTURE);
        registerWithTab("delicious_tincture", DELICIOUS_TINCTURE);

        registerWithTab("label", LABEL);

        registerWithTab("beer", BEER);
        registerWithTab("vodka", VODKA);
        registerWithTab("mead", MEAD);
        registerWithTab("pale_jane", PALE_JANE);
        registerWithTab("egg_grog", EGG_GROG);
        registerWithTab("glittering_grenadine", GLITTERING_GRENADINE);
        registerWithTab("saccharine_rum", SACCHARINE_RUM);
        registerWithTab("salty_folly", SALTY_FOLLY);
        registerWithTab("bloody_mary", BLOODY_MARY);
        registerWithTab("red_rum", RED_RUM);
        registerWithTab("strongroot_ale", STRONGROOT_ALE);
        registerWithTab("steel_toe_stout", STEEL_TOE_STOUT);
        registerWithTab("dread_nog", DREAD_NOG);
        registerWithTab("withering_dross", WITHERING_DROSS);

        registerWithTab("kombucha", KOMBUCHA, "farmersrespite");

        registerWithTab("unripe_flaxen_cheese_wheel", UNRIPE_FLAXEN_CHEESE_WHEEL);
        registerWithTab("flaxen_cheese_wheel", FLAXEN_CHEESE_WHEEL);
        registerWithTab("flaxen_cheese_wedge", FLAXEN_CHEESE_WEDGE);

        registerWithTab("unripe_scarlet_cheese_wheel", UNRIPE_SCARLET_CHEESE_WHEEL);
        registerWithTab("scarlet_cheese_wheel", SCARLET_CHEESE_WHEEL);
        registerWithTab("scarlet_cheese_wedge", SCARLET_CHEESE_WEDGE);
        
        registerWithTab("vegetable_omelet", VEGETABLE_OMELET);
        registerWithTab("creamy_onion_soup", CREAMY_ONION_SOUP);
        registerWithTab("cheesy_pasta", CHEESY_PASTA);
        registerWithTab("horror_lasagna", HORROR_LASAGNA);
        registerWithTab("scarlet_pierogi", SCARLET_PIEROGI);

        registerWithTab("fiery_fondue_pot", FIERY_FONDUE_POT);
        registerWithTab("fiery_fondue", FIERY_FONDUE);

        registerWithTab("pizza", PIZZA);
        registerWithTab("quiche", QUICHE);

        registerWithTab("pizza_slice", PIZZA_SLICE);
        registerWithTab("quiche_slice", QUICHE_SLICE);

        registerWithTab("ham_and_cheese_sandwich", HAM_AND_CHEESE_SANDWICH);

        registerWithTab("kimchi", KIMCHI);
        registerWithTab("jerky", JERKY);
        registerWithTab("pickled_pickles", PICKLED_PICKLES);
        registerWithTab("kippers", KIPPERS);
        registerWithTab("cocoa_fudge", COCOA_FUDGE);

        registerWithTab("sweet_berry_jam", SWEET_BERRY_JAM);
        registerWithTab("glow_berry_marmalade", GLOW_BERRY_MARMALADE);
        registerWithTab("apple_jelly", APPLE_JELLY);
    }
}
