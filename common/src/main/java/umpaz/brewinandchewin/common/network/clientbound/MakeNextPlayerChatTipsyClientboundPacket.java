package umpaz.brewinandchewin.common.network.clientbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.utility.BnCClientTextUtils;
import umpaz.brewinandchewin.platform.BnCPacket;

public record MakeNextPlayerChatTipsyClientboundPacket(int level, long randomSeed, int clearDelayAmount) implements BnCPacket {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("make_next_player_chat_tipsy");

    public MakeNextPlayerChatTipsyClientboundPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readLong(), buf.readInt());
    }

    public static void encode(FriendlyByteBuf buf, MakeNextPlayerChatTipsyClientboundPacket packet) {
        buf.writeInt(packet.level());
        buf.writeLong(packet.randomSeed());
        buf.writeInt(packet.clearDelayAmount());
    }

    public void handle() {
        BnCClientTextUtils.tipsyMessageLevel = level();
        BnCClientTextUtils.randomSeed = randomSeed();
        BnCClientTextUtils.clearDelayAmount = clearDelayAmount();
        BnCClientTextUtils.generatedRandom = true;
    }

    @Override
    public void write(FriendlyByteBuf buf) {

    }

    @Override
    public void handle(ServerPlayer player) {

    }
}
