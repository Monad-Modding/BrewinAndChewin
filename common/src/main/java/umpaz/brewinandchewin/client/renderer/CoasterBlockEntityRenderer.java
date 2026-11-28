package umpaz.brewinandchewin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import umpaz.brewinandchewin.client.renderer.texture.BnCTextureModifiers;
import umpaz.brewinandchewin.client.renderer.texture.modifier.TextureModifier;
import umpaz.brewinandchewin.common.block.CoasterBlock;
import umpaz.brewinandchewin.common.block.entity.CoasterBlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class CoasterBlockEntityRenderer implements BlockEntityRenderer<CoasterBlockEntity, CoasterBlockEntityRenderer.CoasterRenderState> {
    private static final Map<Identifier, List<ModelEntry>> ITEM_TO_MODELS = new HashMap<>();
    private static final Set<Identifier> ERRONEOUS_ENTRIES  = new HashSet<>();

    public static void resetCache() {
        ITEM_TO_MODELS.clear();
        ERRONEOUS_ENTRIES.clear();
    }

    public static List<ModelEntry> getModelEntries(Identifier itemId) {
        return ITEM_TO_MODELS.get(itemId);
    }

    public static void addToModelMap(Identifier itemId, List<ModelEntry> models) {
        ITEM_TO_MODELS.put(itemId, models);
    }

    private final RandomSource random = new LegacyRandomSource(0L);
    private final ItemModelResolver itemModelResolver;

    public CoasterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    // there is definitely a better way to do this, but it felt better to do this than what was there
    private void poseUtil(PoseStack poseStack, int fullCount, int curCount, RandomSource seededRandom, boolean invisible) {
        Vector2f translateVec = switch (fullCount) {
            case 2:
                if (curCount == 0) {
                    yield new Vector2f(-0.20F, -0.15F);
                } else {
                    yield new Vector2f(0.20F, 0.15F);
                }
            case 3:
                if (curCount == 0) {
                    yield new Vector2f(0.05F, 0.25F);
                } else if (curCount == 1) {
                    yield new Vector2f(-0.25F, -0.15F);
                } else {
                    yield new Vector2f(0.25F, -0.25F);
                }
            case 4:
                if (curCount == 0) {
                    yield new Vector2f(0.20F, 0.25F);
                } else if (curCount == 1) {
                    yield new Vector2f(-0.25F, 0.20F);
                } else if (curCount == 2) {
                    yield new Vector2f(0.25F, -0.20F);
                } else {
                    yield new Vector2f(-0.20F, -0.25F);
                }
            default:
                yield new Vector2f();
        };

        float rotation = switch (fullCount) {
            case 2:
                if (curCount == 0) {
                    yield 190;
                } else {
                    yield 10;
                }
            case 3:
                if (curCount == 0) {
                    yield -20;
                } else if (curCount == 1) {
                    yield 220;
                } else {
                    yield 100;
                }
            case 4:
                if (curCount == 0) {
                    yield -5;
                } else if (curCount == 1) {
                    yield 265;
                } else if (curCount == 2) {
                    yield 85;
                } else {
                    yield 175;
                }
            default: yield 0;
        };

        poseStack.rotateAround(new Quaternionf().fromAxisAngleDeg(0, 1, 0, rotation + seededRandom.nextFloat() * 20.0f - 10.0f), .5f + translateVec.x(), 0f, .5f + translateVec.y());

        poseStack.translate(translateVec.x(),  invisible ? 0 : 1.0 / 16f, translateVec.y());
    }

    public static class CoasterRenderState extends BlockEntityRenderState {
        public final List<ItemStackRenderState> items = new ArrayList<>();
        public boolean invisible;
        public long posSeed;
    }

    @Override
    public CoasterRenderState createRenderState() {
        return new CoasterRenderState();
    }

    @Override
    public void extractRenderState(CoasterBlockEntity blockEntity, CoasterRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, breakProgress);
        state.items.clear();
        BlockState blockState = blockEntity.getBlockState();
        state.invisible = blockState.hasProperty(CoasterBlock.INVISIBLE) && blockState.getValue(CoasterBlock.INVISIBLE);
        state.posSeed = blockEntity.getBlockPos().asLong();
        for (ItemStack stack : blockEntity.getItems()) {
            if (!stack.isEmpty()) {
                ItemStackRenderState itemState = new ItemStackRenderState();
                this.itemModelResolver.updateForTopItem(itemState, stack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, (int) state.posSeed);
                state.items.add(itemState);
            }
        }
    }

    @Override
    public void submit(CoasterRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int count = state.items.size();
        if (count == 0) return;
        this.random.setSeed(state.posSeed);
        for (int i = 0; i < count; i++) {
            ItemStackRenderState itemState = state.items.get(i);
            if (itemState.isEmpty()) continue;
            poseStack.pushPose();
            poseUtil(poseStack, count, i, this.random, state.invisible);
            poseStack.translate(0.5F, 0.25F, 0.5F);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            itemState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    public record ModelEntry(Identifier model, List<? extends TextureModifier> modifiers) {
        private static final Codec<ModelEntry> DIRECT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("model").forGetter(ModelEntry::model),
                BnCTextureModifiers.CODEC.listOf().optionalFieldOf("texture_modifiers", List.of()).forGetter(modelEntry -> (List)modelEntry.modifiers())
        ).apply(inst, ModelEntry::new));
        public static final Codec<List<ModelEntry>> LIST_CODEC = Codec.either(Identifier.CODEC, DIRECT_CODEC.listOf())
                .xmap(either -> either.map(resourceLocation -> List.of(new ModelEntry(resourceLocation, List.of())), Function.identity()), modelEntry -> {
                    if (modelEntry.size() == 1 && modelEntry.get(0).modifiers().isEmpty())
                        return Either.left(modelEntry.get(0).model());
                    return Either.right(modelEntry);
                });
    }
}