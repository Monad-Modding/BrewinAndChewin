
package umpaz.brewinandchewin.common.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.gui.KegScreen;
import umpaz.brewinandchewin.platform.BnCPacket;

public record ClearKegFluidContainerComponentsClientboundPacket() implements BnCPacket {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("clear_keg_fluid_container_components");

    public ClearKegFluidContainerComponentsClientboundPacket(FriendlyByteBuf buf) {
        this();
    }

    @Override
    public void write(FriendlyByteBuf buf) {

    }

    @Override
    public void handle(ServerPlayer player) {
        Minecraft.getInstance().execute(KegScreen::clearFluidContainerComponents);
    }
}
