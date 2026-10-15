package umpaz.brewinandchewin.common.mixin.client.integration.appleskin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.registry.BnCEffects;

import java.util.Random;

@Mixin(targets = "squeek.appleskin.client.HUDOverlayHandler$OffsetsCache")
public class HUDOverlayHandlerMixin {
    @ModifyVariable(method = "generate", at = @At(value = "INVOKE", target = "Ljava/util/Vector;get(I)Ljava/lang/Object;", ordinal = 1), name = "x")
    private int brewinandchewin$drawIntoxicationSaturationX(int x, int guiTicks, Player player, @Local(ordinal = 6) int i) {
        if (BnCConfiguration.client().intoxicationFoodOverlay() && player.hasEffect(BnCEffects.INTOXICATION)) {
            Random rand = new Random();
            rand.setSeed(guiTicks * 312871L);
            return x + (int) (Mth.cos((guiTicks + i * 2) * 0.20F) * 2F);
        }
        return x;
    }

    @ModifyVariable(method = "generate", at = @At(value = "INVOKE", target = "Ljava/util/Vector;get(I)Ljava/lang/Object;", ordinal = 1), ordinal = 8)
    private int brewinandchewin$drawIntoxicationSaturationY(int y, int guiTicks, Player player, @Local(ordinal = 6) int i) {
        if (BnCConfiguration.client().intoxicationFoodOverlay() && player.hasEffect(BnCEffects.INTOXICATION)) {
            Random rand = new Random();
            rand.setSeed(guiTicks * 312871L);
            return y + (int) (Mth.sin((guiTicks + i * 2) * 0.25F) * 2F);
        }
        return y;
    }
}
