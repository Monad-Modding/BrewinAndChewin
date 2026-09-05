package umpaz.brewinandchewin.neoforge.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.neoforge.item.TankardItem;

public class BnCItemsNeoForge {
    public static void applyOverrides() {
        ResourceLocation tankardId = BrewinAndChewin.asResource("tankard");
        Item neoforgeTankard = new TankardItem(new Item.Properties());
        Registry.register(BuiltInRegistries.ITEM, tankardId, neoforgeTankard);
    }


}
