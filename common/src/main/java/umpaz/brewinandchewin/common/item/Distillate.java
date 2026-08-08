package umpaz.brewinandchewin.common.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface Distillate {
    default List<MobEffectInstance> getDistillateEffects(ItemStack stack) {
        return List.of();
    }

    default int getTipsyBonus(ItemStack stack) {
        return 0;
    }

    default boolean isSickening(ItemStack stack) {
        return false;
    }

    default boolean isDelicious(ItemStack stack) {
        return false;
    }
}
