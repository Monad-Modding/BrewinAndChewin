package umpaz.brewinandchewin.common.registry;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import vectorwing.farmersdelight.common.registry.ModEffects;

public class BnCFoods {
    public static final FoodProperties BEER = (new FoodProperties.Builder())
            .effect(new MobEffectInstance(BnCEffects.TIPSY, 2400, 0), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, true, false), 1.0F)
            .alwaysEdible()
            .build();
    public static final FoodProperties VODKA = (new FoodProperties.Builder())
            .effect(new MobEffectInstance(BnCEffects.TIPSY, 3600, 0), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.INTOXICATION, 3000, 0, false, false), 1.0F)
            .alwaysEdible()
            .build();
    public static final FoodProperties MEAD = (new FoodProperties.Builder())
            .effect(new MobEffectInstance(BnCEffects.TIPSY, 2400, 0), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.SWEET_HEART, 2400, 0, false, false), 1.0F)
            .alwaysEdible()
            .build();
    public static final FoodProperties RICE_WINE = (new FoodProperties.Builder())
            .effect(new MobEffectInstance(BnCEffects.TIPSY, 2400, 0), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT, 1200, 0), 1.0F)
            .alwaysEdible()
            .build();
    public static final FoodProperties PALE_JANE = (new FoodProperties.Builder())
            .effect(new MobEffectInstance(BnCEffects.TIPSY, 3600, 0), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.INTOXICATION, 3000, 0, false, false), 1.0F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT, 2400, 0, false, false), 1.0F)
            .alwaysEdible()
            .build();
    public static final FoodProperties EGG_GROG = (new FoodProperties.Builder())
            .effect(new MobEffectInstance(BnCEffects.TIPSY, 2400, 0), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
            .effect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 0), 1.0F)
            .alwaysEdible()
            .build();
    public static final FoodProperties GLITTERING_GRENADINE = (new FoodProperties.Builder())
            .effect(new MobEffectInstance(BnCEffects.TIPSY, 2400, 0), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
            .effect(new MobEffectInstance(MobEffects.GLOWING, 600, 0), 1.0F)
            .effect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0), 1.0F)
            .alwaysEdible()
            .build();
    public static final FoodProperties SACCHARINE_RUM = (new FoodProperties.Builder())
            .effect(new MobEffectInstance(BnCEffects.TIPSY, 3600, 1), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.INTOXICATION, 2400, 0, false, false), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.SWEET_HEART, 3600, 0, false, false), 1.0F)
            .alwaysEdible()
            .build();
    public static final FoodProperties SALTY_FOLLY = (new FoodProperties.Builder())
            .effect(new MobEffectInstance(BnCEffects.TIPSY, 3600, 1), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.INTOXICATION, 3000, 0, false, false), 1.0F)
            .effect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1800, 0), 1.0F)
            .alwaysEdible()
            .build();
    public static final FoodProperties BLOODY_MARY = (new FoodProperties.Builder())
            .effect(new MobEffectInstance(BnCEffects.TIPSY, 2400, 1), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.RAGING, 1200, 0), 1.0F)
            .alwaysEdible()
            .build();
    public static final FoodProperties RED_RUM = (new FoodProperties.Builder())
            .effect(new MobEffectInstance(BnCEffects.TIPSY, 2400, 2), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.RAGING, 2400, 0), 1.0F)
            .alwaysEdible()
            .build();
    public static final FoodProperties STRONGROOT_ALE = (new FoodProperties.Builder())
            .effect(new MobEffectInstance(BnCEffects.TIPSY, 2400, 1), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
            .effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0), 1.0F)
            .alwaysEdible()
            .build();
    public static final FoodProperties STEEL_TOE_STOUT = (new FoodProperties.Builder())
            .effect(new MobEffectInstance(BnCEffects.TIPSY, 2400, 2), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
            .effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 0), 1.0F)
            .alwaysEdible()
            .build();
    public static final FoodProperties DREAD_NOG = (new FoodProperties.Builder())
            .effect(new MobEffectInstance(BnCEffects.TIPSY, 4800, 2), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.INTOXICATION, 4200, 0, false, false), 1.0F)
            .effect(new MobEffectInstance(MobEffects.BAD_OMEN, 72000, 0), 1.0F)
            .alwaysEdible()
            .build();
    public static final FoodProperties WITHERING_DROSS = (new FoodProperties.Builder())
            .effect(new MobEffectInstance(BnCEffects.TIPSY, 3600, 2), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.INTOXICATION, 3000, 0, false, false), 1.0F)
            .effect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0), 1.0F)
            .effect(new MobEffectInstance(MobEffects.WEAKNESS, 3000, 0), 1.0F)
            .effect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3000, 0), 1.0F)
            .effect(new MobEffectInstance(MobEffects.WITHER, 1200, 0), 1.0F)
            .alwaysEdible()
            .build();

    // TODO: Give this more attention after the Farmer's Respite rework.
    public static final FoodProperties KOMBUCHA = (new FoodProperties.Builder())
            .effect(new MobEffectInstance(BnCEffects.TIPSY, 2400, 0), 1.0F)
            .effect(new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
            .effect(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 1), 1.0F)
            .alwaysEdible()
            .build();

    public static final FoodProperties FLAXEN_CHEESE = (new FoodProperties.Builder())
            .nutrition(4).saturationModifier(1.0F).build();
    public static final FoodProperties SCARLET_CHEESE = (new FoodProperties.Builder())
            .nutrition(4).saturationModifier(1.0F).build();

    public static final FoodProperties VEGETABLE_OMELET = (new FoodProperties.Builder())
            .nutrition(12).saturationModifier(0.8F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT, 3600, 0, false, false), 1.0F).build();
    public static final FoodProperties CREAMY_ONION_SOUP = (new FoodProperties.Builder())
            .nutrition(12).saturationModifier(0.8F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT, 3600, 0, false, false), 1.0F).build();
    public static final FoodProperties CHEESY_PASTA = (new FoodProperties.Builder())
            .nutrition(14).saturationModifier(0.75F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT, 6000, 0, false, false), 1.0F).build();
    public static final FoodProperties HORROR_LASAGNA = (new FoodProperties.Builder())
            .nutrition(16).saturationModifier(0.55F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT, 6000, 0, false, false), 1.0F).build();
    public static final FoodProperties SCARLET_PIEROGI = (new FoodProperties.Builder())
            .nutrition(12).saturationModifier(1.0F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT, 8400, 0, false, false), 1.0F).build();
    public static final FoodProperties FIERY_FONDUE = (new FoodProperties.Builder())
            .nutrition(14).saturationModifier(0.75f)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT, 8400, 0, false, false), 1.0F).build();

    public static final FoodProperties PIZZA_SLICE = (new FoodProperties.Builder())
            .nutrition(5).saturationModifier(1.0F).build();
    public static final FoodProperties QUICHE_SLICE = (new FoodProperties.Builder())
            .nutrition(4).saturationModifier(0.8F).fast().build();

    public static final FoodProperties HAM_AND_CHEESE_SANDWICH = (new FoodProperties.Builder())
            .nutrition(9).saturationModifier(1.0F).build();

    public static final FoodProperties KIMCHI = (new FoodProperties.Builder())
            .nutrition(2).saturationModifier(0.6F).build();
    public static final FoodProperties JERKY = (new FoodProperties.Builder())
            .nutrition(3).saturationModifier(0.7F).fast().build();
    public static final FoodProperties PICKLED_PICKLES = (new FoodProperties.Builder())
            .nutrition(4).saturationModifier(0.3F).build();
    public static final FoodProperties KIPPERS = (new FoodProperties.Builder())
            .nutrition(6).saturationModifier(0.5F).build();
    public static final FoodProperties COCOA_FUDGE = (new FoodProperties.Builder())
            .nutrition(4).saturationModifier(0.8F)
            .effect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 800, 0, false, false), 1.0F).build();

    public static final FoodProperties SWEET_BERRY_JAM = (new FoodProperties.Builder())
            .nutrition(6).saturationModifier(0.4F).build();
    public static final FoodProperties GLOW_BERRY_MARMALADE = (new FoodProperties.Builder())
            .nutrition(6).saturationModifier(0.4F).build();
    public static final FoodProperties APPLE_JELLY = (new FoodProperties.Builder())
            .nutrition(10).saturationModifier(0.6F).build();
}
