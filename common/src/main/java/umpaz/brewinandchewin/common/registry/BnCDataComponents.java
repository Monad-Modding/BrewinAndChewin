package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.item.component.LabelContents;
import umpaz.brewinandchewin.common.item.component.WineContents;

public class BnCDataComponents {
    public static final DataComponentType<WineContents> WINE_CONTENTS = DataComponentType.<WineContents>builder()
            .persistent(WineContents.CODEC)
            .networkSynchronized(WineContents.STREAM_CODEC)
            .build();

    public static final DataComponentType<LabelContents> LABEL = DataComponentType.<LabelContents>builder()
            .persistent(LabelContents.CODEC)
            .networkSynchronized(LabelContents.STREAM_CODEC)
            .build();

    public static void registerAll() {
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, BrewinAndChewin.asResource("wine_contents"), WINE_CONTENTS);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, BrewinAndChewin.asResource("label"), LABEL);
    }
}
