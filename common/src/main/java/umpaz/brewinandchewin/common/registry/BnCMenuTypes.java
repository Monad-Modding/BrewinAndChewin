package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.container.AgingCaskMenu;
import umpaz.brewinandchewin.common.block.entity.container.DistilleryMenu;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;

public class BnCMenuTypes {
    public static final MenuType<KegMenu> KEG = BrewinAndChewin.getHelper().createMenuType(KegMenu::new);
    public static final MenuType<DistilleryMenu> DISTILLERY = BrewinAndChewin.getHelper().createDistilleryMenuType(DistilleryMenu::new);
    public static final MenuType<AgingCaskMenu> AGING_CASK = BrewinAndChewin.getHelper().createAgingCaskMenuType(AgingCaskMenu::new);

    public static void registerAll() {
        Registry.register(BuiltInRegistries.MENU, BrewinAndChewin.asResource("keg"), KEG);
        Registry.register(BuiltInRegistries.MENU, BrewinAndChewin.asResource("distillery"), DISTILLERY);
        Registry.register(BuiltInRegistries.MENU, BrewinAndChewin.asResource("aging_cask"), AGING_CASK);
    }
}
