package umpaz.brewinandchewin.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.gui.KegTooltip;
import umpaz.brewinandchewin.client.particle.DrunkBubbleParticle;
import umpaz.brewinandchewin.client.particle.RagingParticle;
import umpaz.brewinandchewin.client.renderer.CoasterBlockEntityRenderer;
import umpaz.brewinandchewin.client.renderer.texture.BnCTextureModifiers;
import umpaz.brewinandchewin.client.renderer.texture.modifier.TextureModifier;
import umpaz.brewinandchewin.client.utility.IdentifiableListener;
import umpaz.brewinandchewin.common.block.entity.CoasterBlockEntity;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.registry.BnCParticleTypes;
import umpaz.brewinandchewin.platform.client.BnCClientPlatformHelper;
import vectorwing.farmersdelight.client.particle.SteamParticle;

import java.io.Reader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BnCClientSetup {

    public static void registerBlockEntityRenderers(BiConsumer<BlockEntityType<?>, BlockEntityRendererProvider> consumer) {
        consumer.accept(BnCBlockEntityTypes.COASTER, CoasterBlockEntityRenderer::new);
    }

    public static void registerRenderTypes(BiConsumer<Block, ChunkSectionLayer> consumer) {
    }

    @FunctionalInterface
    public interface ParticleFactory<T extends net.minecraft.core.particles.ParticleOptions> {
        net.minecraft.client.particle.ParticleProvider<T> create(net.minecraft.client.particle.SpriteSet sprites);
    }

    public static void registerParticles(BiConsumer<ParticleType<?>, ParticleFactory<?>> consumer) {
        registerParticle(consumer, BnCParticleTypes.FOG, SteamParticle.Factory::new);
        registerParticle(consumer, BnCParticleTypes.DRUNK_BUBBLE, DrunkBubbleParticle.Factory::new);
        registerParticle(consumer, BnCParticleTypes.RAGING_STAGE_1, RagingParticle.Factory::new);
        registerParticle(consumer, BnCParticleTypes.RAGING_STAGE_2, RagingParticle.Factory::new);
        registerParticle(consumer, BnCParticleTypes.RAGING_STAGE_3, RagingParticle.Factory::new);
        registerParticle(consumer, BnCParticleTypes.RAGING_STAGE_4, RagingParticle.Factory::new);
    }

    private static <T extends net.minecraft.core.particles.ParticleOptions> void registerParticle(BiConsumer<ParticleType<?>, ParticleFactory<?>> consumer, ParticleType<T> type, ParticleFactory<T> factory) {
        consumer.accept(type, factory);
    }

    public static void registerReloadListeners(Consumer<IdentifiableListener> consumer) {
        consumer.accept(BnCFluidItemDisplays.Loader.INSTANCE);
    }

    public static final Set<Identifier> MODELS = new HashSet<>();

    public static CompletableFuture<List<Identifier>> getModels(ResourceManager manager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            ArrayList<Identifier> models = new ArrayList<>();

            for (Map.Entry<Identifier, Resource> resourceEntry : manager.listResources("brewinandchewin/coaster", fileName -> fileName.getPath().endsWith(".json")).entrySet()) {
                models.addAll(CompletableFuture.supplyAsync(() -> {
                    try {
                        Reader reader = resourceEntry.getValue().openAsReader();
                        JsonElement json = JsonParser.parseReader(reader);
                        reader.close();
                        if (json instanceof JsonObject jsonObject) {
                            Identifier itemId = Identifier.CODEC.decode(JsonOps.INSTANCE, jsonObject.get("item")).getOrThrow().getFirst();
                            List<CoasterBlockEntityRenderer.ModelEntry> modelEntries = CoasterBlockEntityRenderer.ModelEntry.LIST_CODEC.decode(JsonOps.INSTANCE, jsonObject.get("models")).getOrThrow().getFirst();
                            CoasterBlockEntityRenderer.addToModelMap(itemId, modelEntries);
                            return modelEntries.stream().map(CoasterBlockEntityRenderer.ModelEntry::model).toList();
                        }
                    } catch (Exception ex) {
                        BrewinAndChewin.LOG.error("Unexpected error in Brewin' And Chewin' coaster model JSON \"{}\". {}", resourceEntry.getKey(), ex);
                        return List.<Identifier>of();
                    }
                    BrewinAndChewin.LOG.error("Unexpected error in Brewin' And Chewin' coaster model JSON: {}.", resourceEntry.getKey());
                    return List.<Identifier>of();
                }, executor).join());
            }
            List<Identifier> modelPaths = models.stream().filter(Objects::nonNull).toList();
            MODELS.addAll(modelPaths);
            return modelPaths;
        });
    }
}
