package umpaz.brewinandchewin.client.particle;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

public abstract class ScalableParticleOptionsBase implements ParticleOptions {
    protected final float size;

    public ScalableParticleOptionsBase(float size) {
        this.size = size;
    }

    public float getScale() {
        return size;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeFloat(size);
    }

    public String writeToString(){
        return getType() + " " + this.size;
    }

    @Override
    public abstract ParticleType<?> getType();
}
