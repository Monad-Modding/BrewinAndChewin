package umpaz.brewinandchewin.neoforge.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.neoforge.client.model.CoasterWrappedModel;
import umpaz.brewinandchewin.platform.client.BnCClientPlatformHelper;

public class BnCClientPlatfomHelperNeoForge implements BnCClientPlatformHelper {
    @Override
    public BakedModel getModel(ResourceLocation modelId) {
        return Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(modelId));
    }

    @Override
    public void registerItemProperty(Item item, ResourceLocation id, ClampedItemPropertyFunction function) {
        ItemProperties.register(item, id, function);
    }

    @Override
    public void tesselateModel(BlockAndTintGetter level, ResourceLocation modelId, BlockState state, BlockPos pos, PoseStack poseStack, MultiBufferSource buffer, RandomSource random, long seed, int packedOverlay, int tintIndex, RenderType renderType) {
        ModelData data = ModelData.EMPTY;
        if (tintIndex != -1) {
            data = ModelData.builder()
                    .with(CoasterWrappedModel.TINT_INDEX, tintIndex)
                    .build();
        }
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(level, Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(modelId)), state, pos, poseStack, buffer.getBuffer(renderType), false, random, seed, packedOverlay, data, renderType);
    }

    @Override
    public void renderFluidInKeg(AbstractedFluidStack stack, GuiGraphics gui, int x, int y, float alphaModifier) {
        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(stack.fluid());
        FluidStack fluidStack = (FluidStack) stack.loaderSpecific();
        if (fluidStack == null)
            return;
        ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
        int tintColor = fluidTypeExtensions.getTintColor(fluidStack);

        float alpha = ((tintColor >> 24) & 0xFF) / 255f * alphaModifier;
        float red = ((tintColor >> 16) & 0xFF) / 255f;
        float green = ((tintColor >> 8) & 0xFF) / 255f;
        float blue = (tintColor & 0xFF) / 255f;

        float capacity = Math.min(BnCConfiguration.common().keg().localizedCapacity(), stack.unit().convertToLoader(stack.amount())) / (float) BnCConfiguration.common().keg().localizedCapacity();
        if (capacity > 0.57) {
            int y1 = y + (int) (12 * (1 - ((capacity - 0.57F) / .43F)));
            int y2 = y + 12;
            float topCapacity = (capacity - 0.57F) / 0.43F;
            float vDistance = sprite.getV1() - sprite.getV0();
            float v0 = sprite.getV0() + (0.25F * vDistance) + (0.75F * vDistance * (1 - topCapacity));
            gui.innerBlit(sprite.atlasLocation(), x, x + 16, y1, y2, 0, sprite.getU0(), sprite.getU1(), v0, sprite.getV1(), red, green, blue, alpha);
            gui.innerBlit(sprite.atlasLocation(), x + 16, x + 16 + 8, y1, y2, 0, sprite.getU0(), sprite.getU0() + 0.5F * (sprite.getU1() - sprite.getU0()), v0, sprite.getV1(), red, green, blue, alpha);

        }
        int y1 = y + 12 + (int) (16 * (1 - Math.min(1, (capacity / .57F))));
        int y2 = y + 12 + 16;
        float vDistance = sprite.getV1() - sprite.getV0();
        float v0 = sprite.getV0() + (vDistance * (1 - Math.min(1, (capacity / .57F))));
        gui.innerBlit(sprite.atlasLocation(), x, x + 16, y1, y2, 0, sprite.getU0(), sprite.getU1(), v0, sprite.getV1(), red, green, blue, alpha);
        gui.innerBlit(sprite.atlasLocation(), x + 16, x + 16 + 8, y1, y2, 0, sprite.getU0(), sprite.getU0() + 0.5F * (sprite.getU1() - sprite.getU0()), v0, sprite.getV1(), red, green, blue, alpha);
    }
}
