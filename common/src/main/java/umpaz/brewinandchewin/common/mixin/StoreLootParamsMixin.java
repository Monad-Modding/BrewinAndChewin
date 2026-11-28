package umpaz.brewinandchewin.common.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import umpaz.brewinandchewin.common.access.LootParamsParamSetAccess;

public class StoreLootParamsMixin {
    @Mixin(LootParams.class)
    public static class LootParamsMixin implements LootParamsParamSetAccess {
        @Unique
        private ContextKeySet brewinandchewin$paramSet;

        @Override
        public ContextKeySet brewinandchewin$getParamSet() {
            return brewinandchewin$paramSet;
        }

        @Override
        public void brewinandchewin$setParamSet(ContextKeySet value) {
            this.brewinandchewin$paramSet = value;
        }
    }

    @Mixin(LootParams.Builder.class)
    public static class LootParamsBuilderMixin {
        @ModifyReturnValue(method = "create", at = @At("RETURN"))
        private LootParams brewinandchewin$handleLootParams(LootParams original, ContextKeySet paramSet) {
            ((LootParamsParamSetAccess)original).brewinandchewin$setParamSet(paramSet);
            return original;
        }
    }
}
