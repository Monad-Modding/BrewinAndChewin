package umpaz.brewinandchewin.common.network.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.item.LabelItem;
import umpaz.brewinandchewin.common.item.component.LabelContents;
import umpaz.brewinandchewin.common.utility.BnCLabelUtils;

public record SetLabelContentsServerboundPacket(boolean offhand, String text, boolean showAuthor,
                                                boolean showAuthenticity) implements CustomPacketPayload {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("set_label_contents");
    public static final CustomPacketPayload.Type<SetLabelContentsServerboundPacket> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SetLabelContentsServerboundPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SetLabelContentsServerboundPacket::offhand,
            ByteBufCodecs.stringUtf8(LabelContents.MAX_TEXT_LENGTH), SetLabelContentsServerboundPacket::text,
            ByteBufCodecs.BOOL, SetLabelContentsServerboundPacket::showAuthor,
            ByteBufCodecs.BOOL, SetLabelContentsServerboundPacket::showAuthenticity,
            SetLabelContentsServerboundPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer sender) {
        sender.server.execute(() -> {
            ItemStack stack = sender.getItemInHand(offhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (!(stack.getItem() instanceof LabelItem))
                return;
            LabelContents contents = BnCLabelUtils.readFromItem(stack)
                    .withText(text())
                    .withAuthor(sender.getGameProfile().getName())
                    .withShowAuthor(showAuthor())
                    .withShowAuthenticity(showAuthenticity());
            BnCLabelUtils.setLabel(stack, contents);
        });
    }
}
