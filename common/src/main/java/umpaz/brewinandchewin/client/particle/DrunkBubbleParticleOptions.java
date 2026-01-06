package umpaz.brewinandchewin.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;
import umpaz.brewinandchewin.common.registry.BnCParticleTypes;

public class DrunkBubbleParticleOptions extends ScalableParticleOptionsBase {
   public static final MapCodec<DrunkBubbleParticleOptions> CODEC = RecordCodecBuilder.mapCodec((particle ) -> particle.group(
           ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(DrunkBubbleParticleOptions::getColor),
           Codec.FLOAT.fieldOf("scale").forGetter(DrunkBubbleParticleOptions::getScale)
   ).apply(particle, DrunkBubbleParticleOptions::new));

   @Override
   public void writeToNetwork(FriendlyByteBuf buf) {
      buf.writeVector3f(this.color);
      buf.writeFloat(this.size);
   }
   private final Vector3f color;

   public static DrunkBubbleParticleOptions fromNetwork(FriendlyByteBuf buf) {
      Vector3f color = buf.readVector3f();
      float scale = buf.readFloat();
      return new DrunkBubbleParticleOptions(color, scale);
   }

   public DrunkBubbleParticleOptions(Vector3f color, float size) {
      super(size);
      this.color = color;
   }

   public Vector3f getColor() {
       return color;
   }

   @Override
   public ParticleType<DrunkBubbleParticleOptions> getType() {
      return BnCParticleTypes.DRUNK_BUBBLE;
   }

}
