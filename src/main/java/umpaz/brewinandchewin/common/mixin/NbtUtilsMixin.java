
package umpaz.brewinandchewin.common.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import umpaz.brewinandchewin.common.utility.dfu.BnCDataFixer;

@Mixin(NbtUtils.class)
public class NbtUtilsMixin {
    @ModifyReturnValue(method = "addDataVersion", at = @At("RETURN"))
    private static CompoundTag bovinesandbuttercups$addBovinesDataVersion(CompoundTag original) {
        return BnCDataFixer.setModDataVersion(original);
    }
}