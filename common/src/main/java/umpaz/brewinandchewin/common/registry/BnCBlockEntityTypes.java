package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.AgingCaskBlockEntity;
import umpaz.brewinandchewin.common.block.entity.BottleRackBlockEntity;
import umpaz.brewinandchewin.common.block.entity.CoasterBlockEntity;
import umpaz.brewinandchewin.common.block.entity.DistilleryBlockEntity;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;

public class BnCBlockEntityTypes {
    public static final BlockEntityType<KegBlockEntity> KEG = BlockEntityType.Builder.of(BrewinAndChewin.getHelper().supplyBlockEntity(), BnCBlocks.KEG).build(null);
    public static final BlockEntityType<CoasterBlockEntity> COASTER = BlockEntityType.Builder.of(CoasterBlockEntity::new, BnCBlocks.COASTER).build(null);
    public static final BlockEntityType<AgingCaskBlockEntity> AGING_CASK = BlockEntityType.Builder.of(BrewinAndChewin.getHelper().supplyAgingCaskBlockEntity(), BnCBlocks.AGING_CASK).build(null);
    public static final BlockEntityType<BottleRackBlockEntity> BOTTLE_RACK = BlockEntityType.Builder.of(BottleRackBlockEntity::new, BnCBlocks.BOTTLE_RACK).build(null);
    public static final BlockEntityType<DistilleryBlockEntity> DISTILLERY = BlockEntityType.Builder.of(BrewinAndChewin.getHelper().supplyDistilleryBlockEntity(), BnCBlocks.DISTILLERY).build(null);

    public static void registerAll() {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, BrewinAndChewin.asResource("keg"), KEG);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, BrewinAndChewin.asResource("coaster"), COASTER);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, BrewinAndChewin.asResource("aging_cask"), AGING_CASK);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, BrewinAndChewin.asResource("bottle_rack"), BOTTLE_RACK);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, BrewinAndChewin.asResource("distillery"), DISTILLERY);
    }
}
