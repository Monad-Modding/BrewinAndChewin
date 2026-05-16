package umpaz.brewinandchewin.common.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.brewinandchewin.client.BnCClientSetup;
import umpaz.brewinandchewin.client.utility.BnCClientTextUtils;

import java.time.Instant;

@Mixin(ChatListener.class)
public class ChatListenerMixin {
    @Shadow @Final private Minecraft minecraft;

    @ModifyVariable(method = "showMessageToPlayer", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/multiplayer/chat/ChatListener;evaluateTrustLevel(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/network/chat/Component;Ljava/time/Instant;)Lnet/minecraft/client/multiplayer/chat/ChatTrustLevel;"), name = "chattrustlevel")
    public ChatTrustLevel brewinandchewin$setTipsyChatToModified(ChatTrustLevel original, @Local(argsOnly = true) PlayerChatMessage chatMessage, @Local(argsOnly = true) boolean onlyShowSecureChat) {
        if (!(onlyShowSecureChat && !original.isNotSecure()) && !this.minecraft.isBlocked(chatMessage.sender()) && !chatMessage.isFullyFiltered()) {
            BnCClientTextUtils.setupChatMessage(chatMessage);
            if (BnCClientTextUtils.getTipsyMessage() != null) {
                BnCClientTextUtils.clearTipsyMessage();
                return ChatTrustLevel.MODIFIED;
            }
        }
        return original;
    }

    @Inject(method = "showMessageToPlayer", at = @At("RETURN"))
    public void brewinandchewin$modifyTipsyMessageWhileFiltering(ChatType.Bound bound, PlayerChatMessage playerChatMessage, Component component, GameProfile gameProfile, boolean bl, Instant instant, CallbackInfoReturnable<Boolean> cir) {
        BnCClientTextUtils.tipsyMessageLevel = 0;
        BnCClientTextUtils.randomSeed = 0L;
        BnCClientTextUtils.generatedRandom = false;
    }
}
