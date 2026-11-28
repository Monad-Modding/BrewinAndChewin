package umpaz.brewinandchewin.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import umpaz.brewinandchewin.client.utility.BnCClientTextUtils;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @ModifyReturnValue(method = "getNameTag", at = @At("RETURN"))
    private Component brewinandchewin$scrambleName(Component displayName) {
        return displayName == null ? null : BnCClientTextUtils.nameTagRenderer(displayName);
    }
}
