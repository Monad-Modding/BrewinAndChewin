package umpaz.brewinandchewin;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.platform.BnCPlatformHelper;

public class BrewinAndChewin {
    public static final String MODID = "brewinandchewin";
    public static final Logger LOG = LoggerFactory.getLogger("Brewin' And Chewin'");
    private static BnCPlatformHelper helper;

    @ApiStatus.Internal
    public static boolean isClient = false;

    public static BnCPlatformHelper getHelper() {
        return helper;
    }

    public static void init(BnCPlatformHelper helper) {
        BrewinAndChewin.helper = helper;
        BnCConfiguration.init();
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MODID, path);
    }
}
