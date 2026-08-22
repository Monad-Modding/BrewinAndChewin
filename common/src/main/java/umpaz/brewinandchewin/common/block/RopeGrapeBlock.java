package umpaz.brewinandchewin.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.utility.BnCShapeUtils;
import vectorwing.farmersdelight.common.registry.ModBlocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RopeGrapeBlock extends Block implements SimpleWaterloggedBlock, BonemealableBlock {
    public static final EnumProperty<GrapePart> PART = EnumProperty.create("part", GrapePart.class);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public static final int MAX_AGE = 3;
    public static final int MAX_REACH = 2;
    public static final float GROW_CHANCE = 0.2F;
    public static final float SPREAD_CHANCE = 0.1F;
    public static final float DEMOTE_CHANCE = 0.2F;
    public static final float BIG_SECOND_GRAPE_CHANCE = 0.5F;

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape LEAVES_SHAPE = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 12.0D);
    private static final VoxelShape LEAVES_SHAPE_ROTATED = BnCShapeUtils.rotate(LEAVES_SHAPE, Direction.EAST);

    private final GrapeColour colour;

    public RopeGrapeBlock(GrapeColour colour, Properties properties) {
        super(properties);
        this.colour = colour;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PART, GrapePart.STEM)
                .setValue(AGE, 0)
                .setValue(AXIS, Direction.Axis.X)
                .setValue(WATERLOGGED, false));
    }

    public GrapeColour getColour() {
        return this.colour;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, AGE, AXIS, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? LEAVES_SHAPE : LEAVES_SHAPE_ROTATED;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !state.getValue(WATERLOGGED);
    }

    @Override
    public boolean isPathfindable(BlockState state, PathComputationType type) {
        return true;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED))
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return state;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbourBlock, BlockPos neighbourPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighbourBlock, neighbourPos, movedByPiston);
        if (level.isClientSide())
            return;
        Direction.Axis axis = state.getValue(AXIS);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (direction.getAxis() == axis)
                continue;
            BlockPos neighbour = pos.relative(direction);
            BlockState neighbourState = level.getBlockState(neighbour);
            BooleanProperty facingUs = PipeBlock.PROPERTY_BY_DIRECTION.get(direction.getOpposite());
            if (isRope(neighbourState) && neighbourState.getValue(facingUs))
                level.setBlock(neighbour, neighbourState.setValue(facingUs, false), Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return false;
    }

    public static boolean isRope(BlockState state) {
        return state.is(ModBlocks.ROPE.get());
    }

    public static boolean isSinglePlaneRope(BlockState state) {
        if (!isRope(state))
            return false;
        boolean alongZ = state.getValue(BlockStateProperties.NORTH);
        boolean alongX = state.getValue(BlockStateProperties.EAST);
        if (alongZ != state.getValue(BlockStateProperties.SOUTH) || alongX != state.getValue(BlockStateProperties.WEST))
            return false;
        return alongZ != alongX;
    }

    public static Direction.Axis ropeAxis(BlockState rope) {
        return rope.getValue(BlockStateProperties.NORTH) ? Direction.Axis.Z : Direction.Axis.X;
    }

    public static boolean canClimbInto(LevelReader level, BlockPos bush) {
        return isSinglePlaneRope(level.getBlockState(bush.above()));
    }

    public BlockState fromRope(BlockState rope) {
        return this.defaultBlockState()
                .setValue(AXIS, ropeAxis(rope))
                .setValue(WATERLOGGED, rope.getValue(WATERLOGGED));
    }

    public static BlockState toRope(BlockState grape) {
        Direction.Axis axis = grape.getValue(AXIS);
        BlockState rope = ModBlocks.ROPE.get().defaultBlockState().setValue(WATERLOGGED, grape.getValue(WATERLOGGED));
        for (Direction direction : Direction.Plane.HORIZONTAL)
            rope = rope.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), direction.getAxis() == axis);
        return rope;
    }

    public static void climb(ServerLevel level, BlockPos bush, GrapeColour colour) {
        BlockPos pos = bush.above();
        BlockState rope = level.getBlockState(pos);
        if (!isSinglePlaneRope(rope))
            return;
        Direction.Axis axis = ropeAxis(rope);
        RopeGrapeBlock block = of(colour);
        level.setBlock(pos, block.fromRope(rope)
                .setValue(PART, GrapePart.STEM).setValue(AGE, 0).setValue(AXIS, axis), Block.UPDATE_ALL);
        level.setBlock(bush, GrapeStemBlock.of(colour).defaultBlockState()
                .setValue(GrapeStemBlock.AGE, 0).setValue(GrapeStemBlock.AXIS, axis), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.VINE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public boolean isGrape(BlockState state) {
        return state.is(this);
    }

    private boolean isStem(BlockState state) {
        return this.isGrape(state) && state.getValue(PART) == GrapePart.STEM;
    }

    private static List<Direction> along(Direction.Axis axis) {
        return axis == Direction.Axis.X
                ? List.of(Direction.EAST, Direction.WEST)
                : List.of(Direction.NORTH, Direction.SOUTH);
    }

    private boolean nextToStem(BlockGetter level, BlockPos pos, Direction.Axis axis) {
        for (Direction direction : along(axis))
            if (this.isStem(level.getBlockState(pos.relative(direction))))
                return true;
        return false;
    }

    public boolean isSupported(BlockGetter level, BlockPos pos, BlockState state) {
        Direction.Axis axis = state.getValue(AXIS);
        if (state.getValue(PART) == GrapePart.STEM)
            return true;
        for (Direction direction : along(axis)) {
            BlockPos neighbour = pos.relative(direction);
            BlockState neighbourState = level.getBlockState(neighbour);
            if (!this.isGrape(neighbourState))
                continue;
            if (neighbourState.getValue(PART) == GrapePart.STEM || this.nextToStem(level, neighbour, axis))
                return true;
        }
        return false;
    }

    private void setAge(Level level, BlockPos pos, BlockState state, int age) {
        level.setBlock(pos, state.setValue(AGE, age), Block.UPDATE_CLIENTS);
        if (state.getValue(PART) != GrapePart.STEM)
            return;
        BlockPos base = pos.below();
        BlockState baseState = level.getBlockState(base);
        if (baseState.is(GrapeStemBlock.of(this.colour)))
            level.setBlock(base, baseState.setValue(GrapeStemBlock.AGE, age), Block.UPDATE_CLIENTS);
    }

    public void breakToRope(LevelAccessor level, BlockPos pos, BlockState state) {
        level.setBlock(pos, toRope(state), Block.UPDATE_ALL);
        if (state.getValue(PART) != GrapePart.STEM)
            return;
        BlockPos base = pos.below();
        if (level.getBlockState(base).is(GrapeStemBlock.of(this.colour)))
            level.removeBlock(base, false);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.getAbilities().instabuild) {
            popResource(level, pos, new ItemStack(GrapeColour.seedsOf(this.colour)));
            if (state.getValue(AGE) >= MAX_AGE)
                popResource(level, pos, new ItemStack(this.colour.getGrapes(), this.grapeCount(level.getRandom(), state)));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (this.isGrape(state))
            this.breakToRope(level, pos, state);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.isSupported(level, pos, state)) {
            this.breakToRope(level, pos, state);
            return;
        }

        GrapePart part = state.getValue(PART);
        Direction.Axis axis = state.getValue(AXIS);

        if (part == GrapePart.STEM)
            this.restoreBase(level, pos, state);

        if (part == GrapePart.BIG && !this.nextToStem(level, pos, axis)) {
            if (random.nextFloat() < DEMOTE_CHANCE)
                level.setBlock(pos, state.setValue(PART, GrapePart.SMALL).setValue(AGE, 0), Block.UPDATE_ALL);
            return;
        }
        if (part == GrapePart.SMALL && this.nextToStem(level, pos, axis)) {
            if (random.nextFloat() < GROW_CHANCE)
                level.setBlock(pos, state.setValue(PART, GrapePart.BIG).setValue(AGE, 0), Block.UPDATE_ALL);
            return;
        }
        int age = state.getValue(AGE);
        if (age < MAX_AGE) {
            if (random.nextFloat() < GROW_CHANCE)
                this.setAge(level, pos, state, age + 1);
            return;
        }
        if (part != GrapePart.SMALL && random.nextFloat() < SPREAD_CHANCE)
            this.spread(level, pos, state, random);
    }

    private void restoreBase(ServerLevel level, BlockPos pos, BlockState state) {
        BlockPos base = pos.below();
        BlockState baseState = level.getBlockState(base);
        Block stem = GrapeStemBlock.of(this.colour);
        if (baseState.is(stem) || !baseState.canBeReplaced())
            return;
        level.setBlock(base, stem.defaultBlockState()
                .setValue(GrapeStemBlock.AGE, state.getValue(AGE))
                .setValue(GrapeStemBlock.AXIS, state.getValue(AXIS)), Block.UPDATE_ALL);
    }

    private void spread(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        Direction.Axis axis = state.getValue(AXIS);
        BlockPos stem = this.findStem(level, pos, axis);
        if (stem == null)
            return;
        List<Direction> directions = new ArrayList<>(along(axis));
        if (random.nextBoolean())
            Collections.reverse(directions);
        for (Direction direction : directions) {
            BlockPos target = pos.relative(direction);
            if (Math.abs(distanceAlong(stem, target, axis)) > MAX_REACH)
                continue;
            BlockState rope = level.getBlockState(target);
            if (!isSinglePlaneRope(rope) || ropeAxis(rope) != axis)
                continue;
            level.setBlock(target, this.fromRope(rope)
                    .setValue(PART, GrapePart.SMALL).setValue(AGE, 0).setValue(AXIS, axis), Block.UPDATE_ALL);
            return;
        }
    }

    private BlockPos findStem(BlockGetter level, BlockPos pos, Direction.Axis axis) {
        Direction positive = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        for (int offset = -MAX_REACH; offset <= MAX_REACH; ++offset) {
            BlockPos other = pos.relative(positive, offset);
            if (this.isStem(level.getBlockState(other)))
                return other;
        }
        return null;
    }

    private static int distanceAlong(BlockPos from, BlockPos to, Direction.Axis axis) {
        return axis == Direction.Axis.X ? to.getX() - from.getX() : to.getZ() - from.getZ();
    }

    private boolean hasSpreadRoom(LevelReader level, BlockPos pos, BlockState state) {
        Direction.Axis axis = state.getValue(AXIS);
        BlockPos stem = this.findStem(level, pos, axis);
        if (stem == null)
            return false;
        for (Direction direction : along(axis)) {
            BlockPos target = pos.relative(direction);
            BlockState rope = level.getBlockState(target);
            if (Math.abs(distanceAlong(stem, target, axis)) <= MAX_REACH
                    && isSinglePlaneRope(rope) && ropeAxis(rope) == axis)
                return true;
        }
        return false;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        GrapePart part = state.getValue(PART);
        Direction.Axis axis = state.getValue(AXIS);
        if (part == GrapePart.SMALL)
            return state.getValue(AGE) < MAX_AGE || this.nextToStem(level, pos, axis);
        return state.getValue(AGE) < MAX_AGE || this.hasSpreadRoom(level, pos, state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < MAX_AGE) {
            this.setAge(level, pos, state, Math.min(MAX_AGE, age + 1 + random.nextInt(2)));
            return;
        }
        if (state.getValue(PART) == GrapePart.SMALL) {
            if (this.nextToStem(level, pos, state.getValue(AXIS)))
                level.setBlock(pos, state.setValue(PART, GrapePart.BIG).setValue(AGE, 0), Block.UPDATE_ALL);
            return;
        }
        this.spread(level, pos, state, random);
    }

    private int grapeCount(RandomSource random, BlockState state) {
        return switch (state.getValue(PART)) {
            case STEM -> 2;
            case BIG -> random.nextFloat() < BIG_SECOND_GRAPE_CHANCE ? 2 : 1;
            case SMALL -> 1;
        };
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (state.getValue(AGE) < MAX_AGE)
            return InteractionResult.PASS;
        if (!level.isClientSide()) {
            popResource(level, pos, new ItemStack(this.colour.getGrapes(), this.grapeCount(level.getRandom(), state)));
            this.setAge(level, pos, state, 0);
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static RopeGrapeBlock of(GrapeColour colour) {
        return (RopeGrapeBlock) (colour == GrapeColour.WHITE ? BnCBlocks.WHITE_ROPE_GRAPE : BnCBlocks.RED_ROPE_GRAPE);
    }

    public enum GrapePart implements StringRepresentable {
        STEM("stem"),
        SMALL("small"),
        BIG("big");

        private final String name;

        GrapePart(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
