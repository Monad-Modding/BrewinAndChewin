package umpaz.brewinandchewin.client.renderer.texture.modifier;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface TextureModifier {
    default int color(BlockAndTintGetter level, BlockState state, BlockPos pos, ItemStack stack, int previous) {
        return previous;
    }

    default RenderType renderType(BlockAndTintGetter level, BlockState state, BlockPos pos, ItemStack stack, RenderType previous) {
        return previous;
    }

    Identifier getId();

    MapCodec<? extends TextureModifier> codec();
}