package umpaz.brewinandchewin.common.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import vectorwing.farmersdelight.common.item.ConsumableItem;

public class JamJarItem extends ConsumableItem {
    public JamJarItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack pStack) {
        return ItemUseAnimation.DRINK;
    }
}
