package umpaz.brewinandchewin.common.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.particle.RagingParticleOptions;
import umpaz.brewinandchewin.common.mixin.LivingEntityAccessor;
import umpaz.brewinandchewin.common.network.clientbound.SyncRagingStacksClientboundPacket;
import umpaz.brewinandchewin.common.registry.BnCEffects;

import java.util.Optional;
import java.util.UUID;

public class RagingAttachment {
    public static final float RESET_TICK_MULTIPLIER = 2.5F;
    public static final ResourceLocation ID = BrewinAndChewin.asResource("raging");
    public static final Codec<RagingAttachment> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("stacks").forGetter(RagingAttachment::getStacks),
            Codec.INT.fieldOf("ticks_until_reset").forGetter(RagingAttachment::getTicksUntilReset)
    ).apply(inst, RagingAttachment::new));

    private int stacks;
    private int ticksUntilReset;
    private int previousStacks = 0;

    public RagingAttachment(int stacks, int ticksUntilReset) {
        this.stacks = stacks;
        this.ticksUntilReset = ticksUntilReset;
    }

    public int getStacks() {
        return stacks;
    }

    public void setStacks(int value) {
        stacks = value;
    }

    public int getTicksUntilReset() {
        return ticksUntilReset;
    }

    public void setTicksUntilReset(int value) {
        ticksUntilReset = value;
    }

    private static final UUID RAGING_ATTRIBUTE_ID =
            UUID.fromString("3b5c1b5a-7c4b-4f67-9d42-9a8c1c7c9e21");

    public static void tick(LivingEntity living) {
        RagingAttachment attachment = BrewinAndChewin.getHelper().getRagingAttachment(living);
        if (attachment == null)
            return;

        if (!living.level().isClientSide()) {
            if (attachment.getTicksUntilReset() <= 0 && attachment.getStacks() > 0) {
                attachment.setStacks(attachment.getStacks() - 1);
                attachment.setTicksUntilReset(Mth.ceil(RESET_TICK_MULTIPLIER * (living instanceof Player player ? player.getCurrentItemAttackStrengthDelay() : 30)));
            }

            if (attachment.getStacks() <= 0 || !living.hasEffect(BnCEffects.RAGING.value())) {
                if (living.getAttributes().hasAttribute(Attributes.ATTACK_SPEED) && living.getAttributes().hasModifier(Attributes.ATTACK_SPEED, RAGING_ATTRIBUTE_ID)) {
                    living.getAttribute(Attributes.ATTACK_SPEED).removeModifier(RAGING_ATTRIBUTE_ID);
                }
                if (attachment.previousStacks != 0) {
                    attachment.previousStacks = 0;
                    BrewinAndChewin.getHelper().setRagingAttachment(living, null);
                    BrewinAndChewin.getHelper().sendClientboundTracking(living, new SyncRagingStacksClientboundPacket(living.getId(), Optional.empty()));
                }
                ((LivingEntityAccessor)living).brewinandchewin$invokeUpdateEffectVisibility();
                return;
            }

            attachment.setTicksUntilReset(attachment.getTicksUntilReset() - 1);
            if (attachment.previousStacks != attachment.stacks) {
                if (living.getAttributes().hasModifier(Attributes.ATTACK_SPEED, RAGING_ATTRIBUTE_ID))
                    living.getAttribute(Attributes.ATTACK_SPEED).removeModifier(RAGING_ATTRIBUTE_ID);
                living.getAttribute(Attributes.ATTACK_SPEED).addTransientModifier(new AttributeModifier("raging", Math.min(0.8, 0.05 * attachment.getStacks() + 0.025 * living.getEffect(BnCEffects.RAGING.value()).getAmplifier() * attachment.getStacks()), AttributeModifier.Operation.MULTIPLY_TOTAL));
                ((LivingEntityAccessor)living).brewinandchewin$invokeUpdateEffectVisibility();
                BrewinAndChewin.getHelper().sendClientboundTracking(living, new SyncRagingStacksClientboundPacket(living.getId(), Optional.of(attachment.getStacks())));
            }
            attachment.previousStacks = attachment.stacks;
        }
    }

    public static ParticleOptions getParticleType(int stacks, float size) {
        return switch (stacks) {
            case 0, 1 -> new RagingParticleOptions.StageOne(size);
            case 2 -> new RagingParticleOptions.StageTwo(size);
            case 3 -> new RagingParticleOptions.StageThree(size);
            default -> new RagingParticleOptions.StageFour(size);
        };
    }
}
