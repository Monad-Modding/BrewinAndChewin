package umpaz.brewinandchewin.neoforge;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.network.clientbound.*;
import umpaz.brewinandchewin.common.network.serverbound.EMIFillFermentingRecipeServerboundPacket;
import umpaz.brewinandchewin.common.network.serverbound.EMIFillPouringRecipeServerboundPacket;
import umpaz.brewinandchewin.common.network.serverbound.JEITransferKegRecipeServerboundPacket;
import umpaz.brewinandchewin.common.network.serverbound.SetLabelContentsServerboundPacket;
import umpaz.brewinandchewin.common.registry.*;
import umpaz.brewinandchewin.common.registry.BnCCreativeTabs;
import umpaz.brewinandchewin.neoforge.container.KegFluidTankNeoForge;
import umpaz.brewinandchewin.neoforge.container.SidedKegWrapperNeoForge;
import umpaz.brewinandchewin.neoforge.registry.BnCAttachments;
import umpaz.brewinandchewin.neoforge.registry.BnCFluidTypes;
import umpaz.brewinandchewin.neoforge.registry.BnCLootModifiers;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import umpaz.brewinandchewin.common.loot.BnCInnardsDrops;
import umpaz.brewinandchewin.neoforge.platform.BnCPlatformHelperNeoForge;

@Mod(BrewinAndChewin.MODID)
public class BrewinAndChewinNeoForge {

    public BrewinAndChewinNeoForge(IEventBus eventBus) {
        BrewinAndChewin.init(new BnCPlatformHelperNeoForge());
        NeoForgeMod.enableMilkFluid();
    }

    @EventBusSubscriber(modid = BrewinAndChewin.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BnCBlockEntityTypes.KEG, (blockEntity, direction) -> (SidedKegWrapperNeoForge)blockEntity.getSidedHandler(direction));
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, BnCBlockEntityTypes.KEG, (blockEntity, direction) -> (KegFluidTankNeoForge)blockEntity.getFluidTank());
        }

        @SubscribeEvent
        public static void registerContent(RegisterEvent event) {
            register(event, NeoForgeRegistries.Keys.ATTACHMENT_TYPES, BnCAttachments::registerAll);
            register(event, Registries.BLOCK, BnCBlocks::registerAll);
            register(event, Registries.BLOCK_ENTITY_TYPE, BnCBlockEntityTypes::registerAll);
            register(event, Registries.CREATIVE_MODE_TAB, BnCCreativeTabs::registerAll);
            register(event, Registries.DATA_COMPONENT_TYPE, BnCDataComponents::registerAll);
            register(event, Registries.FLUID, BnCFluids::registerAll);
            register(event, NeoForgeRegistries.Keys.FLUID_TYPES, BnCFluidTypes::registerAll);

            register(event, Registries.ITEM, BnCEffects::registerAll); // Moved up to item.
            register(event, Registries.ITEM, BnCItems::registerAll);

            register(event, Registries.LOOT_CONDITION_TYPE, BnCLootConditions::registerAll);
            register(event, Registries.LOOT_FUNCTION_TYPE, BnCLootFunctions::registerAll);
            register(event, NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, BnCLootModifiers::registerAll);
            register(event, Registries.MENU, BnCMenuTypes::registerAll);
            register(event, Registries.PARTICLE_TYPE, BnCParticleTypes::registerAll);
            register(event, Registries.RECIPE_TYPE, BnCRecipeTypes::registerAll);
            register(event, Registries.RECIPE_SERIALIZER, BnCRecipeSerializers::registerAll);
        }

        @SubscribeEvent
        public static void registerPayloads(RegisterPayloadHandlersEvent event) {
            event.registrar("3")
                    .playToClient(ClearKegFluidContainerComponentsClientboundPacket.TYPE, ClearKegFluidContainerComponentsClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle())
                    .playToClient(MakeNextPlayerChatTipsyClientboundPacket.TYPE, MakeNextPlayerChatTipsyClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle())
                    .playToClient(SendRecipeBookValuesClientboundPacket.TYPE, SendRecipeBookValuesClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle())
                    .playToClient(SyncNumbedHeartsClientboundPacket.TYPE, SyncNumbedHeartsClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle())
                    .playToClient(SyncRagingStacksClientboundPacket.TYPE, SyncRagingStacksClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle())
                    .playToServer(JEITransferKegRecipeServerboundPacket.TYPE, JEITransferKegRecipeServerboundPacket.STREAM_CODEC, (payload, context) -> payload.handle((ServerPlayer) context.player()))
                    .playToServer(EMIFillFermentingRecipeServerboundPacket.TYPE, EMIFillFermentingRecipeServerboundPacket.STREAM_CODEC, (payload, context) -> payload.handle((ServerPlayer) context.player()))
                    .playToServer(EMIFillPouringRecipeServerboundPacket.TYPE, EMIFillPouringRecipeServerboundPacket.STREAM_CODEC, (payload, context) -> payload.handle((ServerPlayer) context.player()))
                    .playToServer(SetLabelContentsServerboundPacket.TYPE, SetLabelContentsServerboundPacket.STREAM_CODEC, (payload, context) -> payload.handle((ServerPlayer) context.player()));
        }

        public static <T> void register(RegisterEvent event, ResourceKey<Registry<T>> registry, Runnable registerMethod) {
            if (event.getRegistryKey() == registry)
                registerMethod.run();
        }
    }

    @EventBusSubscriber(modid = BrewinAndChewin.MODID)
    public static class GameEvents {
        @SubscribeEvent
        public static void onLootTableLoad(LootTableLoadEvent event) {
            float[] innards = BnCInnardsDrops.DROPS.get(event.getName());
            if (innards != null)
                event.getTable().addPool(BnCInnardsDrops.pool(innards[0], innards[1]).build());
        }
    }

}
