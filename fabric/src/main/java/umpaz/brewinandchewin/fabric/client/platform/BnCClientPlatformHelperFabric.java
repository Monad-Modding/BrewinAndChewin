package umpaz.brewinandchewin.fabric.client.platform;

import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.fabric.client.model.CoasterWrappedModel;
import umpaz.brewinandchewin.platform.client.BnCClientPlatformHelper;

public class BnCClientPlatformHelperFabric implements BnCClientPlatformHelper {
    @Override
    public BakedModel getModel(ResourceLocation modelId) {
        return Minecraft.getInstance().getModelManager().getModel(modelId);
    }

    @Override
    public void registerItemProperty(Item item, ResourceLocation id, ClampedItemPropertyFunction function) {
        FabricModelPredicateProviderRegistry.register(item, id, function);
    }

    @Override
    public void tesselateModel(BlockAndTintGetter level, ResourceLocation modelId, BlockState state, BlockPos pos, PoseStack poseStack, MultiBufferSource buffer, RandomSource random, long seed, int packedOverlay, int tintIndex, RenderType renderType) {
        BakedModel model  = Minecraft.getInstance().getModelManager().getModel(modelId);
        if (model instanceof CoasterWrappedModel coasterModel)
            coasterModel.setTintIndex(tintIndex);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(level, model, state, pos, poseStack, buffer.getBuffer(renderType), false, random, seed, packedOverlay);
    }

    @Override
    public void renderFluidInKeg(AbstractedFluidStack stack, GuiGraphics gui, int x, int y, float alphaModifier) {
        FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(stack.fluid());
        FluidState state = stack.fluid().defaultFluidState();
        TextureAtlasSprite[] sprites = handler.getFluidSprites(null, null, state);
        if (sprites != null) {
            TextureAtlasSprite sprite = sprites[0];
            int tintColor = handler.getFluidColor(null, null, state);

            float red = ((tintColor >> 16) & 0xFF) / 255f;
            float green = ((tintColor >> 8) & 0xFF) / 255f;
            float blue = (tintColor & 0xFF) / 255f;

            float capacity = Math.min(KegBlockEntity.localizedCapacity(), stack.unit().convertToLoader(stack.amount())) / (float) KegBlockEntity.localizedCapacity();
            if (capacity > 0.57) {
                int y1 = y + (int) (12 * (1 - ((capacity - 0.57F) / .43F)));
                int y2 = y + 12;
                float topCapacity = (capacity - 0.57F) / 0.43F;
                float vDistance = sprite.getV1() - sprite.getV0();
                float v0 = sprite.getV0() + (0.25F * vDistance) + (0.75F * vDistance * (1 - topCapacity));
                gui.innerBlit(sprite.atlasLocation(), x, x + 16, y1, y2, 0, sprite.getU0(), sprite.getU1(), v0, sprite.getV1(), red, green, blue, alphaModifier);
                gui.innerBlit(sprite.atlasLocation(), x + 16, x + 16 + 8, y1, y2, 0, sprite.getU0(), sprite.getU0() + 0.5F * (sprite.getU1() - sprite.getU0()), v0, sprite.getV1(), red, green, blue, alphaModifier);

            }
            int y1 = y + 12 + (int) (16 * (1 - Math.min(1, (capacity / .57F))));
            int y2 = y + 12 + 16;
            float vDistance = sprite.getV1() - sprite.getV0();
            float v0 = sprite.getV0() + (vDistance * (1 - Math.min(1, (capacity / .57F))));
            gui.innerBlit(sprite.atlasLocation(), x, x + 16, y1, y2, 0, sprite.getU0(), sprite.getU1(), v0, sprite.getV1(), red, green, blue, alphaModifier);
            gui.innerBlit(sprite.atlasLocation(), x + 16, x + 16 + 8, y1, y2, 0, sprite.getU0(), sprite.getU0() + 0.5F * (sprite.getU1() - sprite.getU0()), v0, sprite.getV1(), red, green, blue, alphaModifier);
        }
    }
}
