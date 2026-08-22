package umpaz.brewinandchewin.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.utility.BnCShapeUtils;

public class GrapeStemBlock extends Block {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0D, 9.0D, 4.0D, 16.0D, 16.0D, 12.0D),
            Block.box(0.0D, 0.0D, 5.0D, 16.0D, 9.0D, 11.0D));
    private static final VoxelShape SHAPE_ROTATED = BnCShapeUtils.rotate(SHAPE, Direction.EAST);

    private final GrapeColour colour;

    public GrapeStemBlock(GrapeColour colour, Properties properties) {
        super(properties);
        this.colour = colour;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(AXIS, Direction.Axis.X));
    }

    public GrapeColour getColour() {
        return this.colour;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, AXIS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? SHAPE : SHAPE_ROTATED;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        Direction.Axis axis = state.getValue(AXIS);
        return side.getAxis() == axis && adjacentState.getBlock() instanceof GrapeStemBlock
                && adjacentState.getValue(AXIS) == axis;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        return RopeGrapeBlock.isAnyGrape(above)
                && above.getValue(RopeGrapeBlock.PART) == RopeGrapeBlock.GrapePart.STEM;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        return facing == Direction.UP && !this.canSurvive(state, level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, facing, facingState, level, pos, facingPos);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockPos stem = pos.above();
        BlockState above = level.getBlockState(stem);
        if (RopeGrapeBlock.isAnyGrape(above))
            level.setBlock(stem, RopeGrapeBlock.toRope(above), Block.UPDATE_ALL);
    }

    @Override
    public boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    public static Block of(GrapeColour colour) {
        return colour == GrapeColour.WHITE ? BnCBlocks.WHITE_GRAPE_STEM : BnCBlocks.RED_GRAPE_STEM;
    }
}
