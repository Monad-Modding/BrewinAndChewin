package umpaz.brewinandchewin.fabric.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;

public class KegBlockEntityFabric extends KegBlockEntity implements ExtendedScreenHandlerFactory {
    public KegBlockEntityFabric(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer serverPlayer, FriendlyByteBuf friendlyByteBuf) {

    }
}