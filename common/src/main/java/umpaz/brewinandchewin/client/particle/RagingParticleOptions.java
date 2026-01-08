package umpaz.brewinandchewin.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.network.FriendlyByteBuf;
import umpaz.brewinandchewin.common.registry.BnCParticleTypes;

import java.util.function.Function;

public abstract class RagingParticleOptions extends ScalableParticleOptionsBase {

    public RagingParticleOptions(float size) {
        super(size);
    }

    public static <T extends RagingParticleOptions> MapCodec<T> createCodec(Function<T, Float> sizeFunction, Function<Float, T> constructor) {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.floatRange(0.0F, 1.0F).fieldOf("size").forGetter(sizeFunction)
        ).apply(inst, constructor));
    }

    public static StageOne readNetwork(FriendlyByteBuf buf) {
        return new StageOne(buf.readFloat());
    }

    public static class StageOne extends RagingParticleOptions {
        public static final MapCodec<StageOne> CODEC = createCodec(RagingParticleOptions::getScale, StageOne::new);

        public static final ParticleOptions.Deserializer<StageOne> DESERIALIZER = new ParticleOptions.Deserializer<>() {
            public StageOne fromCommand(ParticleType<StageOne> particleType, StringReader stringReader) throws CommandSyntaxException {
                stringReader.expect(' ');
                int i = stringReader.readInt();
                return new StageOne(i);
            }

            public StageOne fromNetwork(ParticleType<StageOne> particleType, FriendlyByteBuf friendlyByteBuf) {
                return new StageOne(friendlyByteBuf.readVarInt());
            }
        };

        public StageOne(float size) {
            super(size);
        }

        @Override
        public ParticleType<?> getType() {
            return BnCParticleTypes.RAGING_STAGE_1;
        }
    }

    public static class StageTwo extends RagingParticleOptions {
        public static final MapCodec<StageTwo> CODEC = createCodec(StageTwo::getScale, StageTwo::new);

        public static final ParticleOptions.Deserializer<StageTwo> DESERIALIZER = new ParticleOptions.Deserializer<>() {
            public StageTwo fromCommand(ParticleType<StageTwo> particleType, StringReader stringReader) throws CommandSyntaxException {
                stringReader.expect(' ');
                int i = stringReader.readInt();
                return new StageTwo(i);
            }

            public StageTwo fromNetwork(ParticleType<StageTwo> particleType, FriendlyByteBuf friendlyByteBuf) {
                return new StageTwo(friendlyByteBuf.readVarInt());
            }
        };

        public StageTwo(float size) {
            super(size);
        }

        @Override
        public ParticleType<?> getType() {
            return BnCParticleTypes.RAGING_STAGE_2;
        }
    }

    public static class StageThree extends RagingParticleOptions {
        public static final MapCodec<StageThree> CODEC = createCodec(StageThree::getScale, StageThree::new);

        public static final ParticleOptions.Deserializer<StageThree> DESERIALIZER = new ParticleOptions.Deserializer<>() {
            public StageThree fromCommand(ParticleType<StageThree> particleType, StringReader stringReader) throws CommandSyntaxException {
                stringReader.expect(' ');
                int i = stringReader.readInt();
                return new StageThree(i);
            }

            public StageThree fromNetwork(ParticleType<StageThree> particleType, FriendlyByteBuf friendlyByteBuf) {
                return new StageThree(friendlyByteBuf.readVarInt());
            }
        };

        public StageThree(float size) {
            super(size);
        }

        @Override
        public ParticleType<?> getType() {
            return BnCParticleTypes.RAGING_STAGE_3;
        }
    }

    public static class StageFour extends RagingParticleOptions {
        public static final MapCodec<StageFour> CODEC = createCodec(StageFour::getScale, StageFour::new);

        public static final ParticleOptions.Deserializer<StageFour> DESERIALIZER = new ParticleOptions.Deserializer<>() {
            public StageFour fromCommand(ParticleType<StageFour> particleType, StringReader stringReader) throws CommandSyntaxException {
                stringReader.expect(' ');
                int i = stringReader.readInt();
                return new StageFour(i);
            }

            public StageFour fromNetwork(ParticleType<StageFour> particleType, FriendlyByteBuf friendlyByteBuf) {
                return new StageFour(friendlyByteBuf.readVarInt());
            }
        };

        public StageFour(float size) {
            super(size);
        }

        @Override
        public ParticleType<?> getType() {
            return BnCParticleTypes.RAGING_STAGE_4;
        }
    }
}
