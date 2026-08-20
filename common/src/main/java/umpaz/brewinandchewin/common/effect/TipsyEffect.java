package umpaz.brewinandchewin.common.effect;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;
import umpaz.brewinandchewin.client.particle.DrunkBubbleParticleOptions;

public class TipsyEffect extends MobEffect {
    public static final int FREEZE_IMMUNITY_AMPLIFIER = 2;

    public TipsyEffect() {
        super(MobEffectCategory.NEUTRAL, 13208334, getParticle(13208334));
    }

    public static ParticleOptions getParticle(int color) {
        return new DrunkBubbleParticleOptions(new Vector3f(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f), 0.25f);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (amplifier >= FREEZE_IMMUNITY_AMPLIFIER && entity.getTicksFrozen() > 0)
            entity.setTicksFrozen(0);
        return true;
    }
}