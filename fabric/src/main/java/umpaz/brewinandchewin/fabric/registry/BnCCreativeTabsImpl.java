package umpaz.brewinandchewin.fabric.registry;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.common.registry.BnCCreativeTabs;
import umpaz.brewinandchewin.common.registry.BnCItems;

public class BnCCreativeTabsImpl {
    public static void init() {
        BnCCreativeTabs.TAB_BREWIN_AND_CHEWIN = FabricCreativeModeTab.builder().title(Component.translatable("itemGroup.brewinandchewin"))
                .icon(() -> new ItemStack(BnCItems.KEG))
                .displayItems((parameters, output) -> BnCItems.CREATIVE_TAB_ITEMS.forEach(output::accept))
                .build();
    }
}
