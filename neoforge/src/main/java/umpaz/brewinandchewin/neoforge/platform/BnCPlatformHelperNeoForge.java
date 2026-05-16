package umpaz.brewinandchewin.neoforge.platform;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.attachment.RagingAttachment;
import umpaz.brewinandchewin.common.attachment.TipsyHeartsAttachment;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.block.entity.container.KegStackedContents;
import umpaz.brewinandchewin.common.block.entity.container.SidedKegWrapper;
import umpaz.brewinandchewin.common.utility.*;
import umpaz.brewinandchewin.neoforge.container.KegFluidItemStorageNeoForge;
import umpaz.brewinandchewin.neoforge.container.KegFluidTankNeoForge;
import umpaz.brewinandchewin.neoforge.container.KegItemHandlerNeoForge;
import umpaz.brewinandchewin.neoforge.container.SidedKegWrapperNeoForge;
import umpaz.brewinandchewin.neoforge.registry.BnCAttachments;
import umpaz.brewinandchewin.neoforge.registry.BnCCreativeTabsImpl;
import umpaz.brewinandchewin.neoforge.registry.BnCFluidsImpl;
import umpaz.brewinandchewin.neoforge.utility.*;
import umpaz.brewinandchewin.platform.BnCPlatformHelper;
import umpaz.brewinandchewin.platform.BnCPlatform;
import vectorwing.farmersdelight.common.Configuration;

import java.util.List;
import java.util.function.BiConsumer;

public class BnCPlatformHelperNeoForge implements BnCPlatformHelper {

    @Override
    public BnCPlatform getPlatform() {
        return BnCPlatform.NEOFORGE;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public void sendClientbound(ServerPlayer player, CustomPacketPayload payload) {
        if (player.level().isClientSide)
            return;
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendClientboundTracking(Entity tracked, CustomPacketPayload payload) {
        if (tracked.level().isClientSide)
            return;
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(tracked, payload);
    }

    @Override
    public void sendServerbound(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    @Override
    public Component getFluidDisplayName(AbstractedFluidStack wrapper) {
        return wrapper.loaderSpecific() instanceof FluidStack fluidStack ? fluidStack.getHoverName() : Component.empty();
    }

    @Override
    public MenuType<KegMenu> createMenuType(BnCMenuConstructor<KegMenu> constructor) {
        return IMenuTypeExtension.create((id, inv, data) -> new KegMenu(id, inv, data.readBlockPos()));
    }

    @Override
    public AbstractedItemHandler createKegInventory(int size, BiConsumer<AbstractedItemHandler, Integer> onContentsChanged) {
        return new KegItemHandlerNeoForge(size) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                onContentsChanged.accept(this, slot);
            }
        };
    }

    @Override
    public AbstractedFluidTank createKegTank(long capacity, Runnable onContentsChanged) {
        return new KegFluidTankNeoForge((int) capacity) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                onContentsChanged.run();
            }
        };
    }

    @Override
    public Slot createKegSlot(AbstractedItemHandler inventory, int slot, int x, int y, boolean canInsert, @Nullable Pair<ResourceLocation, ResourceLocation> noItemIcon) {
        return new SlotItemHandler((IItemHandler)inventory, slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return canInsert && super.mayPlace(stack);
            }

            @Override
            public @Nullable Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return noItemIcon;
            }
        };
    }

    @Override
    public Ingredient createStrictFillPickerIngredient(List<KegStackedContents.PouringEntry> fluidOutputStacks) {
        return CompoundIngredient.of(fluidOutputStacks.stream().map(p -> {
            if (p.strict())
                return DataComponentIngredient.of(true, p.stack());
            return Ingredient.of(p.stack().getItem());
        }).toArray(Ingredient[]::new));
    }

    @Override
    public KegRecipeWrapper createRecipeWrapper(AbstractedItemHandler itemHandler, AbstractedFluidTank fluidTank) {
        return new KegRecipeWrapperNeoForge((IItemHandlerModifiable) itemHandler, fluidTank);
    }

    @Override
    public SidedKegWrapper createSidedKegWrapper(AbstractedItemHandler inventory, Direction direction) {
        return new SidedKegWrapperNeoForge(inventory, direction);
    }

    @Override
    public void openKegMenu(Player player, KegBlockEntity blockEntity, BlockPos pos) {
        player.openMenu(blockEntity, pos);
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<KegBlockEntity> supplyBlockEntity() {
        return KegBlockEntity::new;
    }

    @Override
    public Codec<AbstractedFluidStack> getFluidStackWrapperCodec() {
        return BnCNeoForgeCodecs.FLUID_STACK_WRAPPER;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AbstractedFluidStack> getFluidStackWrapperStreamCodec() {
        return BnCNeoForgeStreamCodecs.FLUID_STACK_WRAPPER;
    }

    @Override
    public Codec<AbstractedFluidIngredient> getFluidIngredientWrapperCodec() {
        return KegCompatibleFluidIngredients.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AbstractedFluidIngredient> getFluidIngredientWrapperStreamCodec() {
        return KegCompatibleFluidIngredients.STREAM_CODEC;
    }

    @Override
    public AbstractedFluidStack deserializeTankFluidStack(CompoundTag tag, HolderLookup.Provider provider) {
        if (!tag.contains("Fluid"))
            return AbstractedFluidStack.EMPTY;

        var fluidStack = FluidStack.CODEC.decode(RegistryOps.create(NbtOps.INSTANCE, provider), tag.get("Fluid")).mapOrElse(Pair::getFirst, pairError -> FluidStack.EMPTY);

        return new AbstractedFluidStack(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getComponents(), FluidUnit.MILLIBUCKET);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.getCraftingRemainingItem();
    }

    @Override
    public void initFluids() {
        BnCFluidsImpl.init();
    }

    @Override
    public void initCreativeTab() {
        BnCCreativeTabsImpl.init();
    }

    @Override
    public boolean isEdible(ItemStack stack, LivingEntity entity) {
        return stack.getFoodProperties(entity) != null;
    }

    @Override
    public FoodProperties getFoodProperties(ItemStack stack, LivingEntity entity) {
        return stack.getFoodProperties(entity);
    }

    @Override
    public MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public RagingAttachment getRagingAttachment(LivingEntity entity) {
        return entity.getExistingData(BnCAttachments.RAGING).orElse(null);
    }

    @Override
    public void setRagingAttachment(LivingEntity entity, @Nullable RagingAttachment value) {
        if (value == null) {
            entity.removeData(BnCAttachments.RAGING);
            return;
        }
        entity.setData(BnCAttachments.RAGING, value);
    }

    @Override
    public TipsyHeartsAttachment getTipsyHeartsAttachment(LivingEntity entity) {
        return entity.getExistingData(BnCAttachments.TIPSY_HEARTS).orElse(null);
    }

    @Override
    public void setTipsyHeartsAttachment(LivingEntity entity, @Nullable TipsyHeartsAttachment value) {
        if (value == null) {
            entity.removeData(BnCAttachments.TIPSY_HEARTS);
            return;
        }
        entity.setData(BnCAttachments.TIPSY_HEARTS, value);
    }

    @Override
    public Object createLoaderFluidStack(AbstractedFluidStack abstracted) {
        return new FluidStack(abstracted.fluid().builtInRegistryHolder(), (int) abstracted.unit().convertToLoader(abstracted.amount()), abstracted.components() instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY);
    }

    @Override
    public Object copyLoaderFluidStack(Object fluidStack) {
        return ((FluidStack)fluidStack).copy();
    }

    @Override
    public AbstractedFluidTank getFluidContainerFromItem(ItemStack stack) {
        if (Capabilities.FluidHandler.ITEM.getCapability(stack, null) == null)
            return null;
        return new KegFluidItemStorageNeoForge(stack);
    }

    @Override
    public String getAttachmentKey() {
        return "neoforge:attachments";
    }

    @Override
    public Fluid getMilkFluid() {
        return NeoForgeMod.MILK.get();
    }

    @Override
    public Fluid getFlowingMilkFluid() {
        return NeoForgeMod.FLOWING_MILK.get();
    }

    @Override
    public Fluid getCreatePotionFluid() {
        if (isModLoaded("create")) {
            return BnCCreateDelegate.getPotionSource();
        }
        return null;
    }

    @Override
    public boolean hasFoodEffectTooltip() {
        return Configuration.FOOD_EFFECT_TOOLTIP.get();
    }
}