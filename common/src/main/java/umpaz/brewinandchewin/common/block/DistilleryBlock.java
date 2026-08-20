package umpaz.brewinandchewin.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.DistilleryBlockEntity;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.utility.BnCShapeUtils;

public class DistilleryBlock extends BaseEntityBlock {
    public static final MapCodec<DistilleryBlock> CODEC = simpleCodec(DistilleryBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DistilleryPart> PART = EnumProperty.create("part", DistilleryPart.class);
    public static final BooleanProperty MIRRORED = BooleanProperty.create("mirrored");
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 5.0D, 16.0D),
            Block.box(2.0D, 5.0D, 2.0D, 14.0D, 15.0D, 14.0D),
            Block.box(5.0D, 15.0D, 5.0D, 11.0D, 25.0D, 11.0D),
            Block.box(4.0D, 21.0D, 4.0D, 12.0D, 23.0D, 12.0D),
            Block.box(-9.0D, 23.0D, 7.0D, 5.0D, 25.0D, 9.0D),
            Block.box(-9.0D, 16.0D, 7.0D, -7.0D, 23.0D, 9.0D),
            Block.box(-11.0D, 4.0D, 5.0D, -5.0D, 16.0D, 11.0D),
            Block.box(-12.0D, 0.0D, 4.0D, -4.0D, 4.0D, 12.0D));

    private static final VoxelShape[] SHAPES = buildShapes();

    public DistilleryBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, DistilleryPart.BURNER)
                .setValue(MIRRORED, false)
                .setValue(LIT, false));
    }

    private static VoxelShape[] buildShapes() {
        DistilleryPart[] parts = DistilleryPart.values();
        VoxelShape[] shapes = new VoxelShape[4 * 2 * parts.length];
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (int mirrored = 0; mirrored < 2; ++mirrored) {
                VoxelShape oriented = BnCShapeUtils.rotate(mirrored == 1 ? BnCShapeUtils.mirrorX(SHAPE) : SHAPE, facing);
                Direction condenser = condenserDirection(facing, mirrored == 1);
                for (DistilleryPart part : parts) {
                    BlockPos offset = part.offsetFromBurner(condenser);
                    shapes[shapeIndex(facing, mirrored == 1, part)] =
                            oriented.move(-offset.getX(), -offset.getY(), -offset.getZ());
                }
            }
        }
        return shapes;
    }

    private static int shapeIndex(Direction facing, boolean mirrored, DistilleryPart part) {
        return (facing.get2DDataValue() * 2 + (mirrored ? 1 : 0)) * DistilleryPart.values().length + part.ordinal();
    }

    public static Direction condenserDirection(Direction facing, boolean mirrored) {
        return mirrored ? facing.getClockWise() : facing.getCounterClockWise();
    }

    public static Direction getCondenserDirection(BlockState state) {
        return condenserDirection(state.getValue(FACING), state.getValue(MIRRORED));
    }

    public static BlockPos getBurnerPos(BlockPos pos, BlockState state) {
        return pos.subtract(state.getValue(PART).offsetFromBurner(getCondenserDirection(state)));
    }

    public static void setLit(Level level, BlockPos pos, BlockState state, boolean lit) {
        Direction condenser = getCondenserDirection(state);
        BlockPos burnerPos = getBurnerPos(pos, state);
        for (DistilleryPart part : DistilleryPart.values()) {
            BlockPos partPos = burnerPos.offset(part.offsetFromBurner(condenser));
            BlockState partState = level.getBlockState(partPos);
            if (partState.is(state.getBlock()) && partState.getValue(PART) == part && partState.getValue(LIT) != lit)
                level.setBlock(partPos, partState.setValue(LIT, lit), Block.UPDATE_ALL);
        }
    }

    @Nullable
    public static DistilleryBlockEntity getBurner(BlockGetter level, BlockPos pos, BlockState state) {
        return level.getBlockEntity(getBurnerPos(pos, state)) instanceof DistilleryBlockEntity distillery ? distillery : null;
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
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[shapeIndex(state.getValue(FACING), state.getValue(MIRRORED), state.getValue(PART))];
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
        DistilleryBlockEntity distillery = getBurner(level, pos, state);
        if (distillery == null)
            return InteractionResult.PASS;
        BrewinAndChewin.getHelper().openBlockPosMenu(player, distillery, getBurnerPos(pos, state));
        return InteractionResult.CONSUME;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        boolean preferred = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
        if (context.getClickedPos().getY() >= context.getLevel().getMaxBuildHeight() - 1)
            return null;

        for (boolean mirrored : new boolean[]{preferred, !preferred}) {
            if (!hasRoomFor(context, facing, mirrored))
                continue;
            return this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(MIRRORED, mirrored)
                    .setValue(PART, DistilleryPart.BURNER);
        }
        return null;
    }

    private static boolean hasRoomFor(BlockPlaceContext context, Direction facing, boolean mirrored) {
        Direction condenser = condenserDirection(facing, mirrored);
        for (DistilleryPart part : DistilleryPart.values()) {
            if (part.isBurner())
                continue;
            BlockPos partPos = context.getClickedPos().offset(part.offsetFromBurner(condenser));
            if (!context.getLevel().getBlockState(partPos).canBeReplaced(context))
                return false;
        }
        return true;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide())
            return;
        Direction condenser = getCondenserDirection(state);
        for (DistilleryPart part : DistilleryPart.values()) {
            if (part.isBurner())
                continue;
            level.setBlock(pos.offset(part.offsetFromBurner(condenser)), state.setValue(PART, part), Block.UPDATE_CLIENTS);
        }
        level.blockUpdated(pos, Blocks.AIR);
        state.updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return isStructureIntact(state, level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    private static boolean isStructureIntact(BlockState state, LevelAccessor level, BlockPos pos) {
        Direction condenser = getCondenserDirection(state);
        BlockPos burnerPos = getBurnerPos(pos, state);
        for (DistilleryPart part : DistilleryPart.values()) {
            if (part == state.getValue(PART))
                continue;
            BlockState other = level.getBlockState(burnerPos.offset(part.offsetFromBurner(condenser)));
            if (!other.is(state.getBlock()) || other.getValue(PART) != part
                    || other.getValue(FACING) != state.getValue(FACING)
                    || other.getValue(MIRRORED) != state.getValue(MIRRORED))
                return false;
        }
        return true;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.isCreative()) {
            Direction condenser = getCondenserDirection(state);
            BlockPos burnerPos = getBurnerPos(pos, state);
            for (DistilleryPart part : DistilleryPart.values()) {
                if (part == state.getValue(PART))
                    continue;
                BlockPos partPos = burnerPos.offset(part.offsetFromBurner(condenser));
                BlockState other = level.getBlockState(partPos);
                if (other.is(this) && other.getValue(PART) == part) {
                    level.setBlock(partPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_ALL);
                    level.levelEvent(player, 2001, partPos, Block.getId(other));
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof DistilleryBlockEntity distillery) {
                Containers.dropContents(level, pos, distillery.getDroppableInventory());
                distillery.awardExperience(Vec3.atCenterOf(pos));
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(BnCItems.DISTILLERY);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT))
            return;
        DistilleryPart part = state.getValue(PART);
        if (part == DistilleryPart.BURNER) {
            if (random.nextInt(10) == 0) {
                level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.6F, 1.0F, false);
            }
            Direction facing = state.getValue(FACING);
            level.addParticle(ParticleTypes.SMOKE,
                    pos.getX() + 0.5D + facing.getStepX() * 0.52D + (random.nextDouble() - 0.5D) * 0.2D,
                    pos.getY() + 0.1D + random.nextDouble() * 0.2D,
                    pos.getZ() + 0.5D + facing.getStepZ() * 0.52D + (random.nextDouble() - 0.5D) * 0.2D,
                    0.0D, 0.0D, 0.0D);
        } else if (part == DistilleryPart.CONDENSER_TOP && random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.WHITE_SMOKE,
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.3D,
                    pos.getY() + 0.1D,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.3D,
                    0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        DistilleryBlockEntity distillery = getBurner(level, pos, state);
        return distillery == null ? 0 : AbstractContainerMenu.getRedstoneSignalFromContainer(distillery);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, MIRRORED, LIT);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING))).setValue(MIRRORED, !state.getValue(MIRRORED));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART).isBurner()
                ? BrewinAndChewin.getHelper().supplyDistilleryBlockEntity().create(pos, state)
                : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || !state.getValue(PART).isBurner())
            return null;
        return createTickerHelper(type, BnCBlockEntityTypes.DISTILLERY, DistilleryBlockEntity::distillingTick);
    }
}
