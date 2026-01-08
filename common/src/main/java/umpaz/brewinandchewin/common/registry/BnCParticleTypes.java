package umpaz.brewinandchewin.common.registry;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.BuiltInRegistries;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.particle.DrunkBubbleParticleOptions;
import umpaz.brewinandchewin.client.particle.RagingParticleOptions;

public class BnCParticleTypes {
    public static final SimpleParticleType FOG = FabricParticleTypes.simple();

    public static final ParticleType<RagingParticleOptions.StageOne> RAGING_STAGE_1 = new ParticleType<>(false, RagingParticleOptions.StageOne.DESERIALIZER) {
        @Override
        public Codec<RagingParticleOptions.StageOne> codec() {
            return RagingParticleOptions.StageOne.CODEC.codec();
        }
    };

    public static final ParticleType<RagingParticleOptions.StageTwo> RAGING_STAGE_2 = new ParticleType<>(false, RagingParticleOptions.StageTwo.DESERIALIZER) {
        @Override
        public Codec<RagingParticleOptions.StageTwo> codec() {
            return RagingParticleOptions.StageTwo.CODEC.codec();
        }
    };

    public static final ParticleType<RagingParticleOptions.StageThree> RAGING_STAGE_3 = new ParticleType<>(false, RagingParticleOptions.StageThree.DESERIALIZER) {
        @Override
        public Codec<RagingParticleOptions.StageThree> codec() {
            return RagingParticleOptions.StageThree.CODEC.codec();
        }
    };

    public static final ParticleType<RagingParticleOptions.StageFour> RAGING_STAGE_4 = new ParticleType<>(false, RagingParticleOptions.StageFour.DESERIALIZER) {
        @Override
        public Codec<RagingParticleOptions.StageFour> codec() {
            return RagingParticleOptions.StageFour.CODEC.codec();
        }
    };

    public static final ParticleType<DrunkBubbleParticleOptions> DRUNK_BUBBLE = new ParticleType<>(false, DrunkBubbleParticleOptions.DESERIALIZER) {
        @Override
        public Codec<DrunkBubbleParticleOptions> codec() {
            return DrunkBubbleParticleOptions.CODEC.codec();
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
