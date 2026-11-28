package umpaz.brewinandchewin.common.network.serverbound;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.utility.BnCStreamCodecs;
import umpaz.brewinandchewin.integration.jei.transfer.FermentingTransfer;
import umpaz.brewinandchewin.integration.jei.transfer.FermentingTransferServer;

import java.util.List;

public record JEITransferKegRecipeServerboundPacket(Identifier recipeId,
                                                    List<Pair<Integer, Integer>> resultSlots,
                                                    List<Pair<Integer, Long>> fluidSlots,
                                                    List<Pair<Integer, Long>> emptyingSlots,
                                                    List<Integer> craftingSlots,
                                                    List<Integer> inventorySlots,
                                                    boolean maxTransfer) implements CustomPacketPayload {
    public static final Identifier ID = BrewinAndChewin.asResource("jei_transfer_keg_recipe");
    public static final CustomPacketPayload.Type<JEITransferKegRecipeServerboundPacket> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, JEITransferKegRecipeServerboundPacket> STREAM_CODEC = StreamCodec.of(JEITransferKegRecipeServerboundPacket::encode, JEITransferKegRecipeServerboundPacket::new);

    public JEITransferKegRecipeServerboundPacket(RegistryFriendlyByteBuf buf) {
        this(
                buf.readResourceLocation(),
                BnCStreamCodecs.INT_PAIR_LIST.decode(buf),
                BnCStreamCodecs.INT_LONG_PAIR_LIST.decode(buf),
                BnCStreamCodecs.INT_LONG_PAIR_LIST.decode(buf),
                ByteBufCodecs.INT.apply(ByteBufCodecs.list()).decode(buf),
                ByteBufCodecs.INT.apply(ByteBufCodecs.list()).decode(buf),
                buf.readBoolean()
        );
    }

    public static void encode(FriendlyByteBuf buf, JEITransferKegRecipeServerboundPacket packet) {
        buf.writeResourceLocation(packet.recipeId);
        BnCStreamCodecs.INT_PAIR_LIST.encode(buf, packet.resultSlots);
        BnCStreamCodecs.INT_LONG_PAIR_LIST.encode(buf, packet.fluidSlots);
        BnCStreamCodecs.INT_LONG_PAIR_LIST.encode(buf, packet.emptyingSlots);
        ByteBufCodecs.INT.apply(ByteBufCodecs.list()).encode(buf, packet.craftingSlots);
        ByteBufCodecs.INT.apply(ByteBufCodecs.list()).encode(buf, packet.inventorySlots);
        buf.writeBoolean(packet.maxTransfer);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer sender) {
        sender.server.execute(() -> {
            if (!BrewinAndChewin.getHelper().isModLoaded("jei"))
                return;
            var recipe = sender.getServer().getRecipeManager().byKey(recipeId());
            if (recipe.isEmpty() || !(recipe.get().value() instanceof KegFermentingRecipe kegFermentingRecipe))
                return;
            FermentingTransferServer.setItems(
                    sender,
                    kegFermentingRecipe,
                    FermentingTransfer.TransferOperations.readFromIntegers(resultSlots(), fluidSlots(), emptyingSlots(), sender.containerMenu),
                    craftingSlots().stream().map(sender.containerMenu::getSlot).toList(),
                    inventorySlots().stream().map(sender.containerMenu::getSlot).toList(),
                    maxTransfer()
            );
        });
    }
}
