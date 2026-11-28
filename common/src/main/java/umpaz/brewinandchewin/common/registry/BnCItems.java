package umpaz.brewinandchewin.common.registry;

import com.google.common.collect.Sets;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.item.BoozeItem;
import umpaz.brewinandchewin.common.item.DreadNogItem;
import umpaz.brewinandchewin.common.item.JamJarItem;
import umpaz.brewinandchewin.common.item.KegItem;
import vectorwing.farmersdelight.common.item.ConsumableItem;

import java.util.LinkedHashSet;

public class BnCItems {
    public static LinkedHashSet<Item> CREATIVE_TAB_ITEMS = Sets.newLinkedHashSet();

    private static Item.Properties itemProps(String name) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, BrewinAndChewin.asResource(name)));
    }

    private static Item.Properties blockItemProps(String name) {
        return itemProps(name).useBlockDescriptionPrefix();
    }

    public static void registerWithTab(String name, Item item) {
        registerWithTab(name, item, null);
    }

    public static void registerWithTab(String name, Item item, @Nullable String requiredMod) {
        if (item != TANKARD) {
            Registry.register(BuiltInRegistries.ITEM, BrewinAndChewin.asResource(name), item);
        }
        if (requiredMod == null || BrewinAndChewin.getHelper().isModLoaded(requiredMod))
            CREATIVE_TAB_ITEMS.add(item);
    }

    public static final Item KEG = new KegItem(BnCBlocks.KEG, blockItemProps("keg").stacksTo(1));
    public static final Item HEATING_CASK = new BlockItem(BnCBlocks.HEATING_CASK, blockItemProps("heating_cask"));
    public static final Item ICE_CRATE = new BlockItem(BnCBlocks.ICE_CRATE, blockItemProps("ice_crate"));
    public static final Item COASTER = new BlockItem(BnCBlocks.COASTER, blockItemProps("coaster"));

    public static final Item TANKARD = Registry.register(BuiltInRegistries.ITEM, BrewinAndChewin.asResource("tankard"), new Item(itemProps("tankard")));

    public static final Item BEER = new BoozeItem(() -> BnCFluids.BEER, itemProps("beer")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).usingConvertsTo(BnCItems.TANKARD).food(BnCFoods.BEER, BnCFoods.ConsumableDefs.BEER));
    public static final Item VODKA = new BoozeItem(() -> BnCFluids.VODKA, itemProps("vodka")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).usingConvertsTo(BnCItems.TANKARD).food(BnCFoods.VODKA, BnCFoods.ConsumableDefs.VODKA));
    public static final Item MEAD = new BoozeItem(() -> BnCFluids.MEAD, itemProps("mead")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).usingConvertsTo(BnCItems.TANKARD).food(BnCFoods.MEAD, BnCFoods.ConsumableDefs.MEAD));
    public static final Item RICE_WINE = new BoozeItem(() -> BnCFluids.RICE_WINE, itemProps("rice_wine")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).usingConvertsTo(BnCItems.TANKARD).food(BnCFoods.RICE_WINE, BnCFoods.ConsumableDefs.RICE_WINE));
    public static final Item PALE_JANE = new BoozeItem(() -> BnCFluids.PALE_JANE, itemProps("pale_jane")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).usingConvertsTo(BnCItems.TANKARD).food(BnCFoods.PALE_JANE, BnCFoods.ConsumableDefs.PALE_JANE));
    public static final Item EGG_GROG = new BoozeItem(() -> BnCFluids.EGG_GROG, itemProps("egg_grog")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).usingConvertsTo(BnCItems.TANKARD).food(BnCFoods.EGG_GROG, BnCFoods.ConsumableDefs.EGG_GROG));
    public static final Item GLITTERING_GRENADINE = new BoozeItem(() -> BnCFluids.GLITTERING_GRENADINE, itemProps("glittering_grenadine")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).usingConvertsTo(BnCItems.TANKARD).food(BnCFoods.GLITTERING_GRENADINE, BnCFoods.ConsumableDefs.GLITTERING_GRENADINE));
    public static final Item SACCHARINE_RUM = new BoozeItem(() -> BnCFluids.SACCHARINE_RUM, itemProps("saccharine_rum")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).usingConvertsTo(BnCItems.TANKARD).food(BnCFoods.SACCHARINE_RUM, BnCFoods.ConsumableDefs.SACCHARINE_RUM));
    public static final Item SALTY_FOLLY = new BoozeItem(() -> BnCFluids.SALTY_FOLLY, itemProps("salty_folly")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).usingConvertsTo(BnCItems.TANKARD).food(BnCFoods.SALTY_FOLLY, BnCFoods.ConsumableDefs.SALTY_FOLLY));
    public static final Item BLOODY_MARY = new BoozeItem(() -> BnCFluids.BLOODY_MARY, itemProps("bloody_mary")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).usingConvertsTo(BnCItems.TANKARD).food(BnCFoods.BLOODY_MARY, BnCFoods.ConsumableDefs.BLOODY_MARY));
    public static final Item RED_RUM = new BoozeItem(() -> BnCFluids.RED_RUM, itemProps("red_rum")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).usingConvertsTo(BnCItems.TANKARD).food(BnCFoods.RED_RUM, BnCFoods.ConsumableDefs.RED_RUM));
    public static final Item STRONGROOT_ALE = new BoozeItem(() -> BnCFluids.STRONGROOT_ALE, itemProps("strongroot_ale")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).usingConvertsTo(BnCItems.TANKARD).food(BnCFoods.STRONGROOT_ALE, BnCFoods.ConsumableDefs.STRONGROOT_ALE));
    public static final Item STEEL_TOE_STOUT = new BoozeItem(() -> BnCFluids.STEEL_TOE_STOUT, itemProps("steel_toe_stout")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).usingConvertsTo(BnCItems.TANKARD).food(BnCFoods.STEEL_TOE_STOUT, BnCFoods.ConsumableDefs.STEEL_TOE_STOUT));
    public static final Item DREAD_NOG = new DreadNogItem(() -> BnCFluids.DREAD_NOG, itemProps("dread_nog")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).usingConvertsTo(BnCItems.TANKARD).food(BnCFoods.DREAD_NOG, BnCFoods.ConsumableDefs.DREAD_NOG));
    public static final Item WITHERING_DROSS = new BoozeItem(() -> BnCFluids.WITHERING_DROSS, itemProps("withering_dross")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).usingConvertsTo(BnCItems.TANKARD).food(BnCFoods.WITHERING_DROSS, BnCFoods.ConsumableDefs.WITHERING_DROSS));

    public static final Item KOMBUCHA = new BoozeItem(() -> BnCFluids.KOMBUCHA, itemProps("kombucha")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).usingConvertsTo(BnCItems.TANKARD).food(BnCFoods.KOMBUCHA, BnCFoods.ConsumableDefs.KOMBUCHA));

    public static final Item UNRIPE_FLAXEN_CHEESE_WHEEL = new BlockItem(BnCBlocks.UNRIPE_FLAXEN_CHEESE_WHEEL, blockItemProps("unripe_flaxen_cheese_wheel").stacksTo(16));
    public static final Item FLAXEN_CHEESE_WHEEL = new BlockItem(BnCBlocks.FLAXEN_CHEESE_WHEEL, blockItemProps("flaxen_cheese_wheel").stacksTo(16));
    public static final Item FLAXEN_CHEESE_WEDGE = new Item(itemProps("flaxen_cheese_wedge").food(BnCFoods.FLAXEN_CHEESE));

    public static final Item UNRIPE_SCARLET_CHEESE_WHEEL = new BlockItem(BnCBlocks.UNRIPE_SCARLET_CHEESE_WHEEL, blockItemProps("unripe_scarlet_cheese_wheel").stacksTo(16));
    public static final Item SCARLET_CHEESE_WHEEL = new BlockItem(BnCBlocks.SCARLET_CHEESE_WHEEL, blockItemProps("scarlet_cheese_wheel").stacksTo(16));
    public static final Item SCARLET_CHEESE_WEDGE = new Item(itemProps("scarlet_cheese_wedge").food(BnCFoods.SCARLET_CHEESE));

    public static final Item VEGETABLE_OMELET = new ConsumableItem(itemProps("vegetable_omelet").stacksTo(16).food(BnCFoods.VEGETABLE_OMELET, BnCFoods.ConsumableDefs.VEGETABLE_OMELET).craftRemainder(Items.BOWL).usingConvertsTo(Items.BOWL), true);
    public static final Item CREAMY_ONION_SOUP = new ConsumableItem(itemProps("creamy_onion_soup").stacksTo(16).food(BnCFoods.CREAMY_ONION_SOUP, BnCFoods.ConsumableDefs.CREAMY_ONION_SOUP).craftRemainder(Items.BOWL).usingConvertsTo(Items.BOWL), true);
    public static final Item CHEESY_PASTA = new ConsumableItem(itemProps("cheesy_pasta").stacksTo(16).food(BnCFoods.CHEESY_PASTA, BnCFoods.ConsumableDefs.CHEESY_PASTA).craftRemainder(Items.BOWL).usingConvertsTo(Items.BOWL), true);
    public static final Item HORROR_LASAGNA = new ConsumableItem(itemProps("horror_lasagna").stacksTo(16).food(BnCFoods.HORROR_LASAGNA, BnCFoods.ConsumableDefs.HORROR_LASAGNA).craftRemainder(Items.BOWL).usingConvertsTo(Items.BOWL), true);
    public static final Item SCARLET_PIEROGI = new ConsumableItem(itemProps("scarlet_pierogi").stacksTo(16).food(BnCFoods.SCARLET_PIEROGI, BnCFoods.ConsumableDefs.SCARLET_PIEROGI).craftRemainder(Items.BOWL).usingConvertsTo(Items.BOWL), true);

    public static final Item FIERY_FONDUE_POT = new BlockItem(BnCBlocks.FIERY_FONDUE_POT, blockItemProps("fiery_fondue_pot").stacksTo(1));
    public static final Item FIERY_FONDUE = new ConsumableItem(itemProps("fiery_fondue").stacksTo(16).food(BnCFoods.FIERY_FONDUE, BnCFoods.ConsumableDefs.FIERY_FONDUE).craftRemainder(Items.BOWL).usingConvertsTo(Items.BOWL), true);

    public static final Item PIZZA = new BlockItem(BnCBlocks.PIZZA, blockItemProps("pizza").stacksTo(1));
    public static final Item QUICHE = new BlockItem(BnCBlocks.QUICHE, blockItemProps("quiche"));

    public static final Item PIZZA_SLICE = new Item(itemProps("pizza_slice").food(BnCFoods.PIZZA_SLICE));
    public static final Item QUICHE_SLICE = new Item(itemProps("quiche_slice").food(BnCFoods.QUICHE_SLICE, BnCFoods.ConsumableDefs.FAST_FOOD));

    public static final Item HAM_AND_CHEESE_SANDWICH = new Item(itemProps("ham_and_cheese_sandwich").food(BnCFoods.HAM_AND_CHEESE_SANDWICH));

    public static final Item KIMCHI = new ConsumableItem(itemProps("kimchi").food(BnCFoods.KIMCHI));
    public static final Item JERKY = new ConsumableItem(itemProps("jerky").food(BnCFoods.JERKY, BnCFoods.ConsumableDefs.FAST_FOOD));
    public static final Item PICKLED_PICKLES = new ConsumableItem(itemProps("pickled_pickles").food(BnCFoods.PICKLED_PICKLES));
    public static final Item KIPPERS = new ConsumableItem(itemProps("kippers").food(BnCFoods.KIPPERS));
    public static final Item COCOA_FUDGE = new ConsumableItem(itemProps("cocoa_fudge").food(BnCFoods.COCOA_FUDGE, BnCFoods.ConsumableDefs.COCOA_FUDGE));

    public static final Item SWEET_BERRY_JAM = new JamJarItem(itemProps("sweet_berry_jam").stacksTo(16).craftRemainder(Items.GLASS_BOTTLE).usingConvertsTo(Items.GLASS_BOTTLE).food(BnCFoods.SWEET_BERRY_JAM, BnCFoods.ConsumableDefs.JAM_JAR));
    public static final Item GLOW_BERRY_MARMALADE = new JamJarItem(itemProps("glow_berry_marmalade").stacksTo(16).craftRemainder(Items.GLASS_BOTTLE).usingConvertsTo(Items.GLASS_BOTTLE).food(BnCFoods.GLOW_BERRY_MARMALADE, BnCFoods.ConsumableDefs.JAM_JAR));
    public static final Item APPLE_JELLY = new JamJarItem(itemProps("apple_jelly").stacksTo(16).craftRemainder(Items.GLASS_BOTTLE).usingConvertsTo(Items.GLASS_BOTTLE).food(BnCFoods.APPLE_JELLY, BnCFoods.ConsumableDefs.JAM_JAR));

    public static void registerAll() {
        registerWithTab("keg", KEG);
        registerWithTab("heating_cask", HEATING_CASK);
        registerWithTab("ice_crate", ICE_CRATE);
        registerWithTab("coaster", COASTER);

        registerWithTab("tankard", TANKARD);

        registerWithTab("beer", BEER);
        registerWithTab("vodka", VODKA);
        registerWithTab("mead", MEAD);
        registerWithTab("rice_wine", RICE_WINE);
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
