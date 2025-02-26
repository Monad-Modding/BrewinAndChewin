package umpaz.brewinandchewin.common.registry;

import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.CoasterBlockEntity;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;

public class BnCBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BrewinAndChewin.MODID);

    public static final RegistryObject<BlockEntityType<KegBlockEntity>> KEG = TILES.register("keg",
            () -> BlockEntityType.Builder.of(KegBlockEntity::new, BnCBlocks.KEG.get()).build(Util.fetchChoiceType(References.BLOCK_ENTITY, BrewinAndChewin.asResource("keg").toString())));
    public static final RegistryObject<BlockEntityType<CoasterBlockEntity>> COASTER = TILES.register("coaster",
            () -> BlockEntityType.Builder.of(CoasterBlockEntity::new, BnCBlocks.COASTER.get()).build(Util.fetchChoiceType(References.BLOCK_ENTITY, BrewinAndChewin.asResource("coaster").toString())));
}
