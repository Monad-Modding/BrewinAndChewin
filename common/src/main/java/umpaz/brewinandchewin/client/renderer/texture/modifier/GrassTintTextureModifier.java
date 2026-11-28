package umpaz.brewinandchewin.client.renderer.texture.modifier;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.brewinandchewin.BrewinAndChewin;

public class GrassTintTextureModifier implements TextureModifier {
    public static final Identifier ID = BrewinAndChewin.asResource("grass_tint");
    public static final MapCodec<GrassTintTextureModifier> CODEC = MapCodec.unit(GrassTintTextureModifier::new);

    @Override
    public int color(BlockAndTintGetter level, BlockState state, BlockPos pos, ItemStack stack, int previous) {
        return BiomeColors.getAverageGrassColor(level, pos);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public MapCodec<? extends TextureModifier> codec() {
        return CODEC;
    }
}
