package umpaz.brewinandchewin.fabric.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.function.Supplier;

public abstract class BnCFluidFabric extends FlowingFluid {
    @Override
    public boolean isSame(Fluid fluidIn) {
        return fluidIn == getSource() || fluidIn == getFlowing();
    }

    @Override
    protected boolean canConvertToSource(ServerLevel level) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        Block.dropResources(state, level, pos, blockEntity);
    }

    @Override
    protected int getSlopeFindDistance(LevelReader levelReader) {
        return 4;
    }

    @Override
    protected int getDropOff(LevelReader levelReader) {
        return 1;
    }

    @Override
    public Item getBucket() {
        return Items.AIR;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !isSame(fluid);
    }

    @Override
    public int getTickDelay(LevelReader levelReader) {
        return 5;
    }

    @Override
    protected float getExplosionResistance() {
        return 1;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState fluidState) {
        return Blocks.AIR.defaultBlockState();
    }

    public static class Source extends BnCFluidFabric {
        private final Supplier<FlowingFluid> flowing;

        public Source(Supplier<FlowingFluid> flowing) {
            this.flowing = flowing;
        }

        @Override
        public Fluid getFlowing() {
            return flowing.get();
        }

        @Override
        public Fluid getSource() {
            return this;
        }

        public int getAmount(FluidState fluidState) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState fluidState) {
            return true;
        }
    }

    public static class Flowing extends BnCFluidFabric {
        private final Supplier<FlowingFluid> source;

        public Flowing(Supplier<FlowingFluid> source) {
            this.source = source;
        }

        @Override
        public Fluid getFlowing() {
            return this;
        }

        @Override
        public Fluid getSource() {
            return source.get();
        }

        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        public int getAmount(FluidState fluidState) {
            return fluidState.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState fluidState) {
            return false;
        }
    }
}
