package umpaz.brewinandchewin.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.particle.DrunkBubbleParticleOptions;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.utility.BnCMathUtils;

import java.util.List;
import java.util.Optional;

public class KegBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    private static final float FERMENTING_SOUND_CHANCE = 0.08F; //see line 137 for info ~Oska
    public static final MapCodec<KegBlock> CODEC = simpleCodec(KegBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty VERTICAL = BooleanProperty.create("vertical");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;


    protected static final VoxelShape SHAPE_X = Block.box(1.0D, 0.0D, 0.0D, 15.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_Z = Block.box(0.0D, 0.0D, 1.0D, 16.0D, 16.0D, 15.0D);
    protected static final VoxelShape SHAPE_VERTICAL = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    public KegBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(VERTICAL, false).setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity tileEntity = level.getBlockEntity(pos);
        if (level.isClientSide())
            return ItemInteractionResult.SUCCESS;
        if (tileEntity instanceof KegBlockEntity kegBE) {
            List<ItemStack> itms = stack.isEmpty() ? List.of() : kegBE.extractInWorld(stack, 1, player.getAbilities().instabuild);
            if (!itms.isEmpty()) {
                itms.forEach(itm -> {
                    if (!ItemStack.isSameItemSameComponents(itm, stack)) {
                        if (stack.isEmpty()) {
                            player.setItemInHand(hand, itm);
                        } else if (!player.getInventory().add(itm)) {
                            player.drop(itm, false);
                        }
                    }
                });
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1, 1);
                return ItemInteractionResult.SUCCESS;
            }
            kegBE.updateTemperature();
            BrewinAndChewin.getHelper().openKegMenu(player, kegBE, pos);
        }
        return ItemInteractionResult.SUCCESS;
    }


    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof KegBlockEntity keg) || !keg.isFermenting())
            return;

        if (random.nextInt(15) == 0) {
            Direction facing = state.getValue(FACING);
            boolean vertical = state.getValue(VERTICAL);

            // Base pixel offsets, all values are divided by 16 to match with block size.
            double xOffset = 8 / 16.0; // X = 8 px
            double yOffset = vertical ? -1 / 16.0 : 2 / 16.0; // Y = -1 px if vertical, else 2 px, particles spawn for some reason 2 px higher than desired
            double zOffset = vertical ? 0 / 16.0 : -1 / 16.0; // Z = 0 px if vertical, else -1 px, a little offsetted up front to make the particles appear visually more.

            // Rotate offsets based on facing
            Vec3 offset = switch (facing) {
                case NORTH -> new Vec3(xOffset, yOffset, zOffset);
                case SOUTH -> new Vec3(xOffset, yOffset, 1.0 - zOffset);
                case WEST -> new Vec3(zOffset, yOffset, xOffset);
                case EAST -> new Vec3(1.0 - zOffset, yOffset, xOffset);
                default -> Vec3.ZERO;
            };

            // Slight randomisation for natural bubbling
            double dx = (random.nextDouble() - 0.5) * 0.02;
            double dy = random.nextDouble() * 0.02;
            double dz = (random.nextDouble() - 0.5) * 0.02;

            level.addParticle(new DrunkBubbleParticleOptions(new Vector3f(0.8784f, 0.5725f, 0.1921f), 0.25f),
                    pos.getX() + offset.x + dx,
                    pos.getY() + offset.y + dy,
                    pos.getZ() + offset.z + dz,
                    0.0, 0.02, 0.0);
// 31/08/2026 Oska: From playtesting it may appear that the fermenting sound appears to be less common than before these changes. Values have been tweaked. The Sound is changed to BUBBLE_COLUMN_BUBBLE_POP to prevent confusion with when a keg finishes brewing playing sound BREWING_STAND_BREW, SEE → KeBlockEntity.java → processfermenting → line 421.
// P.S playtest the sound event once more using BELL_BLOCK to see if the frequency still requires tweaking
            if (random.nextFloat() < FERMENTING_SOUND_CHANCE)
                level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS,
                        0.4F, 0.7F + random.nextFloat() * 0.2F, false);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(VERTICAL)) {
            return SHAPE_VERTICAL;
        }
        if ((state.getValue(FACING) == Direction.NORTH || state.getValue(FACING) == Direction.SOUTH)) {
            return SHAPE_X;
        }
        return SHAPE_Z;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        FluidState fluid = level.getFluidState(context.getClickedPos());

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(VERTICAL, context.getNearestLookingVerticalDirection() == Direction.UP || context.getNearestLookingDirection() == Direction.DOWN)
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        Optional<KegBlockEntity> kegBE = level.getBlockEntity(pos, BnCBlockEntityTypes.KEG);
        kegBE.ifPresent(blockEntity -> stack.applyComponents(blockEntity.collectComponents()));
        return stack;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity tileEntity = level.getBlockEntity(pos);
            if (tileEntity instanceof KegBlockEntity kegEntity) {
                Containers.dropContents(level, pos, kegEntity.getDroppableInventory());
                kegEntity.getUsedRecipesAndPopExperience(level, Vec3.atCenterOf(pos));
                level.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, VERTICAL, WATERLOGGED);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        BlockEntity tileEntity = level.getBlockEntity(pos);
        if (tileEntity instanceof KegBlockEntity) {
            AbstractedItemHandler inventory = ((KegBlockEntity) tileEntity).getInventory();
            return BnCMathUtils.redstoneFromItemHandler(inventory);
        }
        return 0;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BnCBlockEntityTypes.KEG.create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        if (level.isClientSide())
            return null;
        return createTickerHelper(blockEntity, BnCBlockEntityTypes.KEG, KegBlockEntity::fermentingTick);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}