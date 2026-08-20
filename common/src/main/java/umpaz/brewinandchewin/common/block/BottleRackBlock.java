package umpaz.brewinandchewin.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.block.entity.BottleRackBlockEntity;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.item.WineItem;
import umpaz.brewinandchewin.common.tag.BnCTags;

import java.util.Optional;
import java.util.OptionalInt;

public class BottleRackBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<BottleRackBlock> CODEC = simpleCodec(BottleRackBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LARGE = BooleanProperty.create("large");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final int ROWS = 3;
    public static final int COLUMNS = 3;

    private static final VoxelShape SHAPE_LARGE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
    private static final VoxelShape SHAPE_WEST = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_EAST = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 16.0D);

    public BottleRackBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LARGE, false)
                .setValue(WATERLOGGED, false));
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
        if (state.getValue(LARGE))
            return SHAPE_LARGE;
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BottleRackBlockEntity rack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!player.getAbilities().mayBuild)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (stack.is(BnCItems.BOTTLE_RACK) && !state.getValue(LARGE) && hitResult.getDirection() == state.getValue(FACING)) {
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(pos, state.setValue(LARGE, true));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!(stack.getItem() instanceof WineItem))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        OptionalInt slot = getHitSlot(hitResult, state);
        if (slot.isEmpty() || !rack.getItem(slot.getAsInt()).isEmpty())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (!level.isClientSide()) {
            rack.setItem(slot.getAsInt(), stack.copyWithCount(1));
            if (!player.getAbilities().instabuild)
                stack.shrink(1);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BottleRackBlockEntity rack))
            return InteractionResult.PASS;

        OptionalInt slot = getHitSlot(hitResult, state);
        if (slot.isEmpty())
            return InteractionResult.PASS;
        ItemStack bottle = rack.getItem(slot.getAsInt());
        if (bottle.isEmpty())
            return InteractionResult.PASS;

        if (!level.isClientSide()) {
            rack.setItem(slot.getAsInt(), ItemStack.EMPTY);
            if (!player.getInventory().add(bottle))
                player.drop(bottle, false);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static OptionalInt getHitSlot(BlockHitResult hitResult, BlockState state) {
        Optional<Vec2> coordinates = getRelativeHitCoordinates(hitResult, state.getValue(FACING));
        if (coordinates.isEmpty())
            return OptionalInt.empty();
        Vec2 hit = coordinates.get();
        int row = hit.y >= 0.666F ? 0 : (hit.y >= 0.333F ? 1 : 2);
        int column = hit.x >= 0.666F ? 2 : (hit.x >= 0.333F ? 1 : 0);
        return OptionalInt.of(column + row * COLUMNS);
    }

    private static Optional<Vec2> getRelativeHitCoordinates(BlockHitResult hitResult, Direction facing) {
        Direction hitFace = hitResult.getDirection();
        if (facing != hitFace)
            return Optional.empty();
        BlockPos relative = hitResult.getBlockPos().relative(hitFace);
        Vec3 location = hitResult.getLocation().subtract(relative.getX(), relative.getY(), relative.getZ());
        double x = location.x();
        double y = location.y();
        double z = location.z();
        return switch (hitFace) {
            case NORTH -> Optional.of(new Vec2((float) (1.0D - x), (float) y));
            case SOUTH -> Optional.of(new Vec2((float) x, (float) y));
            case WEST -> Optional.of(new Vec2((float) z, (float) y));
            case EAST -> Optional.of(new Vec2((float) (1.0D - z), (float) y));
            default -> Optional.empty();
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof BottleRackBlockEntity rack) {
                Containers.dropContents(level, pos, rack.getItems());
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
        if (level.getBlockEntity(pos) instanceof BottleRackBlockEntity rack) {
            return rack.countFilledSlots() * 15 / BottleRackBlockEntity.SLOT_COUNT;
        }
        return 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LARGE, WATERLOGGED);
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
        return new BottleRackBlockEntity(pos, state);
    }
}
