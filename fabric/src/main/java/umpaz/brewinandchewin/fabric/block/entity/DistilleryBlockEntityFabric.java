package umpaz.brewinandchewin.fabric.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.brewinandchewin.common.block.entity.DistilleryBlockEntity;

public class DistilleryBlockEntityFabric extends DistilleryBlockEntity implements ExtendedScreenHandlerFactory<BlockPos> {
    public DistilleryBlockEntityFabric(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.getBlockPos();
    }
}
