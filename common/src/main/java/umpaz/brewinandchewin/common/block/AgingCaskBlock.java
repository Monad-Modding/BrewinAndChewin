package umpaz.brewinandchewin.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.AgingCaskBlockEntity;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.utility.BnCShapeUtils;

public class AgingCaskBlock extends BaseEntityBlock {
    public static final MapCodec<AgingCaskBlock> CODEC = simpleCodec(AgingCaskBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2.0D, 0.0D, 0.0D, 14.0D, 16.0D, 16.0D),
            Block.box(14.0D, 2.0D, 0.0D, 16.0D, 14.0D, 16.0D),
            Block.box(0.0D, 2.0D, 0.0D, 2.0D, 14.0D, 16.0D),
            Block.box(7.0D, 3.0D, -2.0D, 9.0D, 6.0D, 0.0D));

    private static final VoxelShape[] SHAPES = buildShapes();

    private static VoxelShape[] buildShapes() {
        VoxelShape[] shapes = new VoxelShape[4];
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            shapes[facing.get2DDataValue()] = BnCShapeUtils.rotate(SHAPE, facing);
        }
        return shapes;
    }

    public AgingCaskBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OCCUPIED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).get2DDataValue()];
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return this.useWithoutItem(state, level, pos, player, hitResult) == InteractionResult.PASS
                ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof AgingCaskBlockEntity cask) {
            BrewinAndChewin.getHelper().openBlockPosMenu(player, cask, pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(OCCUPIED) || random.nextInt(6) != 0)
            return;
        if (!(level.getBlockEntity(pos) instanceof AgingCaskBlockEntity cask) || cask.countDistillates() == 0)
            return;
        level.addParticle(ParticleTypes.EFFECT,
                pos.getX() + 0.25D + random.nextDouble() * 0.5D,
                pos.getY() + 0.9D,
                pos.getZ() + 0.25D + random.nextDouble() * 0.5D,
                0.0D, 0.02D, 0.0D);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof AgingCaskBlockEntity cask) {
                Containers.dropContents(level, pos, cask.getDroppableInventory());
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof AgingCaskBlockEntity cask && !cask.getWine().isEmpty()) {
            return Math.min(15, 1 + cask.countDistillates() * 2);
        }
        return 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OCCUPIED);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BrewinAndChewin.getHelper().supplyAgingCaskBlockEntity().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide())
            return null;
        return createTickerHelper(type, BnCBlockEntityTypes.AGING_CASK, AgingCaskBlockEntity::agingTick);
    }
}
