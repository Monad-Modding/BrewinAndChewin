package umpaz.brewinandchewin.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import umpaz.brewinandchewin.common.item.component.WineContents;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import umpaz.brewinandchewin.common.utility.BnCLabelUtils;
import umpaz.brewinandchewin.common.utility.BnCWineUtils;

import java.util.List;
import java.util.Optional;

public class WineItem extends Item {
    public static final int DRINK_DURATION = 32;
    public static final int FAST_DRINK_DURATION = 8;
    public static final int VOMIT_CLOUD_DURATION = 200;
    public static final int VOMIT_HUNGER_DURATION = 300;
    public static final int VOMIT_SLOWNESS_DURATION = 100;

    private final WineType type;

    public WineItem(WineType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public WineType getWineType() {
        return this.type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (BnCLabelUtils.hidesEffects(stack))
            return;
        List<MobEffectInstance> effects = BnCWineUtils.getDrinkEffects(this.type, BnCWineUtils.getContents(stack));
        PotionContents.addPotionTooltip(effects, tooltip::add, 1.0F, context.tickRate());
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return BnCWineUtils.getContents(stack).delicious() ? FAST_DRINK_DURATION : DRINK_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        WineContents contents = BnCWineUtils.getContents(stack);
        Player player = consumer instanceof Player playerConsumer ? playerConsumer : null;

        if (!level.isClientSide()) {
            List<MobEffectInstance> effects = BnCWineUtils.getDrinkEffects(this.type, contents);
            if (contents.sickening()) {
                vomit(level, consumer, effects);
            } else {
                for (MobEffectInstance instance : effects) {
                    if (instance.getEffect().value().isInstantenous()) {
                        instance.getEffect().value().applyInstantenousEffect(null, null, consumer, instance.getAmplifier(), 1.0D);
                    } else if (instance.is(BnCEffects.TIPSY)) {
                        applyTipsy(consumer, instance);
                    } else {
                        consumer.addEffect(new MobEffectInstance(instance));
                    }
                }
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        }
        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        ItemStack container = new ItemStack(Items.GLASS_BOTTLE);
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (stack.isEmpty()) {
            return container;
        }
        if (player != null && !player.getAbilities().instabuild && !player.getInventory().add(container)) {
            player.drop(container, false);
        }
        return stack;
    }

    private static void applyTipsy(LivingEntity consumer, MobEffectInstance instance) {
        MobEffectInstance existing = consumer.getEffect(BnCEffects.TIPSY);
        if (existing == null) {
            consumer.addEffect(new MobEffectInstance(instance));
            return;
        }
        consumer.addEffect(new MobEffectInstance(BnCEffects.TIPSY,
                existing.getDuration() == MobEffectInstance.INFINITE_DURATION ? MobEffectInstance.INFINITE_DURATION : existing.getDuration() + instance.getDuration(),
                Math.min(existing.getAmplifier() + instance.getAmplifier() + 1, 9),
                existing.isAmbient(), existing.isVisible(), existing.showIcon()));
    }

    public static void emptyStomach(LivingEntity consumer, boolean total) {
        if (!(consumer instanceof Player player))
            return;
        FoodData food = player.getFoodData();
        food.setFoodLevel(total ? 0 : food.getFoodLevel() / 2);
        food.setSaturation(0.0F);
    }

    private static void vomit(Level level, LivingEntity consumer, List<MobEffectInstance> effects) {
        Vec3 target = consumer.position().add(consumer.getLookAngle().scale(1.5D)).add(0.0D, consumer.getEyeHeight() * 0.5D, 0.0D);
        AreaEffectCloud cloud = new AreaEffectCloud(level, target.x(), target.y(), target.z());
        cloud.setOwner(consumer);
        cloud.setRadius(2.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(40);
        cloud.setDuration(VOMIT_CLOUD_DURATION);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());

        List<MobEffectInstance> cloudEffects = effects.stream()
                .filter(instance -> !instance.is(BnCEffects.TIPSY))
                .map(MobEffectInstance::new)
                .toList();
        cloud.setPotionContents(new PotionContents(Optional.empty(), Optional.empty(), cloudEffects));
        level.addFreshEntity(cloud);

        consumer.removeAllEffects();
        consumer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, VOMIT_SLOWNESS_DURATION, 3));
        emptyStomach(consumer, false);
        level.playSound(null, consumer.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.8F, 0.9F);
    }
}
