package umpaz.brewinandchewin.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.ComposterBlock;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.attachment.RagingAttachment;
import umpaz.brewinandchewin.common.attachment.TipsyHeartsAttachment;
import umpaz.brewinandchewin.common.network.clientbound.*;
import umpaz.brewinandchewin.common.network.serverbound.EMIFillFermentingRecipeServerboundPacket;
import umpaz.brewinandchewin.common.network.serverbound.EMIFillPouringRecipeServerboundPacket;
import umpaz.brewinandchewin.common.network.serverbound.JEITransferKegRecipeServerboundPacket;
import umpaz.brewinandchewin.common.network.serverbound.SetLabelContentsServerboundPacket;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCCreativeTabs;
import umpaz.brewinandchewin.common.registry.BnCDataComponents;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import umpaz.brewinandchewin.common.registry.BnCFluids;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCLootConditions;
import umpaz.brewinandchewin.common.registry.BnCLootFunctions;
import umpaz.brewinandchewin.common.registry.BnCMenuTypes;
import umpaz.brewinandchewin.common.registry.BnCParticleTypes;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.fabric.container.KegFluidTankFabric;
import umpaz.brewinandchewin.fabric.container.SidedKegWrapperFabric;
import umpaz.brewinandchewin.fabric.fluid.BnCFluidVariantAttributeHandler;
import umpaz.brewinandchewin.fabric.platform.BnCPlatformHelperFabric;
import umpaz.brewinandchewin.fabric.registry.BnCAttachments;
import umpaz.brewinandchewin.fabric.registry.BnCFluidsImpl;
import umpaz.brewinandchewin.fabric.registry.BnCLootModificationEvents;

import java.util.Optional;

public class BrewinAndChewinFabric implements ModInitializer {
    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        BrewinAndChewin.init(new BnCPlatformHelperFabric());
        registerContents();
        registerNetwork();
        registerCompostables();
        registerFluidAttributeHandlers();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            BrewinAndChewinFabric.server = server;
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            BrewinAndChewinFabric.server = null;
        });

        EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
            if (entity instanceof LivingEntity living) {
                if (entity.hasAttached(BnCAttachments.TIPSY_HEARTS)) {
                    TipsyHeartsAttachment attachment = entity.getAttached(BnCAttachments.TIPSY_HEARTS);
                    BrewinAndChewin.getHelper().sendClientbound(player, new SyncNumbedHeartsClientboundPacket(living.getId(), attachment.getNumbedHealth(), attachment.getTicksUntilDamage()));
                }
                if (entity.hasAttached(BnCAttachments.RAGING)) {
                    RagingAttachment attachment = entity.getAttached(BnCAttachments.RAGING);
                    BrewinAndChewin.getHelper().sendClientbound(player, new SyncRagingStacksClientboundPacket(living.getId(), Optional.of(attachment.getStacks())));
                }
            }
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof ServerPlayer) {
                if (entity.hasAttached(BnCAttachments.TIPSY_HEARTS)) {
                    TipsyHeartsAttachment attachment = entity.getAttached(BnCAttachments.TIPSY_HEARTS);
                    BrewinAndChewin.getHelper().sendClientboundTracking(entity, new SyncNumbedHeartsClientboundPacket(entity.getId(), attachment.getNumbedHealth(), attachment.getTicksUntilDamage()));
                }
                if (entity.hasAttached(BnCAttachments.RAGING)) {
                    RagingAttachment attachment = entity.getAttached(BnCAttachments.RAGING);
                    BrewinAndChewin.getHelper().sendClientboundTracking(entity, new SyncRagingStacksClientboundPacket(entity.getId(), Optional.of(attachment.getStacks())));
                }
            }
        });
    }

    public static MinecraftServer getServer() {
        return server;
    }

    private static void registerContents() {
        BnCAttachments.registerAll();
        BnCBlocks.registerAll();
        BnCBlockEntityTypes.registerAll();
        BnCCreativeTabs.registerAll();
        BnCDataComponents.registerAll();
        BnCEffects.registerAll();
        BnCFluids.registerAll();
        BnCItems.registerAll();
        BnCLootConditions.registerAll();
        BnCLootFunctions.registerAll();
        BnCLootModificationEvents.init();
        BnCMenuTypes.registerAll();
        BnCParticleTypes.registerAll();
        BnCRecipeTypes.registerAll();
        BnCRecipeSerializers.registerAll();

        ItemStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> (SidedKegWrapperFabric)blockEntity.getSidedHandler(direction), BnCBlockEntityTypes.KEG);
        FluidStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> (KegFluidTankFabric)blockEntity.getFluidTank(), BnCBlockEntityTypes.KEG);
    }

    private static void registerNetwork() {
        PayloadTypeRegistry.playS2C().register(ClearKegFluidContainerComponentsClientboundPacket.TYPE, ClearKegFluidContainerComponentsClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(MakeNextPlayerChatTipsyClientboundPacket.TYPE, MakeNextPlayerChatTipsyClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SendRecipeBookValuesClientboundPacket.TYPE, SendRecipeBookValuesClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncNumbedHeartsClientboundPacket.TYPE, SyncNumbedHeartsClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncRagingStacksClientboundPacket.TYPE, SyncRagingStacksClientboundPacket.STREAM_CODEC);

        PayloadTypeRegistry.playC2S().register(JEITransferKegRecipeServerboundPacket.TYPE, JEITransferKegRecipeServerboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(EMIFillFermentingRecipeServerboundPacket.TYPE, EMIFillFermentingRecipeServerboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(EMIFillPouringRecipeServerboundPacket.TYPE, EMIFillPouringRecipeServerboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(SetLabelContentsServerboundPacket.TYPE, SetLabelContentsServerboundPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(JEITransferKegRecipeServerboundPacket.TYPE, (payload, context) -> payload.handle(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(EMIFillFermentingRecipeServerboundPacket.TYPE, (payload, context) -> payload.handle(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(EMIFillPouringRecipeServerboundPacket.TYPE, (payload, context) -> payload.handle(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(SetLabelContentsServerboundPacket.TYPE, (payload, context) -> payload.handle(context.player()));
    }

    private static void registerCompostables() {
        ComposterBlock.COMPOSTABLES.put(BnCItems.KIMCHI, 0.5F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.PICKLED_PICKLES, 0.5F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.QUICHE_SLICE, 0.85F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.QUICHE, 1.0F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.RED_GRAPES, 0.65F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.WHITE_GRAPES, 0.65F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.RED_GRAPE_SEEDS, 0.3F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.WHITE_GRAPE_SEEDS, 0.3F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.CHOCOLATE_CAKE, 1.0F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.PUMPKIN_ROLL, 1.0F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.GLOW_BERRY_MERINGUE_PIE, 1.0F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.SLICE_OF_CHOCOLATE_CAKE, 0.85F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.SLICE_OF_GLOW_BERRY_MERINGUE_PIE, 0.85F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.SLICE_OF_PUMPKIN_ROLL, 0.85F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.CORN_BREAD, 0.85F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.CORN_MUFFIN, 0.85F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.TORTILLA, 0.85F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.CORN, 0.65F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.CORNMEAL, 0.65F);
        ComposterBlock.COMPOSTABLES.put(BnCItems.CORN_KERNELS, 0.3F);
    }

    private static void registerFluidAttributeHandlers() {
        FluidVariantAttributes.register(BnCFluidsImpl.MILK, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluidsImpl.FLOWING_MILK, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.HONEY, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_HONEY, BnCFluidVariantAttributeHandler.INSTANCE);

        FluidVariantAttributes.register(BnCFluids.BEER, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_BEER, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.VODKA, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_VODKA, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.MEAD, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_MEAD, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.EGG_GROG, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_EGG_GROG, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.STRONGROOT_ALE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_STRONGROOT_ALE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.RICE_WINE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_RICE_WINE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.GLITTERING_GRENADINE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_GLITTERING_GRENADINE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.STEEL_TOE_STOUT, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_STEEL_TOE_STOUT, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.DREAD_NOG, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_DREAD_NOG, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.SACCHARINE_RUM, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_SACCHARINE_RUM, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.PALE_JANE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_PALE_JANE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.SALTY_FOLLY, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_SALTY_FOLLY, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.BLOODY_MARY, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_BLOODY_MARY, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.RED_RUM, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_RED_RUM, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.WITHERING_DROSS, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_WITHERING_DROSS, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.KOMBUCHA, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_KOMBUCHA, BnCFluidVariantAttributeHandler.INSTANCE);

        FluidVariantAttributes.register(BnCFluids.RED_WINE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_RED_WINE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.WHITE_WINE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_WHITE_WINE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.CURRANT_WINE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_CURRANT_WINE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.VERRUCA_WINE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_VERRUCA_WINE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.TWISTED_WINE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_TWISTED_WINE, BnCFluidVariantAttributeHandler.INSTANCE);

        FluidVariantAttributes.register(BnCFluids.FLAXEN_CHEESE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_FLAXEN_CHEESE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.SCARLET_CHEESE, BnCFluidVariantAttributeHandler.INSTANCE);
        FluidVariantAttributes.register(BnCFluids.FLOWING_SCARLET_CHEESE, BnCFluidVariantAttributeHandler.INSTANCE);
    }
}
