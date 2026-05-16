package umpaz.brewinandchewin.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.attachment.RagingAttachment;
import umpaz.brewinandchewin.common.attachment.TipsyHeartsAttachment;
import umpaz.brewinandchewin.common.mixin.LivingEntityAccessor;
import umpaz.brewinandchewin.common.network.clientbound.SyncNumbedHeartsClientboundPacket;
import umpaz.brewinandchewin.common.registry.BnCDamageTypes;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import umpaz.brewinandchewin.common.registry.BnCParticleTypes;
import umpaz.brewinandchewin.common.tag.BnCTags;
import umpaz.brewinandchewin.fabric.access.PlayerPreHurtAttackStrengthAccess;
import umpaz.brewinandchewin.fabric.registry.BnCAttachments;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void brewinandchewin$tickLivingEntity(CallbackInfo ci) {
        LivingEntity living = (LivingEntity)(Object)this;
        TipsyHeartsAttachment attachment = BrewinAndChewin.getHelper().getTipsyHeartsAttachment(living);
        if (attachment != null && attachment.getNumbedHealth() > 0.0) {
            if (attachment.getTicksUntilDamage() > 0)
                attachment.setTicksUntilDamage(attachment.getTicksUntilDamage() - 1);

            if ((attachment.getTicksUntilDamage() <= 0 || !living.hasEffect(BnCEffects.TIPSY)) && !living.level().isClientSide()) {
                float health = living.getHealth() + living.getAbsorptionAmount();
                int remainingHealth = Mth.ceil(Math.min(attachment.getNumbedHealth() - (health % 1 > attachment.getNumbedHealth() % 1 ? 1 : 0), health));
                if (remainingHealth > 0)
                    living.hurt(living.damageSources().source(BnCDamageTypes.CARDIAC_ARREST), attachment.getNumbedHealth());
                attachment.setNumbedHealth(0.0F);
                BrewinAndChewin.getHelper().sendClientboundTracking(living, new SyncNumbedHeartsClientboundPacket(living.getId(), attachment.getNumbedHealth(), attachment.getTicksUntilDamage()));
            }
        }
        RagingAttachment.tick(living);
    }

    @ModifyVariable(method = "hurt", at = @At("RETURN"), argsOnly = true)
    private float brewinandchewin$hurtLiving(float original, DamageSource source) {
        Entity attacker = source.getEntity();
        LivingEntity target = (LivingEntity) (Object) this;
        if (original > 0.0F) {
            if (target.hasEffect(BnCEffects.TIPSY) && !source.is(BnCDamageTypes.CARDIAC_ARREST)) {
                int amplifier = target.getEffect(BnCEffects.TIPSY).getAmplifier();
                float maximumNumbedHealth = Mth.clamp(Mth.floor((2 + (amplifier * 1.6F)) / 2) * 2, 1, target.getMaxHealth() - 2);
                if (BrewinAndChewin.getHelper().getTipsyHeartsAttachment(target) == null) {
                    BrewinAndChewin.getHelper().setTipsyHeartsAttachment(target, new TipsyHeartsAttachment(0, 0));
                }
                TipsyHeartsAttachment attachment = BrewinAndChewin.getHelper().getTipsyHeartsAttachment(target);
                float numbedHealth = Math.min(attachment.getNumbedHealth() + original, maximumNumbedHealth);
                if (numbedHealth - attachment.getNumbedHealth() <= target.getHealth())
                    original = original - (numbedHealth - attachment.getNumbedHealth());
                int ticksUntilDamage = 200 + 20 * amplifier;
                attachment.setNumbedHealth(numbedHealth);
                attachment.setTicksUntilDamage(ticksUntilDamage);
                BrewinAndChewin.getHelper().sendClientboundTracking(target, new SyncNumbedHeartsClientboundPacket(target.getId(), numbedHealth, ticksUntilDamage));
            }
            if (attacker instanceof LivingEntity living && (!(living instanceof PlayerPreHurtAttackStrengthAccess player) || player.brewinandchewin$getPreHurtAttackStrengthScale() > 0.8F) && living.hasEffect(BnCEffects.RAGING) && source.is(BnCTags.DamageTypes.TRIGGERS_RAGING)) {
                if (BrewinAndChewin.getHelper().getRagingAttachment(living) == null)
                    BrewinAndChewin.getHelper().setRagingAttachment(living, new RagingAttachment(0, 0));
                RagingAttachment attachment = BrewinAndChewin.getHelper().getRagingAttachment(living);
                int stacks = Math.min(4, attachment.getStacks() + 1);
                if (stacks != attachment.getStacks() && !target.level().isClientSide()) {
                    double heightAddition = living.getY(1.0D) - living.getY(0.5D);
                    ((ServerLevel) target.level()).sendParticles(RagingAttachment.getParticleType(stacks, 0.5F), target.getX(), target.getY(0.5), target.getZ(), 12, target.getRandom().nextDouble() * 0.4 - 0.2, target.getRandom().nextDouble() * heightAddition * 2 - heightAddition, target.getRandom().nextDouble() * 0.4 - 0.2, 0.0);
                }
                attachment.setStacks(stacks);
                ((LivingEntityAccessor) living).brewinandchewin$invokeUpdateEffectVisibility();
                attachment.setTicksUntilReset(Mth.ceil(RagingAttachment.RESET_TICK_MULTIPLIER * (living instanceof Player player ? player.getCurrentItemAttackStrengthDelay() : 30)));
            }
        }
        if (attacker instanceof PlayerPreHurtAttackStrengthAccess access)
            access.brewinandchewin$resetPreHurtAttackStrengthScale();
        return original;
    }

    @ModifyVariable(method = "updateSynchronizedMobEffectParticles", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/stream/Stream;toList()Ljava/util/List;"), name = "list")
    private List<ParticleOptions> brewinandchewin$setToRagingParticles(List<ParticleOptions> original) {
        LivingEntity living = (LivingEntity)(Object)this;
        return original.stream().map(particleOptions -> {
            if (particleOptions.getType() == BnCParticleTypes.RAGING_STAGE_1) {
                RagingAttachment attachment = living.getAttached(BnCAttachments.RAGING);
                return RagingAttachment.getParticleType(attachment != null ? attachment.getStacks() : 0, 0.75F);
            }
            return particleOptions;
        }).toList();
    }

    @ModifyReturnValue(method = "canBeAffected", at = @At("RETURN"))
    private boolean brewinandchewin$intoxicationImmunity(boolean original, MobEffectInstance mobEffectInstance) {
        if (((LivingEntity)(Object)this).getType().is(BnCTags.EntityTypes.IMMUNE_TO_INTOXICATION) && mobEffectInstance.getEffect().is(BnCEffects.INTOXICATION))
            return false;
        return original;
    }
}
