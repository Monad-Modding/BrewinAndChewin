package umpaz.brewinandchewin.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3fc;
import umpaz.brewinandchewin.common.registry.BnCParticleTypes;

public class DrunkBubbleParticleOptions extends ScalableParticleOptionsBase {
   public static final MapCodec<DrunkBubbleParticleOptions> CODEC = RecordCodecBuilder.mapCodec((p_253370_ ) -> p_253370_.group(
           ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(DrunkBubbleParticleOptions::getColor),
           Codec.FLOAT.fieldOf("scale").forGetter(DrunkBubbleParticleOptions::getScale)
   ).apply(p_253370_, DrunkBubbleParticleOptions::new));
   public static final StreamCodec<RegistryFriendlyByteBuf, DrunkBubbleParticleOptions> STREAM_CODEC = StreamCodec.composite(
           ByteBufCodecs.VECTOR3F, DrunkBubbleParticleOptions::getColor,
           ByteBufCodecs.FLOAT, DrunkBubbleParticleOptions::getScale,
           DrunkBubbleParticleOptions::new
   );
   private final Vector3fc color;


   public DrunkBubbleParticleOptions(Vector3fc color, float size) {
      super(size);
      this.color = color;
   }

   public Vector3fc getColor() {
       return color;
   }

   @Override
   public ParticleType<DrunkBubbleParticleOptions> getType() {
      return BnCParticleTypes.DRUNK_BUBBLE;
   }

}
