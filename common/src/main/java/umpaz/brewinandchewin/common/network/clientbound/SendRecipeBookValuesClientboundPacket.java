
package umpaz.brewinandchewin.common.network.clientbound;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.BnCRecipeBookTypes;
import umpaz.brewinandchewin.platform.BnCPacket;

public record SendRecipeBookValuesClientboundPacket(boolean open, boolean filtering) implements BnCPacket {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("send_recipe_book_values");

    public SendRecipeBookValuesClientboundPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.open);
        buf.writeBoolean(this.filtering);

    }

    @Override
    public void handle(ServerPlayer player) {
        Minecraft.getInstance().execute(() -> {
            ClientRecipeBook recipeBook = Minecraft.getInstance().player.getRecipeBook();
            recipeBook.setOpen(BnCRecipeBookTypes.FERMENTING, open);
            recipeBook.setOpen(BnCRecipeBookTypes.FERMENTING, filtering);
        });
    }
}
