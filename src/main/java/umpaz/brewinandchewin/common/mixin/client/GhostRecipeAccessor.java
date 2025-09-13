package umpaz.brewinandchewin.common.mixin.client;

import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GhostRecipe.class)
public interface GhostRecipeAccessor {
    @Accessor("time")
    float brewinandchewin$getTime();
}
