package umpaz.brewinandchewin.common.mixin;

import com.mojang.datafixers.DSL;
import net.minecraft.util.datafix.DataFixTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DataFixTypes.class)
public interface DataFixTypesAccessor {
    @Accessor("type")
    DSL.TypeReference brewinandchewin$getType();
}