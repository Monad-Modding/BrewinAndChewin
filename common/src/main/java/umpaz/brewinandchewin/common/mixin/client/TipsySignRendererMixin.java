package umpaz.brewinandchewin.common.mixin.client;

import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import umpaz.brewinandchewin.client.utility.BnCClientTextUtils;

public class TipsySignRendererMixin {

    @Mixin(AbstractSignRenderer.class)
    public static class TipsySignRenderMixin {
        @ModifyVariable(method = "submitSignText", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
        private SignText brewinandchewin$renderSignText(SignText signText) {
            return BnCClientTextUtils.signRenderer(signText);
        }
    }
}
