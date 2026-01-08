package umpaz.brewinandchewin.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;

import java.util.List;
import java.util.function.Supplier;

public class DreadNogItem extends BoozeItem {
    public DreadNogItem(Supplier<Fluid> fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        if (!level.isClientSide) {
            var badOmen = BrewinAndChewin.getHelper().getFoodProperties(stack, consumer).getEffects().stream().filter(pair -> pair.getFirst().getEffect() == MobEffects.BAD_OMEN).findFirst();
            this.affectConsumerBadOmen(consumer, badOmen.map(pair -> pair.getFirst().getDuration()).orElse(0), badOmen.map(pair -> pair.getFirst().getAmplifier()).orElse(-1));
        }
        return super.finishUsingItem(stack, level, consumer);
    }

    public void affectConsumerBadOmen(LivingEntity consumer, int duration, int potency) {
        if (consumer.hasEffect(MobEffects.BAD_OMEN)) {
            MobEffectInstance effect = consumer.getEffect(MobEffects.BAD_OMEN);
            consumer.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, effect.getDuration() == -1 ? -1 : Math.max(effect.getDuration(), duration), Math.min(effect.getAmplifier() + potency + 1, 4), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
        }
    }

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, level, tooltip, flagIn);
        for (int i = 0; i < tooltip.size(); ++i) {
            Component tt = tooltip.get(i);
            if (tt.contains(MobEffects.BAD_OMEN.getDisplayName()))
                tooltip.set(i, BnCTextUtils.getTranslation("tooltip.dread_nog").withStyle(ChatFormatting.RED));
        }
    }
}
