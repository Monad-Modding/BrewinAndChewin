package umpaz.brewinandchewin.common.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import umpaz.brewinandchewin.common.registry.BnCEffects;

public class DistillateItem extends Item implements Distillate {
    public static final int DRINK_DURATION = 32;
    public static final int FAST_DRINK_DURATION = 8;
    public static final int CLEANSE_SLOWNESS_DURATION = 100;

    private final int tipsyBonus;
    private final int tipsyDuration;
    private final boolean sickening;
    private final boolean delicious;

    public DistillateItem(int tipsyBonus, int tipsyDuration, boolean sickening, boolean delicious, Properties properties) {
        super(properties);
        this.tipsyBonus = tipsyBonus;
        this.tipsyDuration = tipsyDuration;
        this.sickening = sickening;
        this.delicious = delicious;
    }

    @Override
    public int getTipsyBonus(ItemStack stack) {
        return this.tipsyBonus;
    }

    @Override
    public boolean isSickening(ItemStack stack) {
        return this.sickening;
    }

    @Override
    public boolean isDelicious(ItemStack stack) {
        return this.delicious;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return this.delicious ? FAST_DRINK_DURATION : DRINK_DURATION;
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
        if (!level.isClientSide()) {
            if (this.sickening) {
                consumer.removeAllEffects();
                consumer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, CLEANSE_SLOWNESS_DURATION, 2));
                WineItem.emptyStomach(consumer);
                level.playSound(null, consumer.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.8F, 0.9F);
            }
            if (this.tipsyBonus > 0) {
                MobEffectInstance existing = consumer.getEffect(BnCEffects.TIPSY);
                int amplifier = existing == null ? this.tipsyBonus - 1 : existing.getAmplifier() + this.tipsyBonus;
                int duration = existing == null || existing.getDuration() == MobEffectInstance.INFINITE_DURATION
                        ? this.tipsyDuration : existing.getDuration() + this.tipsyDuration;
                consumer.addEffect(new MobEffectInstance(BnCEffects.TIPSY, duration, Math.min(amplifier, 9)));
            }
        }

        Player player = consumer instanceof Player playerConsumer ? playerConsumer : null;
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
}
