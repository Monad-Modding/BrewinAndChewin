package umpaz.brewinandchewin.common.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import umpaz.brewinandchewin.BrewinAndChewin;

public class BnCTags {

    public static class Items {
        public static final TagKey<Item> CHEESE_WHEELS_UNRIPE = modItemTag("cheese_wheels/unripe");
        public static final TagKey<Item> CHEESE_WHEELS_RIPE = modItemTag("cheese_wheels/ripe");
        public static final TagKey<Item> FERMENTED_DRINKS = modItemTag("fermented_drinks");
        public static final TagKey<Item> FOOD_CHEESE_WEDGE = modItemTag("foods/cheese_wedge");
        public static final TagKey<Item> FOOD_HORROR_MEAT = modItemTag("foods/horror_meat");
        public static final TagKey<Item> FOOD_JERKY_MEAT = modItemTag("foods/jerky_meat");
        public static final TagKey<Item> FOOD_PIZZA_TOPPING = modItemTag("foods/pizza_topping");
        public static final TagKey<Item> PLAYER_WORKSTATIONS_KEGS = modItemTag("player_workstations/kegs");
        public static final TagKey<Item> GRAPES = modItemTag("grapes");
        public static final TagKey<Item> WINES = modItemTag("wines");
        public static final TagKey<Item> DISTILLATES = modItemTag("distillates");
        public static final TagKey<Item> JAMS = modItemTag("jams");
        public static final TagKey<Item> HAGGIS_MEAT = modItemTag("foods/haggis_meat");
        public static final TagKey<Item> HAGGIS_VEGETABLE = modItemTag("foods/haggis_vegetable");

        private static TagKey<Item> modItemTag(String path) {
            return TagKey.create(Registries.ITEM, BrewinAndChewin.asResource(path));
        }
    }

    public static class Blocks {
        public static final TagKey<Block> CHEESE_WHEELS_UNRIPE = modBlockTag("cheese_wheels/unripe");
        public static final TagKey<Block> CHEESE_WHEELS_RIPE = modBlockTag("cheese_wheels/ripe");
        public static final TagKey<Block> FREEZE_SOURCES = modBlockTag("freeze_sources");
        public static final TagKey<Block> PLAYER_WORKSTATIONS_KEGS = modBlockTag("player_workstations/kegs");
        public static final TagKey<Block> WILD_GRAPES_GROW_ON = modBlockTag("wild_grapes_grow_on");

        private static TagKey<Block> modBlockTag(String path) {
            return TagKey.create(Registries.BLOCK, BrewinAndChewin.asResource(path));
        }
    }

    public static class EntityTypes {
        public static final TagKey<EntityType<?>> IMMUNE_TO_INTOXICATION = modEntityTypeTag("immune_to_intoxication");

        private static TagKey<EntityType<?>> modEntityTypeTag(String path) {
            return TagKey.create(Registries.ENTITY_TYPE, BrewinAndChewin.asResource(path));
        }
    }

    public static class Fluids {
        public static final TagKey<Fluid> BEER = modFluidTag("beer");
        public static final TagKey<Fluid> MEAD = modFluidTag("mead");
        public static final TagKey<Fluid> RICE_WINE = modFluidTag("rice_wine");
        public static final TagKey<Fluid> PALE_JANE = modFluidTag("pale_jane");
        public static final TagKey<Fluid> EGG_GROG = modFluidTag("egg_grog");
        public static final TagKey<Fluid> GLITTERING_GRENADINE = modFluidTag("glittering_grenadine");
        public static final TagKey<Fluid> SACCHARINE_RUM = modFluidTag("saccharine_rum");
        public static final TagKey<Fluid> SALTY_FOLLY = modFluidTag("salty_folly");
        public static final TagKey<Fluid> BLOODY_MARY = modFluidTag("bloody_mary");
        public static final TagKey<Fluid> RED_RUM = modFluidTag("red_rum");
        public static final TagKey<Fluid> STRONGROOT_ALE = modFluidTag("strongroot_ale");
        public static final TagKey<Fluid> STEEL_TOE_STOUT = modFluidTag("steel_toe_stout");
        public static final TagKey<Fluid> DREAD_NOG = modFluidTag("dread_nog");
        public static final TagKey<Fluid> WITHERING_DROSS = modFluidTag("withering_dross");
        public static final TagKey<Fluid> KOMBUCHA = modFluidTag("kombucha");

        public static final TagKey<Fluid> RED_WINE = modFluidTag("red_wine");
        public static final TagKey<Fluid> WHITE_WINE = modFluidTag("white_wine");
        public static final TagKey<Fluid> CURRANT_WINE = modFluidTag("currant_wine");
        public static final TagKey<Fluid> VERRUCA_WINE = modFluidTag("verruca_wine");
        public static final TagKey<Fluid> TWISTED_WINE = modFluidTag("twisted_wine");
        public static final TagKey<Fluid> VODKA = modFluidTag("vodka");

        public static final TagKey<Fluid> FLAXEN_CHEESE = modFluidTag("flaxen_cheese");
        public static final TagKey<Fluid> SCARLET_CHEESE = modFluidTag("scarlet_cheese");

        private static TagKey<Fluid> modFluidTag(String path) {
            return TagKey.create(Registries.FLUID, BrewinAndChewin.asResource(path));
        }
    }

    public static class Effects {
        public static final TagKey<MobEffect> MILK_BOTTLE_LOW_PRIORITY = modEffectTag("low_priority/milk_bottle");
        public static final TagKey<MobEffect> HOT_COCOA_LOW_PRIORITY = modEffectTag("low_priority/hot_cocoa");
        public static final TagKey<MobEffect> TWISTED_WINE_AFFLICTIONS = modEffectTag("twisted_wine_afflictions");
        public static final TagKey<MobEffect> OLD_WINE_EFFECTS = modEffectTag("old_wine_effects");

        private static TagKey<MobEffect> modEffectTag(String path) {
            return TagKey.create(Registries.MOB_EFFECT, BrewinAndChewin.asResource(path));
        }
    }

    public static class DamageTypes {
        public static final TagKey<DamageType> TRIGGERS_RAGING = modDamageTypeTag("triggers_raging");

        private static TagKey<DamageType> modDamageTypeTag(String path) {
            return TagKey.create(Registries.DAMAGE_TYPE, BrewinAndChewin.asResource(path));
        }
    }

}
