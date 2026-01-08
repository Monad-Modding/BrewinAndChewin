package umpaz.brewinandchewin.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import umpaz.brewinandchewin.common.registry.BnCItems;
import vectorwing.farmersdelight.common.registry.ModParticleTypes;
import vectorwing.farmersdelight.common.registry.ModSounds;


public class FieryFonduePotBlock extends Block {
    public static final VoxelShape INSIDE = box(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    public static final VoxelShape SHAPE = Shapes.join(Shapes.block(), Shapes.or(box(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D), box(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D), box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D), INSIDE), BooleanOp.ONLY_FIRST);
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;



    public FieryFonduePotBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(3)).setValue(FACING, Direction.NORTH));
    }

    protected double getContentHeight(BlockState state) {
        return (6.0D + (double)state.getValue(LEVEL).intValue() * 3.0D) / 16.0D;
    }

    public boolean isEntityInsideContent(BlockState state, BlockPos pos, Entity entity) {
        return entity.getY() < (double)pos.getY() + this.getContentHeight(state) && entity.getBoundingBox().maxY > (double)pos.getY() + 0.25D;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (this.isEntityInsideContent(state, pos, entity)) {
            entity.lavaHurt();
        }
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(LEVEL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(LEVEL, FACING);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return this.takeServing(level, pos, state, player, hand);
    }

    private InteractionResult takeServing(Level level, BlockPos pos, BlockState state, Player player, InteractionHand handIn) {
        int servings = state.getValue(LEVEL);
        ItemStack bowl = new ItemStack(Items.BOWL);
        ItemStack fondue = new ItemStack(BnCItems.FIERY_FONDUE);
        ItemStack heldStack = player.getItemInHand(handIn);
        if (heldStack.is(bowl.getItem())) {
            if (!player.getAbilities().instabuild) {
                heldStack.shrink(1);
            }
            if (!player.getInventory().add(fondue)) {
                player.drop(fondue, false);
            }
            BlockState newState = level.getBlockState(pos).getValue(LEVEL) > 1 ? state.setValue(LEVEL, servings - 1) : Blocks.CAULDRON.defaultBlockState();
            level.setBlock(pos, newState, 3);
            if (state.getValue(LEVEL) == 1) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.BONE));
            }

            level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        player.displayClientMessage(Component.translatable("farmersdelight.block.feast.use_container", bowl.getHoverName()), true);
        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return INSIDE;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    // NeoForge:
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType path) {
        return false;
    }

    @Override
    public void animateTick(BlockState stateIn, Level level, BlockPos pos, RandomSource rand) {
        super.animateTick(stateIn, level, pos, rand);
        RandomSource random = level.random;
        if (random.nextFloat() < 0.8F) {
            double x = (double) pos.getX() + 0.5D + (random.nextDouble() * 0.6D - 0.3D);
            double y = (double) pos.getY() + this.getContentHeight(stateIn);
            double z = (double) pos.getZ() + 0.5D + (random.nextDouble() * 0.6D - 0.3D);
            level.addParticle(ModParticleTypes.STEAM.get(), x, y, z, 0.0D, 0.0D, 0.0D);

            double x1 = (double) pos.getX() + 0.5D;
            double y1 = pos.getY();
            double z1 = (double) pos.getZ() + 0.5D;
            if (rand.nextInt(10) == 0) {
                level.playLocalSound(x1, y1, z1, ModSounds.BLOCK_COOKING_POT_BOIL_SOUP.get(), SoundSource.BLOCKS, 0.5F, rand.nextFloat() * 0.2F + 0.9F, false);
            }
        }
    }
}