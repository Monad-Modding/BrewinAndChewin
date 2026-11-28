package umpaz.brewinandchewin.common.mixin;

import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ContextMap.class)
public interface LootParamsAccessor {
    @Accessor("params")
    Map<ContextKey<?>, Object> brewinandchewin$getParams();
}
