package umpaz.brewinandchewin.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
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
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import vectorwing.farmersdelight.common.utility.MathUtils;

import javax.annotation.Nullable;
import java.util.List;

public class KegBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final BooleanProperty VERTICAL = BooleanProperty.create("vertical");
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;


   protected static final VoxelShape SHAPE_X = Block.box(1.0D, 0.0D, 0.0D, 15.0D, 16.0D, 16.0D);
   protected static final VoxelShape SHAPE_Z = Block.box(0.0D, 0.0D, 1.0D, 16.0D, 16.0D, 15.0D);
   protected static final VoxelShape SHAPE_VERTICAL = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

   public KegBlock( BlockBehaviour.Properties properties ) {
      super(properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(VERTICAL, false).setValue(WATERLOGGED, false));
   }

   @Override
   public InteractionResult use( BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result ) {
       ItemStack heldStack = player.getItemInHand(hand);

       BlockEntity tileEntity = level.getBlockEntity(pos);
       if (level.isClientSide())
           return InteractionResult.SUCCESS;
       if (tileEntity instanceof KegBlockEntity kegBE) {
           List<ItemStack> itms = kegBE.extractInWorld(heldStack, 1, player.getAbilities().instabuild);
           if (!itms.isEmpty()) {
               itms.forEach(itm -> {
                   if (!ItemStack.isSameItemSameTags(itm, heldStack)) {
                       if (heldStack.isEmpty()) {
                           player.setItemInHand(hand, itm);
                       } else if (!player.getInventory().add(itm)) {
                           player.drop(itm, false);
                       }
                   }
               });
               level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1, 1);
               return InteractionResult.CONSUME;
           }
           kegBE.updateTemperature();
           NetworkHooks.openScreen((ServerPlayer) player, kegBE, pos);
       }
       return InteractionResult.CONSUME;
   }


   @Override
   public RenderShape getRenderShape( BlockState pState ) {
      return RenderShape.MODEL;
   }

   @Override
   public VoxelShape getShape( BlockState state, BlockGetter level, BlockPos pos, CollisionContext context ) {
      if ( state.getValue(VERTICAL) ) {
         return SHAPE_VERTICAL;
      }
      if ( ( state.getValue(FACING) == Direction.NORTH || state.getValue(FACING) == Direction.SOUTH ) ) {
         return SHAPE_X;
      }
      return SHAPE_Z;
   }

   @Override
   public BlockState getStateForPlacement( BlockPlaceContext context ) {
      Level level = context.getLevel();
      FluidState fluid = level.getFluidState(context.getClickedPos());

      return this.defaultBlockState()
              .setValue(FACING, context.getHorizontalDirection().getOpposite())
              .setValue(VERTICAL, context.getNearestLookingVerticalDirection() == Direction.UP || context.getNearestLookingDirection() == Direction.DOWN)
              .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
   }

   @Override
   public BlockState updateShape( BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos ) {
      if ( state.getValue(WATERLOGGED) ) {
         level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
      }
      return state;
   }

   @Override
   public ItemStack getCloneItemStack( BlockGetter level, BlockPos pos, BlockState state ) {
      ItemStack stack = super.getCloneItemStack(level, pos, state);
      KegBlockEntity kettleEntity = (KegBlockEntity) level.getBlockEntity(pos);
      if ( kettleEntity != null ) {
         CompoundTag nbt = kettleEntity.writeMeal(new CompoundTag());
         if ( !nbt.isEmpty() ) {
            stack.addTagElement("BlockEntityTag", nbt);
         }
         if ( kettleEntity.hasCustomName() ) {
            stack.setHoverName(kettleEntity.getCustomName());
         }
      }
      return stack;
   }

   @Override
   public void onRemove( BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving ) {
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
   protected void createBlockStateDefinition( StateDefinition.Builder<Block, BlockState> builder ) {
      super.createBlockStateDefinition(builder);
      builder.add(FACING, VERTICAL, WATERLOGGED);
   }

   @Override
   public void setPlacedBy( Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack ) {
      if ( stack.hasCustomHoverName() ) {
         BlockEntity tileEntity = level.getBlockEntity(pos);
         if ( tileEntity instanceof KegBlockEntity ) {
            ( (KegBlockEntity) tileEntity ).setCustomName(stack.getHoverName());
         }
      }
   }

   @Override
   public boolean hasAnalogOutputSignal( BlockState state ) {
      return true;
   }

   @Override
   public int getAnalogOutputSignal( BlockState blockState, Level level, BlockPos pos ) {
      BlockEntity tileEntity = level.getBlockEntity(pos);
      if ( tileEntity instanceof KegBlockEntity ) {
         ItemStackHandler inventory = ( (KegBlockEntity) tileEntity ).getInventory();
         return MathUtils.calcRedstoneFromItemHandler(inventory);
      }
      return 0;
   }

   @Override
   public FluidState getFluidState( BlockState state ) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity( BlockPos pos, BlockState state ) {
      return BnCBlockEntityTypes.KEG.get().create(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker( Level level, BlockState state, BlockEntityType<T> blockEntity ) {
       if (level.isClientSide())
           return null;
       return createTickerHelper(blockEntity, BnCBlockEntityTypes.KEG.get(), KegBlockEntity::fermentingTick);
   }
}