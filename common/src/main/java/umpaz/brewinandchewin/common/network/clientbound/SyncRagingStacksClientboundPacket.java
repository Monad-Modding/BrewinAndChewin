
package umpaz.brewinandchewin.common.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.attachment.RagingAttachment;
import umpaz.brewinandchewin.platform.BnCPacket;

import java.util.Optional;

public record SyncRagingStacksClientboundPacket(int entityId, Optional<Integer> stacks) implements BnCPacket {

    public SyncRagingStacksClientboundPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readOptional(FriendlyByteBuf::readInt));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeOptional(stacks, FriendlyByteBuf::writeInt);
    }

    @Override
    public void handle(ServerPlayer player) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().level == null)
                return;

            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            if (!(entity instanceof LivingEntity living))
                return;

            BrewinAndChewin.getHelper().setRagingAttachment(
                    living,
                    stacks.map(i -> new RagingAttachment(i, 0)).orElse(null)
            );
        });
    }
}
