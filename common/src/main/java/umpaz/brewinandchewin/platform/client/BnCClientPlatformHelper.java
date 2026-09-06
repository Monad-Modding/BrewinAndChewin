package umpaz.brewinandchewin.platform.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;

public interface BnCClientPlatformHelper {
    BakedModel getModel(ResourceLocation modelId);

    void registerItemProperty(Item item, ResourceLocation id, ClampedItemPropertyFunction function);

    void tesselateModel(BlockAndTintGetter level,
                               ResourceLocation modelId,
                               BlockState state,
                               BlockPos pos,
                               PoseStack poseStack,
                               MultiBufferSource buffer,
                               RandomSource random,
                               long seed,
                               int packedOverlay,
                               int tintIndex,
                               RenderType renderType);

    void renderFluidInKeg(AbstractedFluidStack stack, GuiGraphics graphics, int x, int y, float alphaModifier);
}
