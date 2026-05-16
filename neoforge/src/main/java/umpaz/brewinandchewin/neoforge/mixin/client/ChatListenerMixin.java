package umpaz.brewinandchewin.neoforge.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import umpaz.brewinandchewin.client.utility.BnCClientTextUtils;

@Mixin(ChatListener.class)
public class ChatListenerMixin {
    @ModifyVariable(method = "showMessageToPlayer", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/multiplayer/chat/ChatTrustLevel;createTag(Lnet/minecraft/network/chat/PlayerChatMessage;)Lnet/minecraft/client/GuiMessageTag;"), name = "guimessagetag")
    public GuiMessageTag brewinandchewin$setTipsyChatToModified(GuiMessageTag original, @Local(argsOnly = true) PlayerChatMessage chatMessage, @Local(argsOnly = true) boolean onlyShowSecureChat) {
        BnCClientTextUtils.setupChatMessage(chatMessage);
        if (BnCClientTextUtils.getTipsyMessage() != null) {
            BnCClientTextUtils.clearTipsyMessage();
            return GuiMessageTag.chatModified(chatMessage.signedContent());
        }
        return original;
    }
}
