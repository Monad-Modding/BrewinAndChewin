package umpaz.brewinandchewin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.client.gui.LabelScreen;

public class BnCClientHooks {
    public static void openLabelScreen(ItemStack stack, InteractionHand hand) {
        Minecraft.getInstance().setScreen(new LabelScreen(stack, hand));
    }
}
