package umpaz.brewinandchewin.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public class DrunkBubbleParticle extends SingleQuadParticle {
   protected DrunkBubbleParticle( ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ, TextureAtlasSprite sprite ) {
      super(level, x, y, z, sprite);
      this.scale(2.0F);
      this.setSize(0.25F, 0.25F);

      this.lifetime = this.random.nextInt(50) + 30;

      this.gravity = 3.0E-6F;
      this.xd = motionX;
      this.yd = motionY + (double) ( this.random.nextFloat() / 500.0F );
      this.zd = motionZ;
   }

   @Override
   @NotNull
   protected SingleQuadParticle.Layer getLayer() {
      return SingleQuadParticle.Layer.TRANSLUCENT;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if ( this.age++ < this.lifetime && !( this.alpha <= 0.0F ) ) {
         this.xd += this.random.nextFloat() / 2500.0F * (float) ( this.random.nextBoolean() ? 1 : -1 );
         this.zd += this.random.nextFloat() / 2500.0F * (float) ( this.random.nextBoolean() ? 1 : -1 );
         this.yd -= this.gravity;
         this.move(this.xd, this.yd, this.zd);
         if ( this.age >= this.lifetime - 60 && this.alpha > 0.01F ) {
            this.alpha -= 0.02F;
         }
      }
      else {
         this.remove();
      }
   }

   public static class Factory implements ParticleProvider<DrunkBubbleParticleOptions> {
      private final SpriteSet spriteSet;

      public Factory( SpriteSet sprite ) {
         this.spriteSet = sprite;
      }

      @Override
      public Particle createParticle( DrunkBubbleParticleOptions typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random ) {
         DrunkBubbleParticle particle = new DrunkBubbleParticle(level, x, y + 0.3D, z, 0.0, 0.002, 0.0, this.spriteSet.get(random));
         particle.setAlpha(0.6F);
         particle.setColor(typeIn.getColor().x(), typeIn.getColor().y(), typeIn.getColor().z());
         particle.scale(typeIn.getScale());
         return particle;
      }
   }
}