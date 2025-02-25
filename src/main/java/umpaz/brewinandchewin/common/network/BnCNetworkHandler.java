
package umpaz.brewinandchewin.common.network;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.network.clientbound.ClearKegFluidContainerComponentsPacket;
import umpaz.brewinandchewin.common.network.clientbound.SyncNumbedHeartsClientboundPacket;
import umpaz.brewinandchewin.common.network.clientbound.SyncRagingStacksClientboundPacket;
import umpaz.brewinandchewin.common.network.serverbound.EMIFillFermentingRecipeServerboundPacket;
import umpaz.brewinandchewin.common.network.serverbound.EMIFillPouringRecipeServerboundPacket;
import umpaz.brewinandchewin.common.network.serverbound.JEITransferKegRecipeServerboundPacket;

public class BnCNetworkHandler {
    private static final String PROTOCOL_VERISON = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            BrewinAndChewin.asResource("main"),
            () -> PROTOCOL_VERISON,
            PROTOCOL_VERISON::equals,
            PROTOCOL_VERISON::equals
    );

    public static void register() {
        int i = 0;
        INSTANCE.registerMessage(i++, SyncNumbedHeartsClientboundPacket.class, SyncNumbedHeartsClientboundPacket::encode, SyncNumbedHeartsClientboundPacket::decode, SyncNumbedHeartsClientboundPacket.Handler::handle);
        INSTANCE.registerMessage(i++, SyncRagingStacksClientboundPacket.class, SyncRagingStacksClientboundPacket::encode, SyncRagingStacksClientboundPacket::decode, SyncRagingStacksClientboundPacket.Handler::handle);
        INSTANCE.registerMessage(i++, ClearKegFluidContainerComponentsPacket.class, ClearKegFluidContainerComponentsPacket::encode, ClearKegFluidContainerComponentsPacket::decode, ClearKegFluidContainerComponentsPacket.Handler::handle);
        INSTANCE.registerMessage(i++, JEITransferKegRecipeServerboundPacket.class, JEITransferKegRecipeServerboundPacket::encode, JEITransferKegRecipeServerboundPacket::decode, JEITransferKegRecipeServerboundPacket.Handler::handle);
        INSTANCE.registerMessage(i++, EMIFillFermentingRecipeServerboundPacket.class, EMIFillFermentingRecipeServerboundPacket::encode, EMIFillFermentingRecipeServerboundPacket::decode, EMIFillFermentingRecipeServerboundPacket.Handler::handle);
        INSTANCE.registerMessage(i++, EMIFillPouringRecipeServerboundPacket.class, EMIFillPouringRecipeServerboundPacket::encode, EMIFillPouringRecipeServerboundPacket::decode, EMIFillPouringRecipeServerboundPacket.Handler::handle);
    }

}
