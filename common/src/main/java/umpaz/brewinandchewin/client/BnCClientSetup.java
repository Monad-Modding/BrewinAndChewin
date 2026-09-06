package umpaz.brewinandchewin.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
import umpaz.brewinandchewin.client.renderer.BottleRackBlockEntityRenderer;
import umpaz.brewinandchewin.client.renderer.CoasterBlockEntityRenderer;
import umpaz.brewinandchewin.client.renderer.texture.BnCTextureModifiers;
import umpaz.brewinandchewin.client.renderer.texture.modifier.TextureModifier;
import umpaz.brewinandchewin.client.utility.IdentifiableListener;
import umpaz.brewinandchewin.common.block.entity.BottleRackBlockEntity;
import umpaz.brewinandchewin.common.block.entity.CoasterBlockEntity;
import umpaz.brewinandchewin.common.item.WineItem;
import umpaz.brewinandchewin.common.item.WineType;
import umpaz.brewinandchewin.common.mixin.client.ModelBakeryAccessor;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.registry.BnCParticleTypes;
import umpaz.brewinandchewin.common.utility.BnCLabelUtils;
import umpaz.brewinandchewin.common.utility.BnCWineUtils;
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
        consumer.accept(BnCBlockEntityTypes.BOTTLE_RACK, BottleRackBlockEntityRenderer::new);
    }

    public static void appendLabelTooltip(ItemStack stack, List<Component> tooltip, float tickRate) {
        BnCLabelUtils.appendLabelTooltip(stack, tooltip, tickRate);
    }

    public static List<ResourceLocation> getBottleRackModels() {
        List<ResourceLocation> models = new ArrayList<>();
        for (Item item : BnCItems.CREATIVE_TAB_ITEMS) {
            if (!(item instanceof WineItem wine))
                continue;
            models.add(BottleRackBlockEntityRenderer.bottleModel(wine, false));
            models.add(BottleRackBlockEntityRenderer.bottleModel(wine, true));
        }
        for (int slot = 0; slot < BottleRackBlockEntity.SLOT_COUNT; ++slot) {
            models.add(BottleRackBlockEntityRenderer.labelModel(slot));
        }
        return models;
    }

    public static final ResourceLocation WINE_FOAM_PROPERTY = BrewinAndChewin.asResource("foam");
    public static final ResourceLocation WINE_LABELLED_PROPERTY = BrewinAndChewin.asResource("labelled");
    public static final ResourceLocation WINE_FINELY_AGED_PROPERTY = BrewinAndChewin.asResource("finely_aged");
    public static final ResourceLocation WINE_VARIANT_PROPERTY = BrewinAndChewin.asResource("variant");

    public static void registerItemProperties() {
        BnCClientPlatformHelper helper = BrewinAndChewinClient.getHelper();
        for (Item item : BnCItems.CREATIVE_TAB_ITEMS) {
            if (!(item instanceof WineItem wine))
                continue;
            helper.registerItemProperty(item, WINE_FOAM_PROPERTY,
                    (stack, level, entity, seed) -> BnCWineUtils.getContents(stack).hasFoam() ? 1.0F : 0.0F);
            helper.registerItemProperty(item, WINE_LABELLED_PROPERTY,
                    (stack, level, entity, seed) -> BnCLabelUtils.getLabel(stack).isPresent() ? 1.0F : 0.0F);
            helper.registerItemProperty(item, WINE_FINELY_AGED_PROPERTY,
                    (stack, level, entity, seed) -> BnCWineUtils.getContents(stack).isFinelyAged(wine.getWineType()) ? 1.0F : 0.0F);
            helper.registerItemProperty(item, WINE_VARIANT_PROPERTY,
                    (stack, level, entity, seed) -> BnCWineUtils.getContents(stack).variant() / (float) (WineType.OLD_WINE_VARIANTS - 1));
        }
    }

    public static void registerItemColorHandlers(BiConsumer<ItemColor, ItemLike> consumer) {
        ItemColor labelTint = (stack, tintIndex) ->
                tintIndex == 1 && BnCLabelUtils.getLabel(stack).isPresent() ? BnCLabelUtils.getLabelColor(stack) : -1;
        for (Item item : BnCItems.CREATIVE_TAB_ITEMS) {
            if (item instanceof WineItem)
                consumer.accept(labelTint, item);
        }
        consumer.accept((stack, tintIndex) -> tintIndex == 0 ? BnCLabelUtils.getDyeColor(stack) : -1, BnCItems.LABEL);
    }

    public static void registerRenderTypes(BiConsumer<Block, RenderType> consumer) {
        consumer.accept(BnCBlocks.AGING_CASK, RenderType.cutout());
        consumer.accept(BnCBlocks.BOTTLE_RACK, RenderType.cutout());
        consumer.accept(BnCBlocks.CORN_CROP, RenderType.cutout());
        consumer.accept(BnCBlocks.WILD_CORN, RenderType.cutout());
        consumer.accept(BnCBlocks.WILD_GRAPES, RenderType.cutout());
        consumer.accept(BnCBlocks.TRELLIS, RenderType.cutout());
        consumer.accept(BnCBlocks.TRELLIS_GRAPE, RenderType.cutout());
        consumer.accept(BnCBlocks.RED_GRAPE_BUSH, RenderType.cutout());
        consumer.accept(BnCBlocks.WHITE_GRAPE_BUSH, RenderType.cutout());
        consumer.accept(BnCBlocks.RED_GRAPE_STEM, RenderType.cutout());
        consumer.accept(BnCBlocks.WHITE_GRAPE_STEM, RenderType.cutout());
        consumer.accept(BnCBlocks.RED_ROPE_GRAPE, RenderType.cutout());
        consumer.accept(BnCBlocks.WHITE_ROPE_GRAPE, RenderType.cutout());
    }

    public static void registerParticles(BiConsumer<ParticleType<?>, ParticleEngine.SpriteParticleRegistration> consumer) {
        consumer.accept(BnCParticleTypes.FOG, SteamParticle.Factory::new);
        consumer.accept(BnCParticleTypes.DRUNK_BUBBLE, DrunkBubbleParticle.Factory::new);
        consumer.accept(BnCParticleTypes.RAGING_STAGE_1, RagingParticle.Factory::new);
        consumer.accept(BnCParticleTypes.RAGING_STAGE_2, RagingParticle.Factory::new);
        consumer.accept(BnCParticleTypes.RAGING_STAGE_3, RagingParticle.Factory::new);
        consumer.accept(BnCParticleTypes.RAGING_STAGE_4, RagingParticle.Factory::new);
    }

    public static void registerReloadListeners(Consumer<IdentifiableListener> consumer) {
        consumer.accept(BnCFluidItemDisplays.Loader.INSTANCE);
    }

    public static void registerColorHandlers(BiConsumer<BlockColor, Block> consumer) {
        consumer.accept((state, level, pos, pTintIndex) -> {
            if (level != null && pos != null && level.getBlockEntity(pos) instanceof CoasterBlockEntity blockEntity) {
                int tintIndex = -1;
                int count = 0;
                for (ItemStack item : blockEntity.getItems()) {
                    if (!item.isEmpty())
                        ++count;
                }
                for (int i = 0; i < count; i++) {
                    ItemStack stack = blockEntity.getItems().get(i);
                    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    List<CoasterBlockEntityRenderer.ModelEntry> modelEntries = CoasterBlockEntityRenderer.getModelEntries(itemId);

                    if (modelEntries != null) {
                        for (CoasterBlockEntityRenderer.ModelEntry modelEntry : modelEntries) {
                            int color = 0XFFFFFFFF;
                            for (TextureModifier modifier : modelEntry.modifiers()) {
                                color = modifier.color(level, state, pos, stack, color);
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

        consumer.accept((state, level, pos, tintIndex) -> {
            if (level == null || pos == null || !(level.getBlockEntity(pos) instanceof BottleRackBlockEntity rack))
                return -1;
            return BnCLabelUtils.getLabelColor(rack.getItem(tintIndex));
        }, BnCBlocks.BOTTLE_RACK);
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
                            ResourceLocation itemId = ResourceLocation.CODEC.decode(JsonOps.INSTANCE, jsonObject.get("item")).getOrThrow().getFirst();
                            List<CoasterBlockEntityRenderer.ModelEntry> modelEntries = CoasterBlockEntityRenderer.ModelEntry.LIST_CODEC.decode(JsonOps.INSTANCE, jsonObject.get("models")).getOrThrow().getFirst();
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
