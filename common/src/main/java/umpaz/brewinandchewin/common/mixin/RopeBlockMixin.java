package umpaz.brewinandchewin.common.mixin;

import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.brewinandchewin.common.block.RopeGrapeBlock;
import vectorwing.farmersdelight.common.block.RopeBlock;

@Mixin(value = RopeBlock.class, remap = false)
public class RopeBlockMixin {
    @Inject(method = "tieToRopeAndWalls", at = @At("HEAD"), cancellable = true)
    private static void brewinandchewin$tieToRopeGrapes(BlockState state, CallbackInfoReturnable<Boolean> callback) {
        if (state.getBlock() instanceof RopeGrapeBlock)
            callback.setReturnValue(true);
    }
}
