package umpaz.brewinandchewin.common.registry;

import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.*;

import java.util.function.Supplier;

public class BnCBlockEntityTypes {
    //*
    public static final LazyRegistrar<BlockEntityType<?>> TILES = LazyRegistrar.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BrewinAndChewin.MODID);


    public static final Supplier<BlockEntityType<KegBlockEntity>> KEG = TILES.register("keg",
            () -> FabricBlockEntityTypeBuilder.create(KegBlockEntity::new, BnCBlocks.KEG).build());
    public static final Supplier<BlockEntityType<CoasterBlockEntity>> COASTER = TILES.register("coaster",
            () -> FabricBlockEntityTypeBuilder.create(CoasterBlockEntity::new, BnCBlocks.COASTER).build(null));

    public BnCBlockEntityTypes() {
    }

     //*/
}
