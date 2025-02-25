package umpaz.brewinandchewin.common.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.logging.LoggerAdapterDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.utility.dfu.BnCDataFixer;

import javax.annotation.Nullable;

@Mixin(PrimaryLevelData.class)
public class PrimaryLevelDataMixin {
    @Shadow @Nullable private CompoundTag loadedPlayerTag;

    @ModifyExpressionValue(method = "updatePlayerTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/DataVersion;getVersion()I"))
    private int brewinandchewin$updatePlayerTagWithBnCDataFixer(int original) {
        if (loadedPlayerTag != null && BnCDataFixer.getModDataVersion(loadedPlayerTag) < BnCDataFixer.CURRENT_VERSION)
            return Integer.MAX_VALUE;
        return original;
    }
}
