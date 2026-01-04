package umpaz.brewinandchewin.platform;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.attachment.RagingAttachment;
import umpaz.brewinandchewin.common.attachment.TipsyHeartsAttachment;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.block.entity.container.KegStackedContents;
import umpaz.brewinandchewin.common.block.entity.container.SidedKegWrapper;
import umpaz.brewinandchewin.common.utility.BnCMenuConstructor;
import umpaz.brewinandchewin.common.utility.AbstractedFluidIngredient;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.KegContainer;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

import java.util.List;
import java.util.function.BiConsumer;

public interface BnCPlatformHelper {

    BnCPlatform getPlatform();

    boolean isModLoaded(String modId);

    default boolean isModLoadedEarly(String modId) {
        return isModLoaded(modId);
    }

    boolean isDevelopmentEnvironment();

    void sendClientbound(ServerPlayer player, BnCPacket payload);
    void sendClientboundTracking(Entity tracked, BnCPacket payload);
    void sendServerbound(BnCPacket payload);

    Component getFluidDisplayName(AbstractedFluidStack wrapper);

    void openKegMenu(Player player, KegBlockEntity blockEntity, BlockPos pos);

    BlockEntityType<KegBlockEntity> supplyBlockEntity();
    MenuType<KegMenu> createMenuType(BnCMenuConstructor<KegMenu> constructor);

    AbstractedItemHandler createKegInventory(int size, BiConsumer<AbstractedItemHandler, Integer> onContentsChanged);

    AbstractedFluidTank createKegTank(long capacity, Runnable onContentsChanged);

    default Slot createKegSlot(AbstractedItemHandler inventory, int slot, int x, int y) {
        return createKegSlot(inventory, slot, x, y, true, null);
    }
    default Slot createKegContainerSlot(AbstractedItemHandler inventory, int slot, int x, int y) {
        return createKegSlot(inventory, slot, x, y, true, Pair.of(InventoryMenu.BLOCK_ATLAS, KegMenu.EMPTY_CONTAINER_SLOT_TANKARD));
    }
    default Slot createKegResultSlot(AbstractedItemHandler inventory, int slot, int x, int y) {
        return createKegSlot(inventory, slot, x, y, false, null);
    }
    Slot createKegSlot(AbstractedItemHandler inventory, int slot, int x, int y, boolean canInsert, @Nullable Pair<ResourceLocation, ResourceLocation> noItemIcon);

    Ingredient createStrictFillPickerIngredient(List<KegStackedContents.PouringEntry> fluidOutputStacks);

    KegContainer createRecipeWrapper(AbstractedItemHandler itemHandler, AbstractedFluidTank fluidTank);

    SidedKegWrapper createSidedKegWrapper(AbstractedItemHandler inventory, Direction direction);

    Codec<AbstractedFluidStack> getFluidStackWrapperCodec();

    void writeFluidStack(FriendlyByteBuf buf, AbstractedFluidStack stack);
    AbstractedFluidStack readFluidStack(FriendlyByteBuf buf);

    Codec<AbstractedFluidIngredient> getFluidIngredientWrapperCodec();

    void writeFluidIngredient(FriendlyByteBuf buf, AbstractedFluidIngredient ingredient);
    AbstractedFluidIngredient readFluidIngredient(FriendlyByteBuf buf);

    AbstractedFluidStack deserializeTankFluidStack(CompoundTag tag, HolderLookup.Provider provider);

    ItemStack getCraftingRemainingItem(ItemStack stack);

    void initFluids();
    void initCreativeTab();

    boolean isEdible(ItemStack stack, LivingEntity entity);

    FoodProperties getFoodProperties(ItemStack stack, LivingEntity entity);

    MinecraftServer getServer();

    RagingAttachment getRagingAttachment(LivingEntity entity);
    void setRagingAttachment(LivingEntity entity, @Nullable RagingAttachment value);

    TipsyHeartsAttachment getTipsyHeartsAttachment(LivingEntity entity);
    void setTipsyHeartsAttachment(LivingEntity entity, @Nullable TipsyHeartsAttachment value);

    Object createLoaderFluidStack(AbstractedFluidStack abstracted);
    Object copyLoaderFluidStack(Object fluidStack);

    @Nullable
    AbstractedFluidTank getFluidContainerFromItem(ItemStack stack);

    String getAttachmentKey();

    Fluid getMilkFluid();
    Fluid getFlowingMilkFluid();

    Fluid getCreatePotionFluid();

    boolean hasFoodEffectTooltip();
}