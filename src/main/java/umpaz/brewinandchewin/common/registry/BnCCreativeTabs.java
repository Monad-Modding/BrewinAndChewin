package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class BnCCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS;
    public static final RegistryObject<CreativeModeTab> TAB_BREWIN_AND_CHEWIN;

    public BnCCreativeTabs() {
    }

    static {
        CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "brewinandchewin");
        TAB_BREWIN_AND_CHEWIN = CREATIVE_TABS.register("brewinandchewin", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.brewinandchewin")).icon(() -> new ItemStack(BnCItems.KEG.get())).displayItems((parameters, output) -> {
            BnCItems.CREATIVE_TAB_ITEMS.forEach((item) -> {
                output.accept(item.get());
            });
        }).build());
    }
}
