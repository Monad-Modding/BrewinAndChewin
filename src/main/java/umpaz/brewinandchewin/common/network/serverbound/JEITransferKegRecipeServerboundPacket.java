package umpaz.brewinandchewin.common.network.serverbound;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.integration.jei.transfer.FermentingTransfer;
import umpaz.brewinandchewin.integration.jei.transfer.FermentingTransferServer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record JEITransferKegRecipeServerboundPacket(ResourceLocation recipeId,
                                                    List<Pair<Integer, Integer>> resultSlots,
                                                    List<Pair<Integer, Integer>> fluidSlots,
                                                    List<Pair<Integer, Integer>> emptyingSlots,
                                                    List<Integer> craftingSlots,
                                                    List<Integer> inventorySlots,
                                                    boolean maxTransfer) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(recipeId);
        encodeIntPairs(buf, resultSlots);
        encodeIntPairs(buf, fluidSlots);
        encodeIntPairs(buf, emptyingSlots);
        encodeSlots(buf, craftingSlots);
        encodeSlots(buf, inventorySlots);
        buf.writeBoolean(maxTransfer);
    }

    private static void encodeIntPairs(FriendlyByteBuf buf, List<Pair<Integer, Integer>> slots) {
        buf.writeInt(slots.size());
        for (Pair<Integer, Integer> slot : slots) {
            buf.writeVarInt(slot.getFirst());
            buf.writeInt(slot.getSecond());
        }
    }

    private static void encodeSlots(FriendlyByteBuf buf, List<Integer> slots) {
        buf.writeInt(slots.size());
        for (int slot : slots)
            buf.writeVarInt(slot);
    }

    public static JEITransferKegRecipeServerboundPacket decode(FriendlyByteBuf buf) {
        return new JEITransferKegRecipeServerboundPacket(
                buf.readResourceLocation(),
                decodeIntPairs(buf),
                decodeIntPairs(buf),
                decodeIntPairs(buf),
                decodeSlots(buf),
                decodeSlots(buf),
                buf.readBoolean()
        );
    }

    private static List<Pair<Integer, Integer>> decodeIntPairs(FriendlyByteBuf buf) {
        int listSize = buf.readInt();
        List<Pair<Integer, Integer>> slots = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; ++i) {
            int inventorySlotIndex = buf.readVarInt();
            int craftingSlotIndex = buf.readInt();
            slots.add(Pair.of(inventorySlotIndex, craftingSlotIndex));
        }
        return slots;
    }

    private static List<Integer> decodeSlots(FriendlyByteBuf buf) {
        int listSize = buf.readInt();
        List<Integer> slots = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; ++i) {
            int slotIndex = buf.readVarInt();
            slots.add(slotIndex);
        }
        return slots;
    }

    public static class Handler {
        public static void handle(JEITransferKegRecipeServerboundPacket packet, Supplier<NetworkEvent.Context> context) {
            if (context.get().getDirection() != NetworkDirection.PLAY_TO_SERVER)
                return;
            context.get().enqueueWork(() -> {
                ServerPlayer sender = context.get().getSender();
                if (sender == null)
                    return;
                var recipe = sender.getServer().getRecipeManager().byKey(packet.recipeId);
                if (recipe.isEmpty() || !(recipe.get() instanceof KegFermentingRecipe kegFermentingRecipe))
                    return;
                FermentingTransferServer.setItems(
                        sender,
                        kegFermentingRecipe,
                        FermentingTransfer.TransferOperations.readFromIntegers(packet.resultSlots, packet.fluidSlots, packet.emptyingSlots, sender.containerMenu),
                        packet.craftingSlots.stream().map(sender.containerMenu::getSlot).toList(),
                        packet.inventorySlots.stream().map(sender.containerMenu::getSlot).toList(),
                        packet.maxTransfer
                );
            });
            context.get().setPacketHandled(true);
        }
    }
}
