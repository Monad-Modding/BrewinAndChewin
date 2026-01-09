package umpaz.brewinandchewin.fabric.client.integration;

import squeek.appleskin.api.event.FoodValuesEvent;
import squeek.appleskin.api.food.FoodValues;
import umpaz.brewinandchewin.common.registry.BnCEffects;

public class IntoxicationAppleSkinCompatFabric {
    public static void init() {
        FoodValuesEvent.EVENT.register(event -> {
            if (event.player != null && event.player.hasEffect(BnCEffects.INTOXICATION.value())) {
                // override saturation for HUD display
                event.modifiedFoodValues = new FoodValues(
                        event.modifiedFoodValues.hunger,
                        0.0F // zero saturation while intoxicated
                );
            }
        });
    }
}
