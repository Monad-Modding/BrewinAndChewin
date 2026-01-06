package umpaz.brewinandchewin.common.mixin;

import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.BitSet;
import java.util.List;

@Mixin(targets = "net.minecraft.world.entity.player.StackedContents$RecipePicker")
public interface StackedContentsRecipePickerAccessor {
    @Accessor("ingredients")
    List<Ingredient> brewinandchewin$getIngredients();

    @Accessor("ingredientCount")
    int brewinandchewin$getIngredientCount();

    @Accessor("ingredientCount") @Mutable @Final
    void brewinandchewin$setIngredientCount(int value);

    @Accessor("items")
    int[] brewinandchewin$getItems();

    @Accessor("items") @Mutable @Final
    void brewinandchewin$setItems(int[] value);

    @Accessor("itemCount")
    int brewinandchewin$getItemCount();

    @Accessor("itemCount") @Mutable @Final
    void brewinandchewin$setItemCount(int value);

    @Accessor("data")
    BitSet brewinandchewin$getData();

    @Accessor("data") @Mutable @Final
    void brewinandchewin$setData(BitSet value);

    @Invoker("getUniqueAvailableIngredientItems")
    int[] brewinandchewin$invokeGetUniqueAvailableIngredientItems();

    @Invoker("getIndex")
    int brewinandchewin$invokeGetIndex(boolean isIngredientPath, int stackingIndex, int pathIndex);
}
