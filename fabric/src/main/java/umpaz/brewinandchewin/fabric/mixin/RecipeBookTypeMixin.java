package umpaz.brewinandchewin.fabric.mixin;

import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RecipeBookType.class)
public enum RecipeBookTypeMixin {
    BREWINANDCHEWIN_FERMENTING
}
