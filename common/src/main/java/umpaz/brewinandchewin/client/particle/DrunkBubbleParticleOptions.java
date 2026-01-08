package umpaz.brewinandchewin.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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

   public static final ParticleOptions.Deserializer<DrunkBubbleParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
      public DrunkBubbleParticleOptions fromCommand(ParticleType<DrunkBubbleParticleOptions> particleType, StringReader stringReader) throws CommandSyntaxException {
         stringReader.expect(' ');

         // read three floats for RGB
         float r = stringReader.readFloat();
         stringReader.expect(' ');
         float g = stringReader.readFloat();
         stringReader.expect(' ');
         float b = stringReader.readFloat();
         stringReader.expect(' ');

         // read float for size
         float size = stringReader.readFloat();

         return new DrunkBubbleParticleOptions(new Vector3f(r, g, b), size);
      }

      public DrunkBubbleParticleOptions fromNetwork(ParticleType<DrunkBubbleParticleOptions> particleType, FriendlyByteBuf friendlyByteBuf) {
         return new DrunkBubbleParticleOptions(friendlyByteBuf.readVector3f(), friendlyByteBuf.readFloat());
      }
   };

   @Override
   public void writeToNetwork(FriendlyByteBuf buf) {
      buf.writeVector3f(this.color);
      buf.writeFloat(this.size);
   }
   private final Vector3f color;

   public static DrunkBubbleParticleOptions fromNetwork(FriendlyByteBuf buf) {
      return new DrunkBubbleParticleOptions(buf.readVector3f(), buf.readFloat());
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
