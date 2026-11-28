package umpaz.brewinandchewin.common.registry;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import vectorwing.farmersdelight.common.FoodValues;

import java.util.List;

public class BnCFoods {
    private static FoodProperties drinkFood() {
        return new FoodProperties.Builder().alwaysEdible().build();
    }

    public static final FoodProperties BEER = drinkFood();
    public static final FoodProperties VODKA = drinkFood();
    public static final FoodProperties MEAD = drinkFood();
    public static final FoodProperties RICE_WINE = drinkFood();
    public static final FoodProperties PALE_JANE = drinkFood();
    public static final FoodProperties EGG_GROG = drinkFood();
    public static final FoodProperties GLITTERING_GRENADINE = drinkFood();
    public static final FoodProperties SACCHARINE_RUM = drinkFood();
    public static final FoodProperties SALTY_FOLLY = drinkFood();
    public static final FoodProperties BLOODY_MARY = drinkFood();
    public static final FoodProperties RED_RUM = drinkFood();
    public static final FoodProperties STRONGROOT_ALE = drinkFood();
    public static final FoodProperties STEEL_TOE_STOUT = drinkFood();
    public static final FoodProperties DREAD_NOG = drinkFood();
    public static final FoodProperties WITHERING_DROSS = drinkFood();
    public static final FoodProperties KOMBUCHA = drinkFood();

    public static final FoodProperties FLAXEN_CHEESE = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(1.0F).build();
    public static final FoodProperties SCARLET_CHEESE = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(1.0F).build();

    public static final FoodProperties VEGETABLE_OMELET = new FoodProperties.Builder()
            .nutrition(12).saturationModifier(0.8F).build();
    public static final FoodProperties CREAMY_ONION_SOUP = new FoodProperties.Builder()
            .nutrition(12).saturationModifier(0.8F).build();
    public static final FoodProperties CHEESY_PASTA = new FoodProperties.Builder()
            .nutrition(14).saturationModifier(0.75F).build();
    public static final FoodProperties HORROR_LASAGNA = new FoodProperties.Builder()
            .nutrition(16).saturationModifier(0.55F).build();
    public static final FoodProperties SCARLET_PIEROGI = new FoodProperties.Builder()
            .nutrition(12).saturationModifier(1.0F).build();
    public static final FoodProperties FIERY_FONDUE = new FoodProperties.Builder()
            .nutrition(14).saturationModifier(0.75F).build();

    public static final FoodProperties PIZZA_SLICE = new FoodProperties.Builder()
            .nutrition(5).saturationModifier(1.0F).build();
    public static final FoodProperties QUICHE_SLICE = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.8F).build();

    public static final FoodProperties HAM_AND_CHEESE_SANDWICH = new FoodProperties.Builder()
            .nutrition(9).saturationModifier(1.0F).build();

    public static final FoodProperties KIMCHI = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.6F).build();
    public static final FoodProperties JERKY = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.7F).build();
    public static final FoodProperties PICKLED_PICKLES = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.3F).build();
    public static final FoodProperties KIPPERS = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.5F).build();
    public static final FoodProperties COCOA_FUDGE = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.8F).build();

    public static final FoodProperties SWEET_BERRY_JAM = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.4F).build();
    public static final FoodProperties GLOW_BERRY_MARMALADE = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.4F).build();
    public static final FoodProperties APPLE_JELLY = new FoodProperties.Builder()
            .nutrition(10).saturationModifier(0.6F).build();

    public static class ConsumableDefs {
        private static Consumable drink(MobEffectInstance... effects) {
            return Consumables.defaultDrink()
                    .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(effects)))
                    .build();
        }

        private static Consumable foodWithEffect(float seconds, float probability, MobEffectInstance... effects) {
            return Consumables.defaultFood()
                    .consumeSeconds(seconds)
                    .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(effects), probability))
                    .build();
        }

        public static final Consumable FAST_FOOD = Consumables.defaultFood().consumeSeconds(0.8F).build();

        public static final Consumable JAM_JAR = Consumables.defaultDrink()
                .sound(SoundEvents.HONEY_DRINK)
                .build();

        public static final Consumable BEER = drink(
                new MobEffectInstance(BnCEffects.TIPSY, 2400, 0),
                new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, true, false)
        );
        public static final Consumable VODKA = drink(
                new MobEffectInstance(BnCEffects.TIPSY, 3600, 0),
                new MobEffectInstance(BnCEffects.INTOXICATION, 3000, 0, false, false)
        );
        public static final Consumable MEAD = drink(
                new MobEffectInstance(BnCEffects.TIPSY, 2400, 0),
                new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
                new MobEffectInstance(BnCEffects.SWEET_HEART, 2400, 0, false, false)
        );
        public static final Consumable RICE_WINE = drink(
                new MobEffectInstance(BnCEffects.TIPSY, 2400, 0),
                new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
                FoodValues.nourishment(1200)
        );
        public static final Consumable PALE_JANE = drink(
                new MobEffectInstance(BnCEffects.TIPSY, 3600, 0),
                new MobEffectInstance(BnCEffects.INTOXICATION, 3000, 0, false, false),
                FoodValues.nourishment(2400)
        );
        public static final Consumable EGG_GROG = drink(
                new MobEffectInstance(BnCEffects.TIPSY, 2400, 0),
                new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
                new MobEffectInstance(MobEffects.ABSORPTION, 600, 0)
        );
        public static final Consumable GLITTERING_GRENADINE = drink(
                new MobEffectInstance(BnCEffects.TIPSY, 2400, 0),
                new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
                new MobEffectInstance(MobEffects.GLOWING, 600, 0),
                new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0)
        );
        public static final Consumable SACCHARINE_RUM = drink(
                new MobEffectInstance(BnCEffects.TIPSY, 3600, 1),
                new MobEffectInstance(BnCEffects.INTOXICATION, 2400, 0, false, false),
                new MobEffectInstance(BnCEffects.SWEET_HEART, 3600, 0, false, false)
        );
        public static final Consumable SALTY_FOLLY = drink(
                new MobEffectInstance(BnCEffects.TIPSY, 3600, 1),
                new MobEffectInstance(BnCEffects.INTOXICATION, 3000, 0, false, false),
                new MobEffectInstance(MobEffects.WATER_BREATHING, 1800, 0)
        );
        public static final Consumable BLOODY_MARY = drink(
                new MobEffectInstance(BnCEffects.TIPSY, 2400, 1),
                new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
                new MobEffectInstance(BnCEffects.RAGING, 1200, 0)
        );
        public static final Consumable RED_RUM = drink(
                new MobEffectInstance(BnCEffects.TIPSY, 2400, 2),
                new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
                new MobEffectInstance(BnCEffects.RAGING, 2400, 0)
        );
        public static final Consumable STRONGROOT_ALE = drink(
                new MobEffectInstance(BnCEffects.TIPSY, 2400, 1),
                new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
                new MobEffectInstance(MobEffects.RESISTANCE, 600, 0)
        );
        public static final Consumable STEEL_TOE_STOUT = drink(
                new MobEffectInstance(BnCEffects.TIPSY, 2400, 2),
                new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
                new MobEffectInstance(MobEffects.RESISTANCE, 1200, 0)
        );
        public static final Consumable DREAD_NOG = drink(
                new MobEffectInstance(BnCEffects.TIPSY, 4800, 2),
                new MobEffectInstance(BnCEffects.INTOXICATION, 4200, 0, false, false),
                new MobEffectInstance(MobEffects.BAD_OMEN, 72000, 0)
        );
        public static final Consumable WITHERING_DROSS = drink(
                new MobEffectInstance(BnCEffects.TIPSY, 3600, 2),
                new MobEffectInstance(BnCEffects.INTOXICATION, 3000, 0, false, false),
                new MobEffectInstance(MobEffects.BLINDNESS, 200, 0),
                new MobEffectInstance(MobEffects.WEAKNESS, 3000, 0),
                new MobEffectInstance(MobEffects.SLOWNESS, 3000, 0),
                new MobEffectInstance(MobEffects.WITHER, 1200, 0)
        );
        public static final Consumable KOMBUCHA = drink(
                new MobEffectInstance(BnCEffects.TIPSY, 2400, 0),
                new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
                new MobEffectInstance(MobEffects.HASTE, 1200, 1)
        );

        public static final Consumable VEGETABLE_OMELET = foodWithEffect(1.6F, 1.0F, FoodValues.nourishment(3600));
        public static final Consumable CREAMY_ONION_SOUP = foodWithEffect(1.6F, 1.0F, FoodValues.nourishment(3600));
        public static final Consumable CHEESY_PASTA = foodWithEffect(1.6F, 1.0F, FoodValues.nourishment(6000));
        public static final Consumable HORROR_LASAGNA = foodWithEffect(1.6F, 1.0F, FoodValues.nourishment(6000));
        public static final Consumable SCARLET_PIEROGI = foodWithEffect(1.6F, 1.0F, FoodValues.nourishment(8400));
        public static final Consumable FIERY_FONDUE = foodWithEffect(1.6F, 1.0F, FoodValues.nourishment(8400));
        public static final Consumable COCOA_FUDGE = foodWithEffect(1.6F, 1.0F, new MobEffectInstance(MobEffects.SPEED, 800, 0, false, false));
    }
}
