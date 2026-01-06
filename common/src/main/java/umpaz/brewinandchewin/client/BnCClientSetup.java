package umpaz.brewinandchewin.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
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
import umpaz.brewinandchewin.common.mixin.client.ModelBakeryAccessor;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.registry.BnCParticleTypes;
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

    public static void registerParticles() {

        ParticleFactoryRegistry.getInstance().register(BnCParticleTypes.FOG, SteamParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(BnCParticleTypes.DRUNK_BUBBLE, DrunkBubbleParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(BnCParticleTypes.RAGING_STAGE_1, RagingParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(BnCParticleTypes.RAGING_STAGE_2, RagingParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(BnCParticleTypes.RAGING_STAGE_3, RagingParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(BnCParticleTypes.RAGING_STAGE_4, RagingParticle.Factory::new);
    }

    public static void registerReloadListeners(Consumer<IdentifiableListener> consumer) {
        consumer.accept(BnCFluidItemDisplays.Loader.INSTANCE);
    }

    public static void registerColorHandlers(BiConsumer<BlockColor, Block> consumer) {
        consumer.accept((state, level, pos, pTintIndex) -> {
            if (level != null && pos != null && level.getBlockEntity(pos) instanceof CoasterBlockEntity blockEntity) {
                int tintIndex = -1;
                int count = (int) blockEntity.getItems().stream().filter(i -> !i.isEmpty()).count();
                for (int i = 0; i < count; i++) {
                    ItemStack stack = blockEntity.getItems().get(i);
                    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    List<CoasterBlockEntityRenderer.ModelEntry> modelEntries = CoasterBlockEntityRenderer.getModelEntries(itemId);

                    if (modelEntries != null) {
                        for (CoasterBlockEntityRenderer.ModelEntry modelEntry : modelEntries) {
                            int color = 0XFFFFFFFF;
                            for (int j = 0; j < modelEntry.modifiers().size(); ++j) {
                                for (TextureModifier modifier : modelEntry.modifiers()) {
                                    color = modifier.color(level, state, pos, stack, color);
                                }
                            }
                            if (color != -1) {
                                ++tintIndex;
                                if (tintIndex == pTintIndex)
                                    return color;
                            }
                        }
                    }
                }
            }
            return -1;
        }, BnCBlocks.COASTER);
    }

    public static final Set<ResourceLocation> MODELS = new HashSet<>();

    public static CompletableFuture<List<ResourceLocation>> getModels(ResourceManager manager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            ArrayList<ResourceLocation> models = new ArrayList<>();

            for (Map.Entry<ResourceLocation, Resource> resourceEntry : manager.listResources("brewinandchewin/coaster", fileName -> fileName.getPath().endsWith(".json")).entrySet()) {
                models.addAll(CompletableFuture.supplyAsync(() -> {
                    try {
                        Reader reader = resourceEntry.getValue().openAsReader();
                        JsonElement json = JsonParser.parseReader(reader);
                        reader.close();
                        if (json instanceof JsonObject jsonObject) {
                            ResourceLocation itemId = ResourceLocation.CODEC.decode(JsonOps.INSTANCE, jsonObject.get("item")).getOrThrow(false, RuntimeException::new).getFirst();
                            List<CoasterBlockEntityRenderer.ModelEntry> modelEntries = CoasterBlockEntityRenderer.ModelEntry.LIST_CODEC.decode(JsonOps.INSTANCE, jsonObject.get("models")).getOrThrow(false, RuntimeException::new).getFirst();
                            CoasterBlockEntityRenderer.addToModelMap(itemId, modelEntries);
                            return modelEntries.stream().map(CoasterBlockEntityRenderer.ModelEntry::model).toList();
                        }
                    } catch (Exception ex) {
                        BrewinAndChewin.LOG.error("Unexpected error in Brewin' And Chewin' coaster model JSON \"{}\". {}", resourceEntry.getKey(), ex);
                        return List.<ResourceLocation>of();
                    }
                    BrewinAndChewin.LOG.error("Unexpected error in Brewin' And Chewin' coaster model JSON: {}.", resourceEntry.getKey());
                    return List.<ResourceLocation>of();
                }, executor).join());
            }
            List<ResourceLocation> modelPaths = models.stream().filter(Objects::nonNull).toList();
            MODELS.addAll(modelPaths);
            return modelPaths;
        });
    }
}
