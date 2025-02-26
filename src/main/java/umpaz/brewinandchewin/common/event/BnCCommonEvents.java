package umpaz.brewinandchewin.common.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.capability.RagingCapability;
import umpaz.brewinandchewin.common.capability.TipsyNumbedHeartsCapability;
import umpaz.brewinandchewin.common.network.BnCNetworkHandler;
import umpaz.brewinandchewin.common.network.clientbound.ClearKegFluidContainerComponentsPacket;
import umpaz.brewinandchewin.common.registry.BnCDamageTypes;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import umpaz.brewinandchewin.common.tag.BnCTags;

@Mod.EventBusSubscriber(modid = BrewinAndChewin.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BnCCommonEvents {
    @SubscribeEvent
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity living) {
            event.addCapability(TipsyNumbedHeartsCapability.ID, new TipsyNumbedHeartsCapability(living));
            event.addCapability(RagingCapability.ID, new RagingCapability());
        }
    }

    @SubscribeEvent
    public static void onPlayerJoinLevel(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
            serverPlayer.getCapability(TipsyNumbedHeartsCapability.INSTANCE).ifPresent(cap -> cap.syncToPlayer(serverPlayer));
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity living && event.getEntity() instanceof ServerPlayer serverPlayer)
            living.getCapability(TipsyNumbedHeartsCapability.INSTANCE).ifPresent(cap -> cap.syncToPlayer(serverPlayer));
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            event.getOriginal().reviveCaps();

            event.getEntity().getCapability(TipsyNumbedHeartsCapability.INSTANCE).ifPresent(cap -> {
                cap.setFrom(event.getOriginal().getCapability(TipsyNumbedHeartsCapability.INSTANCE).orElse(null));
                cap.sync();
            });

            event.getOriginal().invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();
        living.getCapability(TipsyNumbedHeartsCapability.INSTANCE).ifPresent(cap -> {
            if (cap.getNumbedHealth() > 0.0) {
                if (cap.getTicksUntilDamage() > 0)
                    cap.setTicksUntilDamage(cap.getTicksUntilDamage() - 1);

                if ((cap.getTicksUntilDamage() <= 0 || !living.hasEffect(BnCEffects.TIPSY.get())) && !living.level().isClientSide) {
                    float health = living.getHealth() + living.getAbsorptionAmount();
                    int remainingHealth = Mth.ceil(Math.min(cap.getNumbedHealth() - (health % 1 > cap.getNumbedHealth() % 1 ? 1 : 0), health));
                    if (remainingHealth > 0)
                        living.hurt(living.damageSources().source(BnCDamageTypes.CARDIAC_ARREST), cap.getNumbedHealth());
                    cap.setNumbedHealth(0.0F);
                    cap.sync();
                }
            }
        });
        RagingCapability.tick(living);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingHurtEvent event) { // Use LivingHurtEvent so we can run before Protection enchantments, Resistance and Absorption.
        LivingEntity target = event.getEntity();
        if (!target.hasEffect(BnCEffects.TIPSY.get()) || event.getSource().is(BnCDamageTypes.CARDIAC_ARREST))
            return;
        int amplifier = target.getEffect(BnCEffects.TIPSY.get()).getAmplifier();
        float maximumNumbedHealth = Mth.clamp(Mth.floor((2 + (amplifier * 1.6F)) / 2) * 2, 1, target.getMaxHealth() - 2);
        target.getCapability(TipsyNumbedHeartsCapability.INSTANCE).ifPresent(cap -> {
            float numbedHealth = Math.min(cap.getNumbedHealth() + event.getAmount(), maximumNumbedHealth);
            if (numbedHealth - cap.getNumbedHealth() <= target.getHealth())
                event.setAmount(event.getAmount() - (numbedHealth - cap.getNumbedHealth()));
            cap.setNumbedHealth(numbedHealth);
            cap.setTicksUntilDamage(200 + 20 * amplifier);
            if (target instanceof Player)
                cap.sync();
        });
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) { // Use LivingDamageEvent to make sure that attack strength is correct.
        Entity attacker = event.getSource().getEntity();
        LivingEntity target = event.getEntity();

        if (attacker instanceof LivingEntity living && (!(living instanceof Player player) || player.getAttackStrengthScale(0.0F) > 0.8F) && living.hasEffect(BnCEffects.RAGING.get()) && event.getSource().is(BnCTags.TRIGGERS_RAGING)) {
            living.getCapability(RagingCapability.INSTANCE).ifPresent((RagingCapability cap) -> {
                int stacks = Math.min(4, cap.getStacks() + 1);
                if (stacks != cap.getStacks() && !target.level().isClientSide()) {
                    double heightAddition = living.getY(1.0D) - living.getY(0.5D);
                    ((ServerLevel) target.level()).sendParticles(RagingCapability.getParticleType(stacks, 0.5F), target.getX(), target.getY(0.5), target.getZ(), 12, target.getRandom().nextDouble() * 0.4 - 0.2, target.getRandom().nextDouble() * heightAddition * 2 - heightAddition, target.getRandom().nextDouble() * 0.4 - 0.2, 0.0);
                }
                cap.setStacks(stacks);
                cap.setTicksUntilReset(Mth.ceil(RagingCapability.RESET_TICK_MULTIPLIER * (living instanceof Player player ? player.getCurrentItemAttackStrengthDelay() : 30)));
            });
        }
    }

    @SubscribeEvent
    public static void sendClearFluidContainerTextComponents(OnDatapackSyncEvent event) {
        BnCNetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClearKegFluidContainerComponentsPacket());
    }
}

