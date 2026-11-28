
package umpaz.brewinandchewin.common.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.attachment.RagingAttachment;

import java.util.Optional;

public record SyncRagingStacksClientboundPacket(int entityId, Optional<Integer> stacks) implements CustomPacketPayload {
    public static final Identifier ID = BrewinAndChewin.asResource("sync_raging_stacks");
    public static final Type<SyncRagingStacksClientboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncRagingStacksClientboundPacket> STREAM_CODEC = StreamCodec.of(SyncRagingStacksClientboundPacket::encode, SyncRagingStacksClientboundPacket::new);

    public SyncRagingStacksClientboundPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readOptional(FriendlyByteBuf::readInt));
    }

    public static void encode(RegistryFriendlyByteBuf buf, SyncRagingStacksClientboundPacket packet) {
        buf.writeInt(packet.entityId());
        buf.writeOptional(packet.stacks(), FriendlyByteBuf::writeInt);
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId());

            if (!(entity instanceof LivingEntity living))
                return;

            BrewinAndChewin.getHelper().setRagingAttachment(living, stacks.map(i -> new RagingAttachment(i, 0)).orElse(null));
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
