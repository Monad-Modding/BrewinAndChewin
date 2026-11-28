package umpaz.brewinandchewin.common.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.ServerRecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.BnCRecipeBookTypes;
import umpaz.brewinandchewin.common.network.clientbound.SendRecipeBookValuesClientboundPacket;

@Mixin(ServerRecipeBook.class)
public class ServerRecipeBookMixin extends RecipeBook {
    @Inject(method = "sendInitialRecipeBook", at = @At("TAIL"))
    private void brewinandchewin$sendFermentingRecipeValues(ServerPlayer player, CallbackInfo ci) {
        BrewinAndChewin.getHelper().sendClientbound(player, new SendRecipeBookValuesClientboundPacket(getBookSettings().isOpen(BnCRecipeBookTypes.FERMENTING), getBookSettings().isFiltering(BnCRecipeBookTypes.FERMENTING)));
    }
}