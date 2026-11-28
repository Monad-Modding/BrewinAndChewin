
package umpaz.brewinandchewin.common.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.gui.KegScreen;

public record ClearKegFluidContainerComponentsClientboundPacket() implements CustomPacketPayload {
    public static final Identifier ID = BrewinAndChewin.asResource("clear_keg_fluid_container_components");
    public static final Type<ClearKegFluidContainerComponentsClientboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearKegFluidContainerComponentsClientboundPacket> STREAM_CODEC = StreamCodec.of(ClearKegFluidContainerComponentsClientboundPacket::encode, ClearKegFluidContainerComponentsClientboundPacket::new);

    public ClearKegFluidContainerComponentsClientboundPacket(RegistryFriendlyByteBuf buf) {
        this();
    }

    public static void encode(FriendlyByteBuf buf, ClearKegFluidContainerComponentsClientboundPacket packet) {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle() {
        Minecraft.getInstance().execute(KegScreen::clearFluidContainerComponents);
    }
}
