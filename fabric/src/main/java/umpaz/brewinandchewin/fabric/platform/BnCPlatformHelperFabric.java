package umpaz.brewinandchewin.fabric.platform;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.MenuProvider;
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
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.attachment.RagingAttachment;
import umpaz.brewinandchewin.common.attachment.TipsyHeartsAttachment;
import umpaz.brewinandchewin.common.block.entity.AgingCaskBlockEntity;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import umpaz.brewinandchewin.common.block.entity.container.AgingCaskMenu;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.block.entity.container.KegStackedContents;
import umpaz.brewinandchewin.common.block.entity.container.SidedKegWrapper;
import umpaz.brewinandchewin.common.utility.*;
import umpaz.brewinandchewin.fabric.BrewinAndChewinFabric;
import umpaz.brewinandchewin.fabric.block.entity.AgingCaskBlockEntityFabric;
import umpaz.brewinandchewin.fabric.block.entity.KegBlockEntityFabric;
import umpaz.brewinandchewin.fabric.container.KegFluidItemStorageFabric;
import umpaz.brewinandchewin.fabric.container.KegFluidTankFabric;
import umpaz.brewinandchewin.fabric.container.KegItemHandlerFabric;
import umpaz.brewinandchewin.fabric.container.SidedKegWrapperFabric;
import umpaz.brewinandchewin.fabric.ingredient.FixedAllIngredient;
import umpaz.brewinandchewin.fabric.registry.BnCAttachments;
import umpaz.brewinandchewin.fabric.registry.BnCCreativeTabsImpl;
import umpaz.brewinandchewin.fabric.registry.BnCFluidsImpl;
import umpaz.brewinandchewin.fabric.utility.*;
import umpaz.brewinandchewin.platform.BnCPlatform;
import umpaz.brewinandchewin.platform.BnCPlatformHelper;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.refabricated.inventory.ItemHandler;
import vectorwing.farmersdelight.refabricated.inventory.ItemHandlerSlot;

import java.util.List;
import java.util.function.BiConsumer;

public class BnCPlatformHelperFabric implements BnCPlatformHelper {

    @Override
    public BnCPlatform getPlatform() {
        return BnCPlatform.FABRIC;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public void sendClientbound(ServerPlayer player, CustomPacketPayload payload) {
        if (player.level().isClientSide)
            return;
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendClientboundTracking(Entity tracked, CustomPacketPayload payload) {
        if (tracked.level().isClientSide)
            return;
        for (ServerPlayer other : PlayerLookup.tracking(tracked))
            ServerPlayNetworking.send(other, payload);

        if (tracked instanceof ServerPlayer player)
            ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendServerbound(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public Component getFluidDisplayName(AbstractedFluidStack wrapper) {
        return FluidVariantAttributes.getName(((AmountedFluidVariant)wrapper.loaderSpecific()).variant());
    }

    @Override
    public void openKegMenu(Player player, KegBlockEntity blockEntity, BlockPos pos) {
        player.openMenu(blockEntity);
    }

    @Override
    public void openBlockPosMenu(Player player, MenuProvider menuProvider, BlockPos pos) {
        player.openMenu(menuProvider);
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<KegBlockEntity> supplyBlockEntity() {
        return KegBlockEntityFabric::new;
    }


    @Override
    public BlockEntityType.BlockEntitySupplier<AgingCaskBlockEntity> supplyAgingCaskBlockEntity() {
        return AgingCaskBlockEntityFabric::new;
    }

    @Override
    public MenuType<KegMenu> createMenuType(BnCMenuConstructor<KegMenu> constructor) {
        return new ExtendedScreenHandlerType<>(KegMenu::new, BlockPos.STREAM_CODEC);
    }


    @Override
    public MenuType<AgingCaskMenu> createAgingCaskMenuType(BnCMenuConstructor<AgingCaskMenu> constructor) {
        return new ExtendedScreenHandlerType<>(AgingCaskMenu::new, BlockPos.STREAM_CODEC);
    }

    @Override
    public AbstractedItemHandler createKegInventory(int size, BiConsumer<AbstractedItemHandler, Integer> onContentsChanged) {
        return new KegItemHandlerFabric(size) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                onContentsChanged.accept(this, slot);
            }
        };
    }

    @Override
    public AbstractedFluidTank createKegTank(long capacity, Runnable onContentsChanged) {
        return new KegFluidTankFabric(capacity) {
            @Override
            protected void onFinalCommit() {
                super.onFinalCommit();
                onContentsChanged.run();
            }
        };
    }

    @Override
    public Slot createKegSlot(AbstractedItemHandler inventory, int slot, int x, int y, boolean canInsert, @Nullable Pair<ResourceLocation, ResourceLocation> noItemIcon) {
        return new ItemHandlerSlot((ItemHandler) inventory, slot, x, y) {
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
        return new FixedAllIngredient(fluidOutputStacks.stream().map(p -> {
            if (p.strict())
                return DefaultCustomIngredients.components(p.stack());
            return Ingredient.of(p.stack().getItem());
        }).toList()).toVanilla();
    }

    @Override
    public KegRecipeWrapper createRecipeWrapper(AbstractedItemHandler itemHandler, AbstractedFluidTank fluidTank) {
        return new KegRecipeWrapperFabric((ItemHandler)itemHandler, fluidTank);
    }

    @Override
    public SidedKegWrapper createSidedKegWrapper(AbstractedItemHandler inventory, Direction direction) {
        return new SidedKegWrapperFabric(inventory, direction);
    }

    @Override
    public Codec<AbstractedFluidStack> getFluidStackWrapperCodec() {
        return BnCFabricCodecs.FLUID_VARIANT_WRAPPER;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AbstractedFluidStack> getFluidStackWrapperStreamCodec() {
        return BnCFabricStreamCodecs.FLUID_STACK_WRAPPER;
    }

    @Override
    public Codec<AbstractedFluidIngredient> getFluidIngredientWrapperCodec() {
        return KegFluidIngredient.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AbstractedFluidIngredient> getFluidIngredientWrapperStreamCodec() {
        return KegFluidIngredient.STREAM_CODEC;
    }

    @Override
    public AbstractedFluidStack deserializeTankFluidStack(CompoundTag tag, HolderLookup.Provider provider) {
        var fluidVariant = FluidVariant.CODEC.decode(RegistryOps.create(NbtOps.INSTANCE, provider), tag.get("variant")).mapOrElse(Pair::getFirst, pairError -> FluidVariant.blank());
        var amount = tag.getLong("amount");

        return new AbstractedFluidStack(fluidVariant.getFluid(), amount, fluidVariant.getComponentMap(), FluidUnit.DROPLET);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.getRecipeRemainder();
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
        return stack.has(DataComponents.FOOD);
    }

    @Override
    public FoodProperties getFoodProperties(ItemStack stack, LivingEntity entity) {
        return stack.get(DataComponents.FOOD);
    }

    @Override
    public MinecraftServer getServer() {
        return BrewinAndChewinFabric.getServer();
    }

    @Override
    public RagingAttachment getRagingAttachment(LivingEntity entity) {
        return entity.getAttachedOrElse(BnCAttachments.RAGING, null);
    }

    @Override
    public void setRagingAttachment(LivingEntity entity, @Nullable RagingAttachment value) {
        if (value == null) {
            entity.removeAttached(BnCAttachments.RAGING);
            return;
        }
        entity.setAttached(BnCAttachments.RAGING, value);
    }

    @Override
    public TipsyHeartsAttachment getTipsyHeartsAttachment(LivingEntity entity) {
        return entity.getAttachedOrElse(BnCAttachments.TIPSY_HEARTS, null);
    }

    @Override
    public void setTipsyHeartsAttachment(LivingEntity entity, @Nullable TipsyHeartsAttachment value) {
        if (value == null) {
            entity.removeAttached(BnCAttachments.TIPSY_HEARTS);
            return;
        }
        entity.setAttached(BnCAttachments.TIPSY_HEARTS, value);
    }

    @Override
    public Object createLoaderFluidStack(AbstractedFluidStack abstracted) {
        return new AmountedFluidVariant(FluidVariant.of(abstracted.fluid(), abstracted.components() instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY), abstracted.amount(), abstracted.unit());
    }

    @Override
    public Object copyLoaderFluidStack(Object fluidStack) {
        AmountedFluidVariant variant = (AmountedFluidVariant) fluidStack;
        return new AmountedFluidVariant(variant.variant(), variant.amount(), variant.fluidUnit());
    }

    @Override
    public AbstractedFluidTank getFluidContainerFromItem(ItemStack stack) {
        if (FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack)) == null)
            return null;
        return new KegFluidItemStorageFabric(stack);
    }

    @Override
    public String getAttachmentKey() {
        return "fabric:attachments";
    }

    @Override
    public Fluid getMilkFluid() {
        return BnCFluidsImpl.MILK;
    }

    @Override
    public Fluid getFlowingMilkFluid() {
        return BnCFluidsImpl.FLOWING_MILK;
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
        return Configuration.ENABLE_FOOD_EFFECT_TOOLTIP.get();
    }
}
