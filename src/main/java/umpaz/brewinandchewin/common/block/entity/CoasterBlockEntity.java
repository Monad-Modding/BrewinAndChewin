package umpaz.brewinandchewin.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import umpaz.brewinandchewin.common.block.CoasterBlock;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.registry.BnCItems;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;

import static umpaz.brewinandchewin.common.block.CoasterBlock.INVISIBLE;
import static umpaz.brewinandchewin.common.block.CoasterBlock.SIZE;

public class CoasterBlockEntity extends SyncedBlockEntity {

    public final NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);

    public CoasterBlockEntity(BlockPos pos, BlockState state ) {
      super(BnCBlockEntityTypes.COASTER.get(), pos, state);
   }

    public InteractionResult onUse( Level level, BlockState state, BlockPos pos, Player player, InteractionHand hand ) {
        ItemStack heldStack = player.getItemInHand(hand);
        ItemStack offhandStack = player.getOffhandItem();
        if (state.getValue(CoasterBlock.INVISIBLE) && heldStack.is(BnCItems.COASTER.get())) {
            if (!player.getAbilities().instabuild)
                heldStack.shrink(1);
            level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS);
            level.setBlockAndUpdate(pos, state.setValue(CoasterBlock.INVISIBLE, false));
            inventoryChanged();
            return InteractionResult.SUCCESS;
        } if (state.getValue(CoasterBlock.SIZE) < 4 && (addItem(level, pos, state, heldStack, player.getAbilities().instabuild, state.getValue(CoasterBlock.SIZE)) || addItem(level, pos, state, offhandStack, player.getAbilities().instabuild, state.getValue(CoasterBlock.SIZE)))) {
            return InteractionResult.SUCCESS;
        } else if (!heldStack.isEmpty()) {
            return InteractionResult.CONSUME;
        } else if (state.getValue(CoasterBlock.SIZE) > 0 && player.getMainHandItem().isEmpty() && hand == InteractionHand.MAIN_HAND) { //Pickup Logic
            if (player.isShiftKeyDown() && !state.getValue(INVISIBLE)) {
                ItemStack coaster = new ItemStack(BnCItems.COASTER.get());
                if (!player.getAbilities().instabuild && !player.addItem(coaster)) {
                    player.drop(coaster, false);
                }
                level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS);
                level.setBlockAndUpdate(pos, state.setValue(CoasterBlock.INVISIBLE, true));
                return InteractionResult.SUCCESS;
            }
            int count = state.getValue(CoasterBlock.SIZE);
            if (!player.getAbilities().instabuild && !player.addItem(inventory.get(count - 1))) {
                player.drop(inventory.get(count - 1), false);
            }
            BlockState replaceWith = Blocks.AIR.defaultBlockState();
            if (!state.getValue(INVISIBLE) || count > 1) {
                replaceWith = state.setValue(CoasterBlock.SIZE, state.getValue(CoasterBlock.SIZE) - 1);
            }
            level.setBlockAndUpdate(pos, replaceWith);
            inventory.set(count - 1, ItemStack.EMPTY);
            inventoryChanged();

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private boolean addItem(Level level, BlockPos pos, BlockState state, ItemStack stack, boolean instabuild, int index) {
        if (stack.isEmpty())
            return false;
        level.setBlock(pos, state.setValue(SIZE, index + 1), 3);
        inventory.set(index, stack.copyWithCount(1));
        inventoryChanged();
        if (!instabuild)
            stack.shrink(1);
        return true;
    }

   @Override
   public void load( CompoundTag nbt ) {
       super.load(nbt);
       inventory.clear();
       ContainerHelper.loadAllItems(nbt, inventory);
   }

   @Override
   protected void saveAdditional( CompoundTag nbt ) {
       super.saveAdditional(nbt);
       ContainerHelper.saveAllItems(nbt, inventory);
   }

   @Override
   public AABB getRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        return AABB.of(BoundingBox.fromCorners(pos, pos.above()));
   }

   public NonNullList<ItemStack> getItems() {
       return inventory;
   }
}