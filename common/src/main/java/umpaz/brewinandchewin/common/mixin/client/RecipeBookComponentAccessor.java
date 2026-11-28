package umpaz.brewinandchewin.common.mixin.client;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeBookComponent.class)
public interface RecipeBookComponentAccessor {
    @Accessor("lastRecipe")
    @Nullable
    RecipeDisplayId brewinandchewin$getLastRecipe();

    @Accessor("lastRecipeCollection")
    @Nullable
    RecipeCollection brewinandchewin$getLastRecipeCollection();
}
