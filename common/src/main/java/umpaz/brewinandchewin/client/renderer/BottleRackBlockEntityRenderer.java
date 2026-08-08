package umpaz.brewinandchewin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.BrewinAndChewinClient;
import umpaz.brewinandchewin.common.block.BottleRackBlock;
import umpaz.brewinandchewin.common.block.entity.BottleRackBlockEntity;
import umpaz.brewinandchewin.common.item.WineItem;
import umpaz.brewinandchewin.common.utility.BnCLabelUtils;
import umpaz.brewinandchewin.common.utility.BnCWineUtils;

import java.util.EnumMap;
import java.util.Map;

public class BottleRackBlockEntityRenderer implements BlockEntityRenderer<BottleRackBlockEntity> {
    private static final float SHALLOW_DEPTH = 5.0F / 16.0F;
    private static final float LARGE_DEPTH = -3.0F / 16.0F;
    private static final float FIRST_COLUMN = 12.0F / 16.0F;
    private static final float FIRST_ROW = 12.0F / 16.0F;
    private static final float SLOT_SPACING = 5.0F / 16.0F;

    private static final Map<Direction, Float> ROTATIONS = new EnumMap<>(Map.of(
            Direction.NORTH, 0.0F,
            Direction.EAST, 90.0F,
            Direction.SOUTH, 180.0F,
            Direction.WEST, 270.0F));

    private final RandomSource random = RandomSource.create();

    public BottleRackBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static ResourceLocation bottleModel(WineItem wine, boolean finelyAged) {
        return BrewinAndChewin.asResource("block/bottle_rack_bottle_" + wine.getWineType().getSerializedName()
                + (finelyAged ? "_fine" : ""));
    }

    public static ResourceLocation labelModel(int slot) {
        return BrewinAndChewin.asResource("block/bottle_rack_bottle_label_" + slot);
    }

    @Override
    public void render(BottleRackBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof BottleRackBlock) || blockEntity.getLevel() == null)
            return;

        Direction facing = state.getValue(BottleRackBlock.FACING);
        float depth = state.getValue(BottleRackBlock.LARGE) ? LARGE_DEPTH : SHALLOW_DEPTH;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-ROTATIONS.get(facing)));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        for (int slot = 0; slot < BottleRackBlockEntity.SLOT_COUNT; ++slot) {
            ItemStack stack = blockEntity.getItem(slot);
            if (!(stack.getItem() instanceof WineItem wine))
                continue;

            float x = FIRST_COLUMN - (slot % BottleRackBlock.COLUMNS) * SLOT_SPACING;
            float y = FIRST_ROW - (slot / BottleRackBlock.COLUMNS) * SLOT_SPACING;
            boolean finelyAged = BnCWineUtils.getContents(stack).isFinelyAged(wine.getWineType());

            poseStack.pushPose();
            poseStack.translate(x, y, depth);
            this.tesselate(blockEntity, state, poseStack, buffer, packedOverlay, bottleModel(wine, finelyAged));
            if (BnCLabelUtils.getLabel(stack).isPresent()) {
                this.tesselate(blockEntity, state, poseStack, buffer, packedOverlay, labelModel(slot));
            }
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void tesselate(BottleRackBlockEntity blockEntity, BlockState state, PoseStack poseStack, MultiBufferSource buffer, int packedOverlay, ResourceLocation model) {
        BrewinAndChewinClient.getHelper().tesselateModel(blockEntity.getLevel(), model, state, blockEntity.getBlockPos(),
                poseStack, buffer, this.random, 0L, packedOverlay, -1, RenderType.cutout());
    }
}
