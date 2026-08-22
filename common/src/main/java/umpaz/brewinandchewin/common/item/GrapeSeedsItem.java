package umpaz.brewinandchewin.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.brewinandchewin.common.block.GrapeColour;
import umpaz.brewinandchewin.common.block.TrellisBlock;
import umpaz.brewinandchewin.common.block.TrellisGrapeBlock;

public class GrapeSeedsItem extends ItemNameBlockItem {
    private final GrapeColour colour;

    public GrapeSeedsItem(Block bush, GrapeColour colour, Properties properties) {
        super(bush, properties);
        this.colour = colour;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof TrellisBlock && !(state.getBlock() instanceof TrellisGrapeBlock)) {
            boolean front = TrellisGrapeBlock.isFront(state, context.getClickedFace());
            if (!level.isClientSide()) {
                level.setBlock(pos, TrellisGrapeBlock.fromTrellis(state)
                        .setValue(TrellisGrapeBlock.vineOf(front), this.colour)
                        .setValue(TrellisGrapeBlock.ageOf(front), 0), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                ItemStack stack = context.getItemInHand();
                if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild)
                    stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useOn(context);
    }
}
