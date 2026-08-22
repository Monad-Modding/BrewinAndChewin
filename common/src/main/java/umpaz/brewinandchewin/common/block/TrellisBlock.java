package umpaz.brewinandchewin.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.utility.BnCShapeUtils;

import java.util.ArrayList;
import java.util.List;

public class TrellisBlock extends Block implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<TrellisPart> PART = EnumProperty.create("part", TrellisPart.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;



    private static final VoxelShape NORTH_SHAPE = Block.box(0.0D, 0.0D, 7.0D, 16.0D, 16.0D, 9.0D);
    private static final VoxelShape[] SHAPES = buildShapes();

    public TrellisBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, TrellisPart.SINGLE)
                .setValue(WATERLOGGED, false));
    }

    private static VoxelShape[] buildShapes() {
        VoxelShape[] shapes = new VoxelShape[4];
        for (Direction facing : Direction.Plane.HORIZONTAL)
            shapes[facing.get2DDataValue()] = BnCShapeUtils.rotate(NORTH_SHAPE, facing);
        return shapes;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).get2DDataValue()];
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        Direction facing = clicked.getAxis().isHorizontal() ? clicked : context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER)
                .setValue(PART, getPart(context.getLevel(), context.getClickedPos(), facing));
    }


    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED))
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        if (direction.getAxis().isVertical())
            return state.setValue(PART, getPart(level, pos, state.getValue(FACING)));
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    protected static TrellisPart getPart(LevelReader level, BlockPos pos, Direction facing) {
        boolean above = connectsTo(level.getBlockState(pos.above()), facing);
        boolean below = connectsTo(level.getBlockState(pos.below()), facing);
        if (above && below)
            return TrellisPart.MIDDLE;
        if (above)
            return TrellisPart.BOTTOM;
        if (below)
            return TrellisPart.TOP;
        return TrellisPart.SINGLE;
    }

    protected static boolean connectsTo(BlockState state, Direction facing) {
        return state.getBlock() instanceof TrellisBlock && state.getValue(FACING) == facing;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    public enum TrellisPart implements StringRepresentable {
        SINGLE("single"),
        BOTTOM("bottom"),
        MIDDLE("middle"),
        TOP("top");

        private final String name;

        TrellisPart(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
