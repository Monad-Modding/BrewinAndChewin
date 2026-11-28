package umpaz.brewinandchewin.platform.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;

public interface BnCClientPlatformHelper {
    void tesselateModel(BlockAndTintGetter level,
                               Identifier modelId,
                               BlockState state,
                               BlockPos pos,
                               PoseStack poseStack,
                               SubmitNodeCollector collector,
                               RandomSource random,
                               long seed,
                               int packedOverlay,
                               int tintIndex,
                               RenderType renderType);

    void renderFluidInKeg(AbstractedFluidStack stack, GuiGraphicsExtractor graphics, int x, int y, float alphaModifier);
}
