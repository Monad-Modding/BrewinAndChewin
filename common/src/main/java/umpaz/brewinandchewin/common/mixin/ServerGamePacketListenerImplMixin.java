package umpaz.brewinandchewin.common.mixin;

import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.access.ChatPlayerListAccess;
import umpaz.brewinandchewin.common.network.clientbound.MakeNextPlayerChatTipsyClientboundPacket;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    // We must go here because chat links may be implemented here.
    @ModifyVariable(method = "broadcastChatMessage", at = @At("HEAD"), argsOnly = true, order = 1500) // Run after other chat message modifications, to make sure we don't screw with them.
    public PlayerChatMessage brewinandchewin$modifyChatMessageForServer(PlayerChatMessage message) {
        ServerPlayer sender = player.getServer().getPlayerList().getPlayer(message.sender());
        if (sender == null)
            return message;
        if (sender.hasEffect(BnCEffects.TIPSY) && sender.getEffect(BnCEffects.TIPSY).getAmplifier() >= BnCConfiguration.common().root().levelChatScramble()) {
            ((ChatPlayerListAccess)player.getServer().getPlayerList()).brewinandchewin$setOriginalMessage(message.decoratedContent());
            long randomSeed = player.getRandom().nextLong();
            for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers())
                BrewinAndChewin.getHelper().sendClientbound(otherPlayer, new MakeNextPlayerChatTipsyClientboundPacket(sender.getEffect(BnCEffects.TIPSY).getAmplifier(), randomSeed, 0));
            return BnCTextUtils.setupChatMessageServer(message, sender, randomSeed);
        }
        return message;
    }
}
