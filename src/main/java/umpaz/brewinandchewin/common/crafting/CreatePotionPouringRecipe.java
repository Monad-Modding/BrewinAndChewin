package umpaz.brewinandchewin.common.crafting;

import com.google.gson.JsonObject;
import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

import javax.annotation.Nullable;
import java.util.Optional;

public class CreatePotionPouringRecipe extends KegPouringRecipe {

    public CreatePotionPouringRecipe(ResourceLocation id, Optional<ItemStack> container, int amount) {
        super(id, AllFluids.POTION.get().getSource(), container, Items.POTION.getDefaultInstance(), amount, false, true);
    }

    @Override
    public ItemStack assemble(KegRecipeWrapper recipeWrapper, RegistryAccess registryAccess) {
        ItemStack stack = new ItemStack(Items.POTION);
        FluidStack fluidStack = recipeWrapper.getFluid(0);
        if (fluidStack.getTag() != null) {
            Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(fluidStack.getTag().getString("Potion")));
            if (potion != null)
                PotionUtils.setPotion(stack, potion);
        }
        return stack;
    }

    @Override
    public FluidStack getFluid(ItemStack container) {
        FluidStack fluidStack = super.getFluid(container);
        Potion potion = PotionUtils.getPotion(container);
        PotionFluid.addPotionToFluidStack(fluidStack, potion);
        return fluidStack;
    }

    @Override
    public boolean hasSpecialFluid() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BnCRecipeSerializers.CREATE_POTION_POURING.get();
    }

    public static class Serializer implements RecipeSerializer<CreatePotionPouringRecipe> {
        public Serializer() {
        }

        @Override
        public CreatePotionPouringRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            final int amountIn = GsonHelper.getAsInt(json, "amount", 250);
            final Optional<ItemStack> containerIn = GsonHelper.isValidNode(json, "container") ? Optional.of(CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "container"), true)) : Optional.empty();
            return new CreatePotionPouringRecipe(recipeId, containerIn, amountIn);
        }

        @Nullable
        @Override
        public CreatePotionPouringRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int amountIn = buffer.readVarInt();
            Optional<ItemStack> containerIn = buffer.readOptional(FriendlyByteBuf::readItem);
            return new CreatePotionPouringRecipe(recipeId, containerIn, amountIn);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CreatePotionPouringRecipe recipe) {
            buffer.writeVarInt(recipe.getAmount());
            buffer.writeOptional(recipe.getRawContainer(), FriendlyByteBuf::writeItem);
        }
    }
}
