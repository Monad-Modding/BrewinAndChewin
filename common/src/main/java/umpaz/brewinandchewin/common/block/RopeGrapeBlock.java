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
import net.minecraft.world.level.block.Blocks;
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
import vectorwing.farmersdelight.common.block.RopeBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;

import java.util.ArrayList;
import java.util.List;

public class RopeGrapeBlock extends Block implements SimpleWaterloggedBlock, BonemealableBlock {
    public static final EnumProperty<GrapePart> PART = EnumProperty.create("part", GrapePart.class);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public static final int MAX_AGE = 3;
    public static final int MAX_REACH = 2;
    public static final int SUPPORT_SCAN = 16;
    public static final int BONEMEAL_ADVANCES = 3;
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
        if (!level.isClientSide())
            level.scheduleTick(pos, this, 1);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!isSupported(level, pos, state) || !this.staysPlanar(level, pos, state))
            this.decayToRope(level, pos, state);
    }

    public boolean staysPlanar(BlockGetter level, BlockPos pos, BlockState state) {
        return tiesInPlane(level, pos, state.getValue(AXIS));
    }

    public void decayToRope(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(AGE) >= MAX_AGE)
            popResource(level, pos, new ItemStack(this.colour.getGrapes(), this.grapeCount(level.getRandom(), state)));
        this.breakToRope(level, pos, state);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        Direction.Axis axis = state.getValue(AXIS);
        if (side.getAxis() != axis || !isAnyGrape(adjacentState) || adjacentState.getValue(AXIS) != axis)
            return false;
        return wallSize(adjacentState) >= wallSize(state);
    }

    private static int wallSize(BlockState state) {
        return switch (state.getValue(PART)) {
            case STEM -> 2;
            case BIG -> 1;
            case SMALL -> 0;
        };
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return false;
    }

    public static boolean isRope(BlockState state) {
        return state.is(ModBlocks.ROPE.get());
    }

    public static boolean tiesInPlane(BlockGetter level, BlockPos pos, Direction.Axis axis) {
        for (Direction direction : Direction.Plane.HORIZONTAL)
            if (RopeBlock.tieToRopeAndWalls(level.getBlockState(pos.relative(direction))) != (direction.getAxis() == axis))
                return false;
        return true;
    }

    public static Direction.Axis planeOf(BlockGetter level, BlockPos pos) {
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z})
            if (tiesInPlane(level, pos, axis))
                return axis;
        return null;
    }

    public static Direction.Axis ropePlaneAt(BlockGetter level, BlockPos pos) {
        return isRope(level.getBlockState(pos)) ? planeOf(level, pos) : null;
    }

    public static Direction.Axis climbAxis(BlockGetter level, BlockPos pos, GrapeColour colour) {
        BlockState state = level.getBlockState(pos);
        if (isRope(state))
            return planeOf(level, pos);
        if (isAnyGrape(state) && state.getValue(PART) != GrapePart.STEM && of(colour) == state.getBlock())
            return state.getValue(AXIS);
        return null;
    }

    public static boolean canClimbInto(BlockGetter level, BlockPos bush, GrapeColour colour) {
        return climbAxis(level, bush.above(), colour) != null;
    }

    public BlockState fromRope(BlockState rope, Direction.Axis axis) {
        return this.defaultBlockState()
                .setValue(AXIS, axis)
                .setValue(WATERLOGGED, rope.getValue(WATERLOGGED));
    }

    public static BlockState toRope(BlockGetter level, BlockPos pos, BlockState grape) {
        BlockState rope = ModBlocks.ROPE.get().defaultBlockState()
                .setValue(WATERLOGGED, grape.getValue(WATERLOGGED))
                .setValue(RopeBlock.TIED_TO_BELL, level.getBlockState(pos.above()).is(Blocks.BELL));
        for (Direction direction : Direction.Plane.HORIZONTAL)
            rope = rope.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction),
                    RopeBlock.tieToRopeAndWalls(level.getBlockState(pos.relative(direction))));
        return rope;
    }

    public static void climb(ServerLevel level, BlockPos bush, GrapeColour colour) {
        BlockPos pos = bush.above();
        BlockState target = level.getBlockState(pos);
        Direction.Axis axis = climbAxis(level, pos, colour);
        if (axis == null)
            return;
        int age = isAnyGrape(target) ? target.getValue(AGE) : 0;
        RopeGrapeBlock block = of(colour);
        level.setBlock(pos, block.fromRope(target, axis)
                .setValue(PART, GrapePart.STEM).setValue(AGE, age), Block.UPDATE_ALL);
        level.setBlock(bush, GrapeStemBlock.of(colour).defaultBlockState()
                .setValue(GrapeStemBlock.AGE, age).setValue(GrapeStemBlock.AXIS, axis), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.VINE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static boolean isAnyGrape(BlockState state) {
        return state.getBlock() instanceof RopeGrapeBlock;
    }

    private static boolean isStem(BlockState state) {
        return isAnyGrape(state) && state.getValue(PART) == GrapePart.STEM;
    }

    private static List<Direction> along(Direction.Axis axis) {
        return axis == Direction.Axis.X
                ? List.of(Direction.EAST, Direction.WEST)
                : List.of(Direction.NORTH, Direction.SOUTH);
    }

    private static boolean nextToStem(BlockGetter level, BlockPos pos, Direction.Axis axis) {
        for (Direction direction : along(axis))
            if (isStem(level.getBlockState(pos.relative(direction))))
                return true;
        return false;
    }

    public static boolean isSupported(BlockGetter level, BlockPos pos, BlockState state) {
        if (state.getValue(PART) == GrapePart.STEM)
            return true;
        Direction.Axis axis = state.getValue(AXIS);
        for (Direction direction : along(axis)) {
            BlockPos walk = pos;
            for (int step = 0; step < SUPPORT_SCAN; ++step) {
                walk = walk.relative(direction);
                BlockState walked = level.getBlockState(walk);
                if (!isAnyGrape(walked) || walked.getValue(AXIS) != axis)
                    break;
                if (walked.getValue(PART) == GrapePart.STEM)
                    return true;
            }
        }
        return false;
    }

    private void setAge(Level level, BlockPos pos, BlockState state, int age) {
        level.setBlock(pos, state.setValue(AGE, age), Block.UPDATE_CLIENTS);
        if (state.getValue(PART) != GrapePart.STEM)
            return;
        BlockPos base = pos.below();
        BlockState baseState = level.getBlockState(base);
        if (baseState.getBlock() instanceof GrapeStemBlock)
            level.setBlock(base, baseState.setValue(GrapeStemBlock.AGE, age), Block.UPDATE_CLIENTS);
    }

    public void breakToRope(LevelAccessor level, BlockPos pos, BlockState state) {
        level.setBlock(pos, toRope(level, pos, state), Block.UPDATE_ALL);
        if (state.getValue(PART) != GrapePart.STEM)
            return;
        BlockPos base = pos.below();
        if (level.getBlockState(base).getBlock() instanceof GrapeStemBlock)
            level.removeBlock(base, false);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.getAbilities().instabuild) {
            if (state.getValue(AGE) >= MAX_AGE)
                popResource(level, pos, new ItemStack(this.colour.getGrapes(), this.grapeCount(level.getRandom(), state)));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (isAnyGrape(state))
            this.breakToRope(level, pos, state);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!isSupported(level, pos, state) || !this.staysPlanar(level, pos, state)) {
            this.decayToRope(level, pos, state);
            return;
        }

        GrapePart part = state.getValue(PART);
        Direction.Axis axis = state.getValue(AXIS);

        if (part == GrapePart.BIG && !nextToStem(level, pos, axis)) {
            if (random.nextFloat() < DEMOTE_CHANCE)
                level.setBlock(pos, state.setValue(PART, GrapePart.SMALL).setValue(AGE, 0), Block.UPDATE_ALL);
            return;
        }
        if (part == GrapePart.SMALL && nextToStem(level, pos, axis)) {
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

    private List<BlockPos> spreadTargets(BlockGetter level, BlockPos pos, BlockState state) {
        Direction.Axis axis = state.getValue(AXIS);
        List<BlockPos> targets = new ArrayList<>();
        BlockPos stem = findStem(level, pos, axis);
        if (stem == null)
            return targets;
        for (Direction direction : along(axis)) {
            BlockPos target = pos.relative(direction);
            if (Math.abs(distanceAlong(stem, target, axis)) <= MAX_REACH && ropePlaneAt(level, target) == axis)
                targets.add(target);
        }
        return targets;
    }

    private void spreadTo(ServerLevel level, BlockPos target, Direction.Axis axis) {
        if (ropePlaneAt(level, target) != axis)
            return;
        level.setBlock(target, this.fromRope(level.getBlockState(target), axis)
                .setValue(PART, GrapePart.SMALL).setValue(AGE, 0), Block.UPDATE_ALL);
    }

    private void spread(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        List<BlockPos> targets = spreadTargets(level, pos, state);
        if (!targets.isEmpty())
            spreadTo(level, targets.get(random.nextInt(targets.size())), state.getValue(AXIS));
    }

    private static BlockPos findStem(BlockGetter level, BlockPos pos, Direction.Axis axis) {
        Direction positive = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        for (int offset = -MAX_REACH; offset <= MAX_REACH; ++offset) {
            BlockPos other = pos.relative(positive, offset);
            if (isStem(level.getBlockState(other)))
                return other;
        }
        return null;
    }

    private static int distanceAlong(BlockPos from, BlockPos to, Direction.Axis axis) {
        return axis == Direction.Axis.X ? to.getX() - from.getX() : to.getZ() - from.getZ();
    }

    private boolean hasSpreadRoom(LevelReader level, BlockPos pos, BlockState state) {
        return !spreadTargets(level, pos, state).isEmpty();
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        if (canAdvanceSelf(level, pos, state))
            return true;
        if (state.getValue(PART) != GrapePart.SMALL && this.hasSpreadRoom(level, pos, state))
            return true;
        for (Direction direction : along(state.getValue(AXIS))) {
            BlockState neighbour = level.getBlockState(pos.relative(direction));
            if (isAnyGrape(neighbour) && neighbour.getValue(AGE) < MAX_AGE)
                return true;
        }
        return false;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        for (int advance = 0; advance < BONEMEAL_ADVANCES; ++advance) {
            BlockState current = level.getBlockState(pos);
            if (!current.is(this))
                return;
            List<Runnable> options = new ArrayList<>();
            if (canAdvanceSelf(level, pos, current))
                options.add(() -> this.advanceSelf(level, pos));
            Direction.Axis axis = current.getValue(AXIS);
            if (current.getValue(PART) != GrapePart.SMALL)
                for (BlockPos target : spreadTargets(level, pos, current))
                    options.add(() -> this.spreadTo(level, target, axis));
            for (Direction direction : along(axis)) {
                BlockPos neighbour = pos.relative(direction);
                BlockState neighbourState = level.getBlockState(neighbour);
                if (isAnyGrape(neighbourState) && neighbourState.getValue(AGE) < MAX_AGE)
                    options.add(() -> growGrape(level, neighbour));
            }
            if (options.isEmpty())
                return;
            options.get(random.nextInt(options.size())).run();
        }
    }

    private boolean canAdvanceSelf(BlockGetter level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < MAX_AGE
                || state.getValue(PART) == GrapePart.SMALL && nextToStem(level, pos, state.getValue(AXIS));
    }

    private void advanceSelf(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(this))
            return;
        if (state.getValue(PART) == GrapePart.SMALL && nextToStem(level, pos, state.getValue(AXIS))) {
            level.setBlock(pos, state.setValue(PART, GrapePart.BIG).setValue(AGE, 0), Block.UPDATE_ALL);
            return;
        }
        int age = state.getValue(AGE);
        if (age < MAX_AGE)
            this.setAge(level, pos, state, age + 1);
    }

    private static void growGrape(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RopeGrapeBlock grape))
            return;
        int age = state.getValue(AGE);
        if (age < MAX_AGE)
            grape.setAge(level, pos, state, age + 1);
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
