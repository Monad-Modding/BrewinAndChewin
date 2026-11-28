
package umpaz.brewinandchewin.common.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.attachment.TipsyHeartsAttachment;

public record SyncNumbedHeartsClientboundPacket(int entityId, float numbedHealth, int ticksUntilDamage) implements CustomPacketPayload {
    public static final Identifier ID = BrewinAndChewin.asResource("sync_numbed_hearts");
    public static final Type<SyncNumbedHeartsClientboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncNumbedHeartsClientboundPacket> STREAM_CODEC = StreamCodec.of(SyncNumbedHeartsClientboundPacket::encode, SyncNumbedHeartsClientboundPacket::new);

    public SyncNumbedHeartsClientboundPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readFloat(), buf.readInt());
    }

    public static void encode(RegistryFriendlyByteBuf buf, SyncNumbedHeartsClientboundPacket packet) {
        buf.writeInt(packet.entityId());
        buf.writeFloat(packet.numbedHealth());
        buf.writeInt(packet.ticksUntilDamage());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId());

            if (!(entity instanceof LivingEntity living))
                return;

            BrewinAndChewin.getHelper().setTipsyHeartsAttachment(living, numbedHealth < 1.0E-5F ? null :new TipsyHeartsAttachment(numbedHealth, ticksUntilDamage));
        });
    }
}
