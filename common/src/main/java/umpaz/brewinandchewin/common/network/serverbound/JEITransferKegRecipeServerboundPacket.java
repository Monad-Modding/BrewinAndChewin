package umpaz.brewinandchewin.common.network.serverbound;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.integration.jei.transfer.FermentingTransfer;
import umpaz.brewinandchewin.integration.jei.transfer.FermentingTransferServer;
import umpaz.brewinandchewin.platform.BnCPacket;

import java.util.ArrayList;
import java.util.List;

public record JEITransferKegRecipeServerboundPacket(ResourceLocation recipeId,
                                                    List<Pair<Integer, Integer>> resultSlots,
                                                    List<Pair<Integer, Long>> fluidSlots,
                                                    List<Pair<Integer, Long>> emptyingSlots,
                                                    List<Integer> craftingSlots,
                                                    List<Integer> inventorySlots,
                                                    boolean maxTransfer) implements BnCPacket {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("jei_transfer_keg_recipe");

    public JEITransferKegRecipeServerboundPacket(FriendlyByteBuf buf) {
        this(
            buf.readResourceLocation(),
            readIntPairList(buf),
            readIntLongPairList(buf),
            readIntLongPairList(buf),
            readIntList(buf),
            readIntList(buf),
            buf.readBoolean()
        );
    }

    private static List<Pair<Integer, Integer>> readIntPairList(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<Pair<Integer, Integer>> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(Pair.of(buf.readInt(), buf.readInt()));
        }
        return list;
    }

    private static List<Pair<Integer, Long>> readIntLongPairList(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<Pair<Integer, Long>> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(Pair.of(buf.readInt(), buf.readLong()));
        }
        return list;
    }

    private static List<Integer> readIntList(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(buf.readInt());
        }
        return list;
    }

    private static void writeIntPairList(FriendlyByteBuf buf, List<Pair<Integer, Integer>> list) {
        buf.writeInt(list.size());
        for (Pair<Integer, Integer> pair : list) {
            buf.writeInt(pair.getFirst());
            buf.writeInt(pair.getSecond());
        }
    }

    private static void writeIntLongPairList(FriendlyByteBuf buf, List<Pair<Integer, Long>> list) {
        buf.writeInt(list.size());
        for (Pair<Integer, Long> pair : list) {
            buf.writeInt(pair.getFirst());
            buf.writeLong(pair.getSecond());
        }
    }

    private static void writeIntList(FriendlyByteBuf buf, List<Integer> list) {
        buf.writeInt(list.size());
        for (int i : list) {
            buf.writeInt(i);
        }
    }


    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.recipeId);

        writeIntPairList(buf, this.resultSlots);
        writeIntLongPairList(buf, this.fluidSlots);
        writeIntLongPairList(buf, this.emptyingSlots);
        writeIntList(buf, this.craftingSlots);
        writeIntList(buf, this.inventorySlots);

        buf.writeBoolean(this.maxTransfer);
    }

    public void handle(ServerPlayer sender) {
        sender.server.execute(() -> {
            if (!BrewinAndChewin.getHelper().isModLoaded("jei"))
                return;
            var recipe = sender.getServer().getRecipeManager().byKey(recipeId());
            if (recipe.isEmpty() || !(recipe.get() instanceof KegFermentingRecipe kegFermentingRecipe))
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
