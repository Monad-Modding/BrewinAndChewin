package umpaz.brewinandchewin.client.renderer.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import umpaz.brewinandchewin.client.renderer.texture.modifier.GlintTextureModifier;
import umpaz.brewinandchewin.client.renderer.texture.modifier.GrassTintTextureModifier;
import umpaz.brewinandchewin.client.renderer.texture.modifier.PotionTintTextureModifier;
import umpaz.brewinandchewin.client.renderer.texture.modifier.TextureModifier;

import java.util.HashMap;
import java.util.Map;

public class BnCTextureModifiers {
    private static final Map<ResourceLocation, MapCodec<? extends TextureModifier>> FACTORIES = new HashMap<>();
    private static final Map<MapCodec<? extends TextureModifier>, ResourceLocation> FACTORIES_TO_KEY = new HashMap<>();

    public static final Codec<TextureModifier> CODEC =  ResourceLocation.CODEC.comapFlatMap(key -> {

        if (!FACTORIES.containsKey(key))
            return DataResult.error(() -> "Could not find texture modifier type " + key);
        return DataResult.success(FACTORIES.get(key));
    }, FACTORIES_TO_KEY::get).dispatch(
            textureModifier -> (MapCodec) textureModifier.codec(),
            mapCodec -> mapCodec.codec());

    public static void register(ResourceLocation id, MapCodec<? extends TextureModifier> codec) {
        if (FACTORIES.containsKey(id)) {
            throw new UnsupportedOperationException("Attempted to register duplicate ID texture modifiers. " + id);
        }
        FACTORIES.put(id, codec);
        FACTORIES_TO_KEY.put(codec, id);
    }

    public static void init() {
        register(GlintTextureModifier.ID, GlintTextureModifier.CODEC);
        register(GrassTintTextureModifier.ID, GrassTintTextureModifier.CODEC);
        register(PotionTintTextureModifier.ID, PotionTintTextureModifier.CODEC);
    }
}
