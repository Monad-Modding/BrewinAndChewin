package umpaz.brewinandchewin.platform;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

public interface BnCPacket{
    void write(FriendlyByteBuf buf);
    void handle(ServerPlayer player);
}