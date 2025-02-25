package umpaz.brewinandchewin.common.registry;

import com.google.common.collect.Sets;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.item.BoozeItem;
import umpaz.brewinandchewin.common.item.DreadNogItem;
import umpaz.brewinandchewin.common.item.JamJarItem;
import umpaz.brewinandchewin.common.item.KegItem;
import vectorwing.farmersdelight.common.item.ConsumableItem;

import java.util.LinkedHashSet;
import java.util.function.Supplier;

public class BnCItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BrewinAndChewin.MODID);
    public static LinkedHashSet<RegistryObject<Item>> CREATIVE_TAB_ITEMS = Sets.newLinkedHashSet();

    public static RegistryObject<Item> registerWithTab(String name, Supplier<Item> supplier) {
        RegistryObject<Item> block = ITEMS.register(name, supplier);
        CREATIVE_TAB_ITEMS.add(block);
        return block;
    }

   public static final RegistryObject<Item> KEG = registerWithTab("keg", () -> new KegItem(BnCBlocks.KEG.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HEATING_CASK = registerWithTab("heating_cask", () -> new BlockItem(BnCBlocks.HEATING_CASK.get(), new Item.Properties()));
    public static final RegistryObject<Item> ICE_CRATE = registerWithTab("ice_crate", () -> new BlockItem(BnCBlocks.ICE_CRATE.get(), new Item.Properties()));
    public static final RegistryObject<Item> COASTER = registerWithTab("coaster", () -> new BlockItem(BnCBlocks.COASTER.get(), new Item.Properties()));

    public static final RegistryObject<Item> TANKARD = registerWithTab("tankard", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BEER = registerWithTab("beer", () -> new BoozeItem(BnCFluids.BEER.get(), new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD.get()).food(BnCFoods.BEER)));
    public static final RegistryObject<Item> VODKA = registerWithTab("vodka", () -> new BoozeItem(BnCFluids.VODKA.get(), new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD.get()).food(BnCFoods.VODKA)));
    public static final RegistryObject<Item> MEAD = registerWithTab("mead", () -> new BoozeItem(BnCFluids.MEAD.get(), new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD.get()).food(BnCFoods.MEAD)));
    public static final RegistryObject<Item> RICE_WINE = registerWithTab("rice_wine", () -> new BoozeItem(BnCFluids.RICE_WINE.get(), new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD.get()).food(BnCFoods.RICE_WINE)));
    public static final RegistryObject<Item> EGG_GROG = registerWithTab("egg_grog", () -> new BoozeItem(BnCFluids.EGG_GROG.get(), new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD.get()).food(BnCFoods.EGG_GROG)));
    public static final RegistryObject<Item> STRONGROOT_ALE = registerWithTab("strongroot_ale", () -> new BoozeItem(BnCFluids.STRONGROOT_ALE.get(), new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD.get()).food(BnCFoods.STRONGROOT_ALE)));
    public static final RegistryObject<Item> SACCHARINE_RUM = registerWithTab("saccharine_rum", () -> new BoozeItem(BnCFluids.SACCHARINE_RUM.get(), new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD.get()).food(BnCFoods.SACCHARINE_RUM)));
    public static final RegistryObject<Item> PALE_JANE = registerWithTab("pale_jane", () -> new BoozeItem(BnCFluids.PALE_JANE.get(), new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD.get()).food(BnCFoods.PALE_JANE)));
    public static final RegistryObject<Item> SALTY_FOLLY = registerWithTab("salty_folly", () -> new BoozeItem(BnCFluids.SALTY_FOLLY.get(), new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD.get()).food(BnCFoods.SALTY_FOLLY)));
    public static final RegistryObject<Item> STEEL_TOE_STOUT = registerWithTab("steel_toe_stout", () -> new BoozeItem(BnCFluids.STEEL_TOE_STOUT.get(), new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD.get()).food(BnCFoods.STEEL_TOE_STOUT)));
    public static final RegistryObject<Item> GLITTERING_GRENADINE = registerWithTab("glittering_grenadine", () -> new BoozeItem(BnCFluids.GLITTERING_GRENADINE.get(), new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD.get()).food(BnCFoods.GLITTERING_GRENADINE)));
    public static final RegistryObject<Item> BLOODY_MARY = registerWithTab("bloody_mary", () -> new BoozeItem(BnCFluids.BLOODY_MARY.get(),  new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD.get()).food(BnCFoods.BLOODY_MARY)));
    public static final RegistryObject<Item> RED_RUM = registerWithTab("red_rum", () -> new BoozeItem(BnCFluids.RED_RUM.get(), new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD.get()).food(BnCFoods.RED_RUM)));
    public static final RegistryObject<Item> WITHERING_DROSS = registerWithTab("withering_dross", () -> new BoozeItem(BnCFluids.WITHERING_DROSS.get(), new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD.get()).food(BnCFoods.WITHERING_DROSS)));
    public static final RegistryObject<Item> DREAD_NOG = registerWithTab("dread_nog", () -> new DreadNogItem(BnCFluids.DREAD_NOG.get(), new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD.get()).food(BnCFoods.DREAD_NOG)));

    public static final RegistryObject<Item> KOMBUCHA = registerWithTab("kombucha", () -> new BoozeItem(BnCFluids.KOMBUCHA.get(), new Item.Properties()
            .stacksTo(16).craftRemainder(BnCItems.TANKARD.get()).food(BnCFoods.KOMBUCHA)));

    public static final RegistryObject<Item> KIMCHI = registerWithTab("kimchi",
            () -> new ConsumableItem(new Item.Properties().food(BnCFoods.KIMCHI)));
    public static final RegistryObject<Item> JERKY = registerWithTab("jerky",
            () -> new ConsumableItem(new Item.Properties().food(BnCFoods.JERKY)));
    public static final RegistryObject<Item> PICKLED_PICKLES = registerWithTab("pickled_pickles",
            () -> new ConsumableItem(new Item.Properties().food(BnCFoods.PICKLED_PICKLES)));
    public static final RegistryObject<Item> KIPPERS = registerWithTab("kippers",
            () -> new ConsumableItem(new Item.Properties().food(BnCFoods.KIPPERS)));
    public static final RegistryObject<Item> COCOA_FUDGE = registerWithTab("cocoa_fudge",
            () -> new ConsumableItem(new Item.Properties().food(BnCFoods.COCOA_FUDGE)));

    public static final RegistryObject<Item> VEGETABLE_OMELET = registerWithTab("vegetable_omelet",
            () -> new ConsumableItem(new Item.Properties().stacksTo(16).food(BnCFoods.VEGETABLE_OMELET).craftRemainder(Items.BOWL), true));
    public static final RegistryObject<Item> CHEESY_PASTA = registerWithTab("cheesy_pasta",
            () -> new ConsumableItem(new Item.Properties().stacksTo(16).food(BnCFoods.CHEESY_PASTA).craftRemainder(Items.BOWL), true));
    public static final RegistryObject<Item> CREAMY_ONION_SOUP = registerWithTab("creamy_onion_soup",
            () -> new ConsumableItem(new Item.Properties().stacksTo(16).food(BnCFoods.CREAMY_ONION_SOUP).craftRemainder(Items.BOWL), true));
    public static final RegistryObject<Item> SCARLET_PIEROGI = registerWithTab("scarlet_pierogi",
            () -> new ConsumableItem(new Item.Properties().stacksTo(16).food(BnCFoods.SCARLET_PIEROGIES).craftRemainder(Items.BOWL), true));
    public static final RegistryObject<Item> HORROR_LASAGNA = registerWithTab("horror_lasagna",
            () -> new ConsumableItem(new Item.Properties().stacksTo(16).food(BnCFoods.HORROR_LASAGNA).craftRemainder(Items.BOWL), true));

    public static final RegistryObject<Item> UNRIPE_FLAXEN_CHEESE_WHEEL = registerWithTab("unripe_flaxen_cheese_wheel",
            () -> new BlockItem(BnCBlocks.UNRIPE_FLAXEN_CHEESE_WHEEL.get(), new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> FLAXEN_CHEESE_WHEEL = registerWithTab("flaxen_cheese_wheel",
            () -> new BlockItem(BnCBlocks.FLAXEN_CHEESE_WHEEL.get(), new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> UNRIPE_SCARLET_CHEESE_WHEEL = registerWithTab("unripe_scarlet_cheese_wheel",
            () -> new BlockItem(BnCBlocks.UNRIPE_SCARLET_CHEESE_WHEEL.get(), new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SCARLET_CHEESE_WHEEL = registerWithTab("scarlet_cheese_wheel",
            () -> new BlockItem(BnCBlocks.SCARLET_CHEESE_WHEEL.get(), new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> FLAXEN_CHEESE_WEDGE = registerWithTab("flaxen_cheese_wedge",
            () -> new Item(new Item.Properties().food(BnCFoods.FLAXEN_CHEESE)));
    public static final RegistryObject<Item> SCARLET_CHEESE_WEDGE = registerWithTab("scarlet_cheese_wedge",
            () -> new Item(new Item.Properties().food(BnCFoods.SCARLET_CHEESE)));

    public static final RegistryObject<Item> PIZZA = registerWithTab("pizza",
            () -> new BlockItem(BnCBlocks.PIZZA.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PIZZA_SLICE = registerWithTab("pizza_slice",
            () -> new Item(new Item.Properties().food(BnCFoods.PIZZA_SLICE)));

    public static final RegistryObject<Item> FIERY_FONDUE_POT = registerWithTab("fiery_fondue_pot",
            () -> new BlockItem(BnCBlocks.FIERY_FONDUE_POT.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FIERY_FONDUE = registerWithTab("fiery_fondue",
            () -> new ConsumableItem(new Item.Properties().stacksTo(16).food(BnCFoods.FIERY_FONDUE).craftRemainder(Items.BOWL), true));

    public static final RegistryObject<Item> HAM_AND_CHEESE_SANDWICH = registerWithTab("ham_and_cheese_sandwich",
            () -> new Item(new Item.Properties().food(BnCFoods.HAM_AND_CHEESE_SANDWICH)));

    public static final RegistryObject<Item> QUICHE = registerWithTab("quiche",
            () -> new BlockItem(BnCBlocks.QUICHE.get(), new Item.Properties()));
    public static final RegistryObject<Item> QUICHE_SLICE = registerWithTab("quiche_slice",
            () -> new Item(new Item.Properties().food(BnCFoods.QUICHE_SLICE)));

    public static final RegistryObject<Item> SWEET_BERRY_JAM = registerWithTab("sweet_berry_jam",
            () -> new JamJarItem(new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE).food(BnCFoods.SWEET_BERRY_JAM)));
    public static final RegistryObject<Item> GLOW_BERRY_MARMALADE = registerWithTab("glow_berry_marmalade",
            () -> new JamJarItem(new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE).food(BnCFoods.GLOW_BERRY_MARMALADE)));
    public static final RegistryObject<Item> APPLE_JELLY = registerWithTab("apple_jelly",
            () -> new JamJarItem(new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE).food(BnCFoods.APPLE_JELLY)));

}
