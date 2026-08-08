package umpaz.brewinandchewin.common.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import umpaz.brewinandchewin.client.BnCClientHooks;
import umpaz.brewinandchewin.common.utility.BnCLabelUtils;

public class LabelItem extends Item {
    public LabelItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player))
            return false;

        ItemStack target = slot.getItem();
        if (!BnCLabelUtils.canBeLabelled(target) || target.getItem() instanceof LabelItem)
            return false;

        BnCLabelUtils.setLabel(target, BnCLabelUtils.readFromItem(stack));
        slot.setChanged();
        stack.shrink(1);
        player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player))
            return false;
        if (!BnCLabelUtils.canBeLabelled(other) || other.getItem() instanceof LabelItem)
            return false;

        BnCLabelUtils.setLabel(other, BnCLabelUtils.readFromItem(stack));
        stack.shrink(1);
        slot.setChanged();
        player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level.isClientSide()) {
            BnCClientHooks.openLabelScreen(held, hand);
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
    }
}
