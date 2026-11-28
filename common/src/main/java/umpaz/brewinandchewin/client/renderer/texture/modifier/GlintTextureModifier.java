
package umpaz.brewinandchewin.client.renderer.texture.modifier;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.brewinandchewin.BrewinAndChewin;

public class GlintTextureModifier implements TextureModifier {
    public static final Identifier ID = BrewinAndChewin.asResource("glint");
    public static final MapCodec<GlintTextureModifier> CODEC = MapCodec.unit(GlintTextureModifier::new);

    public RenderType renderType(BlockAndTintGetter level, BlockState state, BlockPos pos, ItemStack stack, RenderType previous) {
        return RenderTypes.itemTranslucentGlint(TextureAtlas.LOCATION_BLOCKS);
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
