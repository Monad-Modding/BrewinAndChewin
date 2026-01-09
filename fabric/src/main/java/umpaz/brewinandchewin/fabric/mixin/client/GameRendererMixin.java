package umpaz.brewinandchewin.fabric.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.brewinandchewin.fabric.client.gui.BnCHUDOverlays;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", shift = At.Shift.AFTER))
    private void brewinandchewin$renderScreen(float f, boolean bl, CallbackInfo ci, @Local GuiGraphics gui) {
        BnCHUDOverlays.TipsyOverlay.INSTANCE.render(gui, delta);
    }
}
