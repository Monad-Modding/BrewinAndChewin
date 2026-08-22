package umpaz.brewinandchewin.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import umpaz.brewinandchewin.common.registry.BnCBlocks;

public class GrapeBushBlock extends CropBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    public static final int BUSH_MAX_AGE = 2;

    private static final VoxelShape[] SHAPES = {
            Block.box(5.0D, 0.0D, 5.0D, 11.0D, 5.0D, 11.0D),
            Block.box(5.0D, 0.0D, 5.0D, 11.0D, 10.0D, 11.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 13.0D, 12.0D)
    };

    private final GrapeColour colour;

    public GrapeBushBlock(GrapeColour colour, Properties properties) {
        super(properties);
        this.colour = colour;
    }

    public GrapeColour getColour() {
        return this.colour;
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return BUSH_MAX_AGE;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return GrapeColour.seedsOf(this.colour);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(AGE) < BUSH_MAX_AGE) {
            super.randomTick(state, level, pos, random);
            return;
        }
        if (RopeGrapeBlock.canClimbInto(level, pos) && random.nextFloat() < RopeGrapeBlock.GROW_CHANCE)
            RopeGrapeBlock.climb(level, pos, this.colour);
    }

    @Override
    public void growCrops(net.minecraft.world.level.Level level, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < BUSH_MAX_AGE) {
            super.growCrops(level, pos, state);
            return;
        }
        if (level instanceof ServerLevel serverLevel && RopeGrapeBlock.canClimbInto(level, pos))
            RopeGrapeBlock.climb(serverLevel, pos, this.colour);
    }

    @Override
    public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < BUSH_MAX_AGE || RopeGrapeBlock.canClimbInto(level, pos);
    }

    public static Block of(GrapeColour colour) {
        return colour == GrapeColour.WHITE ? BnCBlocks.WHITE_GRAPE_BUSH : BnCBlocks.RED_GRAPE_BUSH;
    }
}
