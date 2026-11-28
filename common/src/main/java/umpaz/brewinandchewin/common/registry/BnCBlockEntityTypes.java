package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.CoasterBlockEntity;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;

import java.util.Set;

public class BnCBlockEntityTypes {
    public static final BlockEntityType<KegBlockEntity> KEG = new BlockEntityType<>(BrewinAndChewin.getHelper().supplyBlockEntity(), Set.of(BnCBlocks.KEG));
    public static final BlockEntityType<CoasterBlockEntity> COASTER = new BlockEntityType<>(CoasterBlockEntity::new, Set.of(BnCBlocks.COASTER));

    public static void registerAll() {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, BrewinAndChewin.asResource("keg"), KEG);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, BrewinAndChewin.asResource("coaster"), COASTER);
    }
}
