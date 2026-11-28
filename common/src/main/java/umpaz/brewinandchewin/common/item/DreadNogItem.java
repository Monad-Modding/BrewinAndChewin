package umpaz.brewinandchewin.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DreadNogItem extends BoozeItem {
    public DreadNogItem(Supplier<Fluid> fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        if (!level.isClientSide()) {
            var badOmen = findEffect(stack, MobEffects.BAD_OMEN);
            this.affectConsumerBadOmen(consumer, badOmen.map(MobEffectInstance::getDuration).orElse(0), badOmen.map(MobEffectInstance::getAmplifier).orElse(-1));
        }
        return super.finishUsingItem(stack, level, consumer);
    }

    public void affectConsumerBadOmen(LivingEntity consumer, int duration, int potency) {
        if (consumer.hasEffect(MobEffects.BAD_OMEN)) {
            MobEffectInstance effect = consumer.getEffect(MobEffects.BAD_OMEN);
            consumer.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, effect.getDuration() == -1 ? -1 : Math.max(effect.getDuration(), duration), Math.min(effect.getAmplifier() + potency + 1, 4), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn) {
        List<Component> lines = new ArrayList<>();
        super.appendHoverText(stack, ctx, display, lines::add, flagIn);
        for (Component tt : lines) {
            if (tt.contains(MobEffects.BAD_OMEN.value().getDisplayName())) {
                tooltip.accept(BnCTextUtils.getTranslation("tooltip.dread_nog").withStyle(ChatFormatting.RED));
            } else {
                tooltip.accept(tt);
            }
        }
    }
}
