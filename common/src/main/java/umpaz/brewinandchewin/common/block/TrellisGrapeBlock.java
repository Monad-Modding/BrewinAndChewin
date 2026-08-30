package umpaz.brewinandchewin.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import umpaz.brewinandchewin.common.registry.BnCBlocks;

import java.util.ArrayList;
import java.util.List;

public class TrellisGrapeBlock extends TrellisBlock implements BonemealableBlock {
    public static final EnumProperty<GrapeColour> VINE_FRONT = EnumProperty.create("vine_front", GrapeColour.class);
    public static final EnumProperty<GrapeColour> VINE_BACK = EnumProperty.create("vine_back", GrapeColour.class);
    public static final IntegerProperty AGE_FRONT = IntegerProperty.create("age_front", 0, 3);
    public static final IntegerProperty AGE_BACK = IntegerProperty.create("age_back", 0, 3);

    public static final int MAX_AGE = 3;
    public static final float GROW_CHANCE = 0.2F;
    public static final float SPREAD_CHANCE = 0.08F;
    public static final float VINE_HARDNESS = 0.2F;
    public static final int BONEMEAL_ADVANCES = 3;

    public TrellisGrapeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(VINE_FRONT, GrapeColour.NONE)
                .setValue(VINE_BACK, GrapeColour.NONE)
                .setValue(AGE_FRONT, 0)
                .setValue(AGE_BACK, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(VINE_FRONT, VINE_BACK, AGE_FRONT, AGE_BACK);
    }

    public static EnumProperty<GrapeColour> vineOf(boolean front) {
        return front ? VINE_FRONT : VINE_BACK;
    }

    public static IntegerProperty ageOf(boolean front) {
        return front ? AGE_FRONT : AGE_BACK;
    }

    public static boolean isFront(BlockState state, Direction hitFace) {
        return hitFace != Direction.get(Direction.AxisDirection.NEGATIVE, state.getValue(AXIS));
    }

    public static BlockState fromTrellis(BlockState trellis) {
        return BnCBlocks.TRELLIS_GRAPE.defaultBlockState()
                .setValue(AXIS, trellis.getValue(AXIS))
                .setValue(PART, trellis.getValue(PART))
                .setValue(WATERLOGGED, trellis.getValue(WATERLOGGED));
    }

    public static BlockState toTrellis(BlockState grape) {
        return BnCBlocks.TRELLIS.defaultBlockState()
                .setValue(AXIS, grape.getValue(AXIS))
                .setValue(PART, grape.getValue(PART))
                .setValue(WATERLOGGED, grape.getValue(WATERLOGGED));
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        GrapeColour planted = GrapeColour.fromSeed(stack.getItem());
        if (planted == GrapeColour.NONE)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        boolean front = isFront(state, hitResult.getDirection());
        if (state.getValue(vineOf(front)) != GrapeColour.NONE)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(vineOf(front), planted).setValue(ageOf(front), 0), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild)
                stack.shrink(1);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        boolean front = isFront(state, hitResult.getDirection());
        GrapeColour colour = state.getValue(vineOf(front));
        if (colour == GrapeColour.NONE || state.getValue(ageOf(front)) < MAX_AGE)
            return InteractionResult.PASS;
        if (!level.isClientSide()) {
            popResource(level, pos, new ItemStack(colour.getGrapes()));
            level.setBlock(pos, state.setValue(ageOf(front), 0), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (state.getValue(vineOf(sideFacingPlayer(state, pos, player))) == GrapeColour.NONE)
            return super.getDestroyProgress(state, player, level, pos);
        return player.getDestroySpeed(state) / VINE_HARDNESS / 100.0F;
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        return hasVine(state) ? SoundType.VINE : super.getSoundType(state);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
        if (!hasVine(state)) return state;
        boolean front = sideFacingPlayer(state, pos, player);
        if (state.getValue(vineOf(front)) != GrapeColour.NONE) {
            if (!level.isClientSide())
                dropVine(state, level, pos, player, front);
            return state.setValue(vineOf(front), GrapeColour.NONE).setValue(ageOf(front), 0);
        }
        if (!level.isClientSide())
            dropVine(state, level, pos, player, !front);
        return toTrellis(state);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (!state.is(this)) return;
        level.setBlock(pos, hasVine(state) ? state : toTrellis(state), Block.UPDATE_ALL);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static boolean sideFacingPlayer(BlockState state, BlockPos pos, Player player) {
        return (state.getValue(AXIS) == Direction.Axis.X
                ? player.getX() - (pos.getX() + 0.5D)
                : player.getZ() - (pos.getZ() + 0.5D)) > 0.0D;
    }

    public static boolean hasVine(BlockState state) {
        return state.getValue(VINE_FRONT) != GrapeColour.NONE || state.getValue(VINE_BACK) != GrapeColour.NONE;
    }

    private static void dropVine(BlockState state, Level level, BlockPos pos, Player player, boolean front) {
        GrapeColour colour = state.getValue(vineOf(front));
        if (colour == GrapeColour.NONE || player.getAbilities().instabuild)
            return;
        if (state.getValue(ageOf(front)) >= MAX_AGE)
            popResource(level, pos, new ItemStack(colour.getGrapes()));
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        for (boolean front : new boolean[]{true, false}) {
            GrapeColour colour = state.getValue(vineOf(front));
            if (colour == GrapeColour.NONE)
                continue;
            int age = state.getValue(ageOf(front));
            if (age < MAX_AGE) {
                if (random.nextFloat() < GROW_CHANCE) {
                    state = state.setValue(ageOf(front), age + 1);
                    level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                }
            } else if (random.nextFloat() < SPREAD_CHANCE) {
                spread(level, pos, state.getValue(AXIS), colour, front, random);
            }
        }
    }

    private static void spread(ServerLevel level, BlockPos pos, Direction.Axis axis, GrapeColour colour, boolean front, RandomSource random) {
        List<BlockPos> targets = new ArrayList<>();
        for (BlockPos next : neighbours(pos, axis)) {
            BlockState state = level.getBlockState(next);
            if (!(state.getBlock() instanceof TrellisBlock) || state.getValue(AXIS) != axis)
                continue;
            if (state.getBlock() instanceof TrellisGrapeBlock && state.getValue(vineOf(front)) != GrapeColour.NONE)
                continue;
            targets.add(next);
        }
        if (targets.isEmpty())
            return;
        BlockPos target = targets.get(random.nextInt(targets.size()));
        BlockState existing = level.getBlockState(target);
        BlockState grown = (existing.getBlock() instanceof TrellisGrapeBlock ? existing : fromTrellis(existing))
                .setValue(vineOf(front), colour).setValue(ageOf(front), 0);
        level.setBlock(target, grown, Block.UPDATE_ALL);
    }

    private static List<BlockPos> neighbours(BlockPos pos, Direction.Axis axis) {
        Direction along = axis == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
        return List.of(pos.relative(along), pos.relative(along.getOpposite()), pos.above(), pos.below());
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        for (boolean front : new boolean[]{true, false}) {
            if (state.getValue(vineOf(front)) == GrapeColour.NONE)
                continue;
            if (state.getValue(ageOf(front)) < MAX_AGE)
                return true;
            Direction.Axis axis = state.getValue(AXIS);
            for (BlockPos next : neighbours(pos, axis)) {
                BlockState neighbour = level.getBlockState(next);
                if (!(neighbour.getBlock() instanceof TrellisBlock) || neighbour.getValue(AXIS) != axis)
                    continue;
                if (!(neighbour.getBlock() instanceof TrellisGrapeBlock) || neighbour.getValue(vineOf(front)) == GrapeColour.NONE)
                    return true;
                if (neighbour.getValue(ageOf(front)) < MAX_AGE)
                    return true;
            }
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
            if (!(current.getBlock() instanceof TrellisGrapeBlock))
                return;
            List<Runnable> options = new ArrayList<>();
            for (boolean front : new boolean[]{true, false})
                collectAdvances(level, pos, current, front, options);
            if (options.isEmpty())
                return;
            options.get(random.nextInt(options.size())).run();
        }
    }

    private static void collectAdvances(ServerLevel level, BlockPos pos, BlockState state, boolean front, List<Runnable> options) {
        GrapeColour colour = state.getValue(vineOf(front));
        if (colour == GrapeColour.NONE)
            return;
        if (state.getValue(ageOf(front)) < MAX_AGE)
            options.add(() -> growVine(level, pos, front));
        Direction.Axis axis = state.getValue(AXIS);
        for (BlockPos next : neighbours(pos, axis)) {
            BlockState neighbour = level.getBlockState(next);
            if (!(neighbour.getBlock() instanceof TrellisBlock) || neighbour.getValue(AXIS) != axis)
                continue;
            if (!(neighbour.getBlock() instanceof TrellisGrapeBlock) || neighbour.getValue(vineOf(front)) == GrapeColour.NONE)
                options.add(() -> plantVine(level, next, front, colour));
            else if (neighbour.getValue(ageOf(front)) < MAX_AGE)
                options.add(() -> growVine(level, next, front));
        }
    }

    private static void growVine(ServerLevel level, BlockPos pos, boolean front) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof TrellisGrapeBlock) || state.getValue(vineOf(front)) == GrapeColour.NONE)
            return;
        int age = state.getValue(ageOf(front));
        if (age < MAX_AGE)
            level.setBlock(pos, state.setValue(ageOf(front), age + 1), Block.UPDATE_CLIENTS);
    }

    private static void plantVine(ServerLevel level, BlockPos pos, boolean front, GrapeColour colour) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof TrellisBlock))
            return;
        if (state.getBlock() instanceof TrellisGrapeBlock && state.getValue(vineOf(front)) != GrapeColour.NONE)
            return;
        BlockState grown = (state.getBlock() instanceof TrellisGrapeBlock ? state : fromTrellis(state))
                .setValue(vineOf(front), colour).setValue(ageOf(front), 0);
        level.setBlock(pos, grown, Block.UPDATE_ALL);
    }

}
