package umpaz.brewinandchewin.common.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import umpaz.brewinandchewin.BrewinAndChewin;

public class TipsyHeartsAttachment {
    public static final Identifier ID = BrewinAndChewin.asResource("tipsy_hearts");
    public static final Codec<TipsyHeartsAttachment> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.fieldOf("numbed_health").forGetter(TipsyHeartsAttachment::getNumbedHealth),
            Codec.INT.fieldOf("ticks_until_damage").forGetter(TipsyHeartsAttachment::getTicksUntilDamage)
    ).apply(inst, TipsyHeartsAttachment::new));


    private float numbedHealth;
    private int ticksUntilDamage;

    public TipsyHeartsAttachment(float numbedHealth, int ticksUntilDamage) {
        this.numbedHealth = numbedHealth;
        this.ticksUntilDamage = ticksUntilDamage;
    }

    public float getNumbedHealth() {
        return numbedHealth;
    }

    public void setNumbedHealth(float value) {
        numbedHealth = value;
    }

    public int getTicksUntilDamage() {
        return ticksUntilDamage;
    }

    public void setTicksUntilDamage(int value) {
        ticksUntilDamage = value;
    }

    public void setFrom(TipsyHeartsAttachment cap) {
        if (cap == null)
            return;
        numbedHealth = cap.numbedHealth;
        ticksUntilDamage = cap.ticksUntilDamage;
    }
}
