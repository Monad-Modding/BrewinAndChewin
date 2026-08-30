package umpaz.brewinandchewin.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import umpaz.brewinandchewin.common.registry.BnCItems;

public class PizzaBlock extends Block
{
    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 3);

    protected static final VoxelShape[] SHAPES = new VoxelShape[]{
            Block.box(0.0D, 0.0D, 0.0D, 8.0D, 2.0D, 8.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 8.0D),
            Shapes.or(Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 8.0D), Block.box(0.0D, 0.0D, 8.0D, 8.0D, 2.0D, 16.0D)),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
    };

    public PizzaBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SERVINGS, 3));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(SERVINGS)];
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            if (this.takeServing(level, pos, state, player, player.getUsedItemHand()).consumesAction()) {
                return InteractionResult.SUCCESS;
            }
        }

        return this.takeServing(level, pos, state, player, player.getUsedItemHand());
    }

    private InteractionResult takeServing(Level level, BlockPos pos, BlockState state, Player player, InteractionHand handIn) {
        int servings = state.getValue(SERVINGS);
        ItemStack heldStack = player.getItemInHand(handIn);
        if (heldStack.isEmpty() || heldStack.getItem().equals(BnCItems.PIZZA_SLICE)) {
            if (heldStack.getItem().equals(BnCItems.PIZZA_SLICE) && heldStack.getCount() < heldStack.getMaxStackSize()) {
                heldStack.setCount(heldStack.getCount() + 1);
            } else {
                player.setItemInHand(handIn, new ItemStack(BnCItems.PIZZA_SLICE));
            }
        }
        else {
            popResource(level, pos, new ItemStack(BnCItems.PIZZA_SLICE, 1));
        }
        level.playSound(null, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (level.getBlockState(pos).getValue(SERVINGS) == 0) {
            level.destroyBlock(pos, false);
        } else if (level.getBlockState(pos).getValue(SERVINGS) > 0) {
            level.setBlock(pos, state.setValue(SERVINGS, servings - 1), 3);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        return facing == Direction.DOWN && !this.canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, facing, facingState, level, pos, facingPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SERVINGS);
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        return blockState.getValue(SERVINGS);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}