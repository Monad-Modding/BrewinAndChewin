
package umpaz.brewinandchewin.common.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.attachment.RagingAttachment;
import umpaz.brewinandchewin.common.attachment.TipsyHeartsAttachment;
import umpaz.brewinandchewin.platform.BnCPacket;

public record SyncNumbedHeartsClientboundPacket(int entityId, float numbedHealth, int ticksUntilDamage) implements BnCPacket {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("sync_numbed_hearts");

    public SyncNumbedHeartsClientboundPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readFloat(), buf.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeFloat(numbedHealth);
        buf.writeInt(ticksUntilDamage);
    }

    @Override
    public void handle(ServerPlayer player) {
        Minecraft.getInstance().execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId());

            if (!(entity instanceof LivingEntity living))
                return;

            BrewinAndChewin.getHelper().setTipsyHeartsAttachment(living, numbedHealth < 1.0E-5F ? null :new TipsyHeartsAttachment(numbedHealth, ticksUntilDamage));
        });
    }
}
