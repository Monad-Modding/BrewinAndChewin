package umpaz.brewinandchewin.common.compat.nomansland;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import umpaz.brewinandchewin.common.registry.BnCFoods;
import umpaz.brewinandchewin.common.registry.BnCItems;
import vectorwing.farmersdelight.common.item.ConsumableItem;

public class BnCNMLItems {
    public static final Item RICE_PUDDING = new ConsumableItem(new Item.Properties().stacksTo(16).craftRemainder(Items.BOWL).food(BnCFoods.RICE_PUDDING), true);
    public static final Item MAPLE_FUDGE = new ConsumableItem(new Item.Properties().food(BnCFoods.MAPLE_FUDGE));

    public static void registerAll() {
        BnCItems.registerWithTab("rice_pudding", RICE_PUDDING);
        BnCItems.registerWithTab("maple_fudge", MAPLE_FUDGE);
    }
}
