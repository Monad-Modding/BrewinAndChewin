package umpaz.brewinandchewin.common.registry;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.particle.DrunkBubbleParticleOptions;
import umpaz.brewinandchewin.client.particle.RagingParticleOptions;

public class BnCParticleTypes {
    public static final SimpleParticleType FOG = FabricParticleTypes.simple(true);
    public static final ParticleType<RagingParticleOptions.StageOne> RAGING_STAGE_1 = new ParticleType<>(false) {
        @Override
        public MapCodec<RagingParticleOptions.StageOne> codec() {
            return RagingParticleOptions.StageOne.CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, RagingParticleOptions.StageOne> streamCodec() {
            return RagingParticleOptions.StageOne.STREAM_CODEC;
        }
    };
    public static final ParticleType<RagingParticleOptions.StageTwo> RAGING_STAGE_2 = new ParticleType<>(false) {
        @Override
        public MapCodec<RagingParticleOptions.StageTwo> codec() {
            return RagingParticleOptions.StageTwo.CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, RagingParticleOptions.StageTwo> streamCodec() {
            return RagingParticleOptions.StageTwo.STREAM_CODEC;
        }
    };
    public static final ParticleType<RagingParticleOptions.StageThree> RAGING_STAGE_3 = new ParticleType<>(false) {
        @Override
        public MapCodec<RagingParticleOptions.StageThree> codec() {
            return RagingParticleOptions.StageThree.CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, RagingParticleOptions.StageThree> streamCodec() {
            return RagingParticleOptions.StageThree.STREAM_CODEC;
        }
    };
    public static final ParticleType<RagingParticleOptions.StageFour> RAGING_STAGE_4 = new ParticleType<>(false) {
        @Override
        public MapCodec<RagingParticleOptions.StageFour> codec() {
            return RagingParticleOptions.StageFour.CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, RagingParticleOptions.StageFour> streamCodec() {
            return RagingParticleOptions.StageFour.STREAM_CODEC;
        }
    };

    public static final ParticleType<DrunkBubbleParticleOptions> DRUNK_BUBBLE = new ParticleType<>(false) {
        @Override
        public MapCodec<DrunkBubbleParticleOptions> codec() {
            return DrunkBubbleParticleOptions.CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, DrunkBubbleParticleOptions> streamCodec() {
            return DrunkBubbleParticleOptions.STREAM_CODEC;
        }
    };

    public static void registerAll() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, BrewinAndChewin.asResource("drunk_bubble"), DRUNK_BUBBLE);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, BrewinAndChewin.asResource("fog"), FOG);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, BrewinAndChewin.asResource("raging_stage_1"), RAGING_STAGE_1);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, BrewinAndChewin.asResource("raging_stage_2"), RAGING_STAGE_2);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, BrewinAndChewin.asResource("raging_stage_3"), RAGING_STAGE_3);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, BrewinAndChewin.asResource("raging_stage_4"), RAGING_STAGE_4);
    }
}
