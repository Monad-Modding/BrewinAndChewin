package umpaz.brewinandchewin.common.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.DataFixTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import umpaz.brewinandchewin.common.utility.dfu.BnCDataFixer;

@Mixin(DataFixTypes.class)
public class DataFixTypesMixin {
    @ModifyReturnValue(method = "update(Lcom/mojang/datafixers/DataFixer;Lcom/mojang/serialization/Dynamic;II)Lcom/mojang/serialization/Dynamic;", at = @At("RETURN"))
    private <T> Dynamic<T> brewinandchewin$updateWithDataFixers(Dynamic<T> original) {
        return BnCDataFixer.get().updateWithFixers((DataFixTypes)(Object)this, original);
    }
}
