package umpaz.brewinandchewin.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import umpaz.brewinandchewin.common.utility.BnCShapeUtils;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.function.Supplier;

public class SlicedCakeBlock extends Block {
    public static final int MAX_BITES = 6;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, MAX_BITES - 1);

    private final Supplier<Item> slice;
    private final VoxelShape[][] shapes;

    public SlicedCakeBlock(Properties properties, Supplier<Item> slice, double minX, double maxX, double height, double minZ, double maxZ) {
        super(properties);
        this.slice = slice;
        this.shapes = buildShapes(minX, maxX, height, minZ, maxZ);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BITES, 0));
    }

    private static VoxelShape[][] buildShapes(double minX, double maxX, double height, double minZ, double maxZ) {
        VoxelShape[][] result = new VoxelShape[MAX_BITES][4];
        double step = (maxZ - minZ) / MAX_BITES;
        for (int bites = 0; bites < MAX_BITES; ++bites) {
            VoxelShape base = Block.box(minX, 0.0D, minZ + step * bites, maxX, height, maxZ);
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                result[bites][facing.get2DDataValue()] = BnCShapeUtils.rotate(base, facing);
            }
        }
        return result;
    }

    public ItemStack getSliceItem() {
        return new ItemStack(this.slice.get());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shapes[state.getValue(BITES)][state.getValue(FACING).get2DDataValue()];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(ModTags.Items.KNIVES)) {
            if (!level.isClientSide()) {
                popResource(level, pos, this.getSliceItem());
                level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 0.8F, 0.8F);
                this.removeBite(level, pos, state);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return this.eatBite(level, pos, state, player);
    }

    private InteractionResult eatBite(Level level, BlockPos pos, BlockState state, Player player) {
        FoodProperties food = this.getSliceItem().get(DataComponents.FOOD);
        if (food == null || !player.canEat(food.canAlwaysEat()))
            return InteractionResult.PASS;

        if (!level.isClientSide()) {
            player.getFoodData().eat(food);
            for (FoodProperties.PossibleEffect possible : food.effects()) {
                if (level.getRandom().nextFloat() < possible.probability())
                    player.addEffect(possible.effect());
            }
            level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.8F, 0.8F);
            this.removeBite(level, pos, state);
            player.awardStat(Stats.EAT_CAKE_SLICE);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void removeBite(Level level, BlockPos pos, BlockState state) {
        int bites = state.getValue(BITES);
        if (bites < MAX_BITES - 1) {
            level.setBlock(pos, state.setValue(BITES, bites + 1), Block.UPDATE_ALL);
        } else {
            level.removeBlock(pos, false);
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.levelEvent(2001, pos, Block.getId(state));
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        return facing == Direction.DOWN && !this.canSurvive(state, level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, facing, facingState, level, pos, facingPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BITES);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return MAX_BITES - state.getValue(BITES);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
