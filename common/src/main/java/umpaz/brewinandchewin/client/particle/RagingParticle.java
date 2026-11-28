package umpaz.brewinandchewin.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public class RagingParticle extends SingleQuadParticle {
    private static final RandomSource RANDOM = RandomSource.create();
    private final SpriteSet sprites;

    protected RagingParticle(ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ, float size, SpriteSet sprites) {
        super(level, x, y, z, 0.5D - RANDOM.nextDouble(), motionY, 0.5D - RANDOM.nextDouble(), sprites.first());
        this.friction = 0.96F;
        this.gravity = -0.1F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = sprites;
        this.yd *= 0.2F;
        if (motionX == 0.0D && motionZ == 0.0D) {
            this.xd *= 0.1F;
            this.zd *= 0.1F;
        }
        this.quadSize *= size;
        this.oRoll = 0.4F;
        this.roll = 0.4F;
        this.lifetime = (int) (8.0D / (Math.random() * 0.8D + 0.2D));
        this.hasPhysics = false;
        this.setSpriteFromAge(sprites);
        if (this.isCloseToScopingPlayer()) {
            this.setAlpha(0.0F);
        }
    }

    @Override
    @NotNull
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public void tick() {
        super.tick();
        if (random.nextFloat() < 0.2F)
            setSprite(sprites.get(random));
        oRoll = 0.4F;
        roll = 0.4F;
        if (this.isCloseToScopingPlayer()) {
            this.setAlpha(0.0F);
        } else {
            this.setAlpha(Mth.lerp(0.025F, alpha, 1.0F));
        }
    }

    private boolean isCloseToScopingPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localplayer = minecraft.player;
        return localplayer != null && localplayer.getEyePosition().distanceToSqr(this.x, this.y, this.z) <= 9.0D && minecraft.options.getCameraType().isFirstPerson() && localplayer.isScoping();
    }

    public static class Factory<T extends RagingParticleOptions> implements ParticleProvider<T> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(T typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new RagingParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, typeIn.getScale(), spriteSet);
        }
    }
}