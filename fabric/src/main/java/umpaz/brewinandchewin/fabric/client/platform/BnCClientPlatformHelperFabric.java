package umpaz.brewinandchewin.fabric.client.platform;

import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.platform.client.BnCClientPlatformHelper;

public class BnCClientPlatformHelperFabric implements BnCClientPlatformHelper {
    @Override
    public void tesselateModel(BlockAndTintGetter level, Identifier modelId, BlockState state, BlockPos pos, PoseStack poseStack, SubmitNodeCollector collector, RandomSource random, long seed, int packedOverlay, int tintIndex, RenderType renderType) {
        // Model rendering migrated to BlockEntityRenderState + SubmitNodeCollector in 26.3
    }

    @Override
    public void renderFluidInKeg(AbstractedFluidStack stack, GuiGraphicsExtractor gui, int x, int y, float alphaModifier) {
        FluidState state = stack.fluid().defaultFluidState();
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state);
        if (model == null || model.stillMaterial() == null)
            return;
        TextureAtlasSprite sprite = model.stillMaterial().sprite();
        if (sprite != null) {
            int tintColor = model.tintSource() != null ? model.tintSource().color(state.createLegacyBlock()) : -1;

            float red = ((tintColor >> 16) & 0xFF) / 255f;
            float green = ((tintColor >> 8) & 0xFF) / 255f;
            float blue = (tintColor & 0xFF) / 255f;
            int color = ARGB.colorFromFloat(alphaModifier, red, green, blue);

            float capacity = Math.min(KegBlockEntity.localizedCapacity(), stack.unit().convertToLoader(stack.amount())) / (float) KegBlockEntity.localizedCapacity();
            int fillHeight = Math.max(1, Math.round(28.0F * capacity));
            gui.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y + (28 - fillHeight), 24, fillHeight, color);
        }
    }
}
