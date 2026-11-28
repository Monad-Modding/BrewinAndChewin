package umpaz.brewinandchewin.fabric.block.entity;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;

public class KegBlockEntityFabric extends KegBlockEntity implements ExtendedMenuProvider<BlockPos> {
    public KegBlockEntityFabric(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return getBlockPos();
    }
}