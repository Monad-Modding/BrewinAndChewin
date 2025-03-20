package umpaz.brewinandchewin.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.utility.TextUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class BoozeItem extends Item {
    private final Fluid fluid;

    public BoozeItem(Fluid fluid, Properties properties) {
        super(properties);
        this.fluid = fluid;
    }

    public Fluid getFluid() {
        return this.fluid;
    }

    public InteractionResultHolder<ItemStack> use( Level level, Player player, InteractionHand hand ) {
        ItemStack heldStack = player.getItemInHand(hand);
       if ( heldStack.isEdible() ) {
          if ( player.canEat(heldStack.getFoodProperties(player).canAlwaysEat()) ) {
             player.startUsingItem(hand);
             return InteractionResultHolder.consume(heldStack);
          }
          else {
             return InteractionResultHolder.fail(heldStack);
          }
       }
       else {
          return ItemUtils.startUsingInstantly(level, player, hand);
       }
    }


    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        if (!level.isClientSide) {
            var tipsy = stack.getFoodProperties(consumer).getEffects().stream().filter(pair -> pair.getFirst().getEffect() == BnCEffects.TIPSY.get()).findFirst();
            this.affectConsumer(consumer, tipsy.map(pair -> pair.getFirst().getDuration()).orElse(0), tipsy.map(pair -> pair.getFirst().getAmplifier()).orElse(-1));
        }
        ItemStack containerStack = stack.getCraftingRemainingItem();
        Player player;
        if (stack.isEdible()) {
            super.finishUsingItem(stack, level, consumer);
        } else {
            player = consumer instanceof Player ? (Player)consumer : null;
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, stack);
            }
            if (player != null) {
                player.awardStat(Stats.ITEM_USED.get(this));
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }
        if (stack.isEmpty()) {
            return containerStack;
        } else {
            if (consumer instanceof Player) {
                player = (Player)consumer;
                if (!((Player)consumer).getAbilities().instabuild && !player.getInventory().add(containerStack)) {
                    player.drop(containerStack, false);
                }
            }
            return stack;
        }
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    //Tipsy Stuff
    public void affectConsumer(LivingEntity consumer, int duration, int potency) {
       if (consumer.hasEffect(BnCEffects.TIPSY.get())) {
           MobEffectInstance effect = consumer.getEffect(BnCEffects.TIPSY.get());
           consumer.addEffect(new MobEffectInstance(BnCEffects.TIPSY.get(), effect.getDuration() == -1 ? -1 : effect.getDuration() + duration, Math.min(effect.getAmplifier() + potency + 1, 9), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
       }
    }

    public static final Set<Supplier<MobEffect>> RED_EFFECTS = Set.of(BnCEffects.TIPSY, () -> MobEffects.BAD_OMEN);

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (!Configuration.FOOD_EFFECT_TOOLTIP.get())
            return;
        TextUtils.addFoodEffectTooltip(stack, tooltip, 1.0F);
        for (int i = 0; i < tooltip.size(); ++i) {
            Component component = tooltip.get(i);
            if (RED_EFFECTS.stream().anyMatch(supplier -> component.contains(Component.translatable(supplier.get().getDescriptionId())))) {
                tooltip.set(i, component.copy().withStyle(ChatFormatting.RED));
            }
        }
    }
}
