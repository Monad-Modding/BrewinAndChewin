package umpaz.brewinandchewin.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
import umpaz.brewinandchewin.common.utility.BnCTextUtils;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.function.Supplier;

public class CheeseWheelBlock extends Block {
    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 3);
    protected static final VoxelShape[] SHAPES = new VoxelShape[]{
            Block.box(2.0D, 0.0D, 2.0D, 8.0D, 6.0D, 8.0D),
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 6.0D, 8.0D),
            Shapes.or(Block.box(2.0D, 0.0D, 2.0D, 14.0D, 6.0D, 8.0D), Block.box(2.0D, 0.0D, 8.0D, 8.0D, 6.0D, 14.0D)),
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 6.0D, 14.0D),
    };

    public final Supplier<Item> cheeseWedgeType;

    public CheeseWheelBlock(Supplier<Item> cheeseWedgeType, Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SERVINGS, 3));
        this.cheeseWedgeType = cheeseWedgeType;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(SERVINGS)];
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
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        int servings = state.getValue(SERVINGS);
        if (stack.is(ModTags.Items.KNIVES)) {
            level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            popResource(level, pos, new ItemStack(cheeseWedgeType.get(), 1));
            if (servings > 0) {
                level.setBlock(pos, state.setValue(SERVINGS, servings - 1), 3);
            } else if (servings == 0) {
                level.destroyBlock(pos, false);
            }
            return ItemInteractionResult.SUCCESS;
        }
        player.displayClientMessage(BnCTextUtils.getTranslation("block.feast.use_knife"), true);
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
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

    // NeoForge:
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }
}