package umpaz.brewinandchewin.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.BnCClientSetup;
import umpaz.brewinandchewin.client.BrewinAndChewinClient;
import umpaz.brewinandchewin.client.gui.KegScreen;
import umpaz.brewinandchewin.client.gui.KegTooltip;
import umpaz.brewinandchewin.client.utility.BnCClientTextUtils;
import umpaz.brewinandchewin.common.fluid.BnCFluidConstants;
import umpaz.brewinandchewin.common.network.clientbound.*;
import umpaz.brewinandchewin.common.registry.BnCFluids;
import umpaz.brewinandchewin.common.registry.BnCMenuTypes;
import umpaz.brewinandchewin.fabric.client.gui.BnCHUDOverlays;
import umpaz.brewinandchewin.fabric.client.platform.BnCClientPlatformHelperFabric;
import umpaz.brewinandchewin.fabric.registry.BnCFluidsImpl;
import umpaz.brewinandchewin.fabric.registry.BnCLootModificationEvents;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BrewinAndChewinFabricClient implements ClientModInitializer {
        @Override
        public void onInitializeClient() {
                BrewinAndChewinClient.init(new BnCClientPlatformHelperFabric());
                BrewinAndChewin.isClient = true;

                BnCHUDOverlays.init();

                MenuScreens.register(BnCMenuTypes.KEG, KegScreen::new);
                BnCClientSetup.registerBlockEntityRenderers(BlockEntityRenderers::register);
                BnCClientSetup.registerParticles((particleType, factory) -> registerParticle(particleType, factory));
                ClientTooltipComponentCallback.EVENT.register(data -> {
                        if (KegTooltip.KegTooltipComponent.class.isAssignableFrom(data.getClass())) {
                                return new KegTooltip((KegTooltip.KegTooltipComponent) data);
                        }
                        return null;
                });
                BnCClientSetup.registerReloadListeners(preparableReloadListener -> ResourceLoader
                                .get(PackType.CLIENT_RESOURCES)
                                .registerReloadListener(preparableReloadListener.getId(), preparableReloadListener));
                ClientReceiveMessageEvents.ALLOW_CHAT.register((message, chatMessage, profile, bound, timestamp) -> {
                        if (chatMessage != null) {
                                BnCClientTextUtils.setupChatMessage(chatMessage);
                                PlayerChatMessage tipsyMessage = BnCClientTextUtils.getTipsyMessage();
                                if (tipsyMessage != null && bound.chatType().isBound()) {
                                        BnCClientTextUtils.clearTipsyMessage();
                                        MutableComponent boundChat = BnCClientTextUtils.getStyledChatPrefix(bound,
                                                        bound.decorate(Component.literal("")).copy());
                                        MutableComponent newMessage = tipsyMessage.decoratedContent().copy()
                                                        .withStyle(bound.chatType().value().chat().style());

                                        Minecraft.getInstance().gui.hud.getChat().addMessage(
                                                        boundChat.append(newMessage.copy().withStyle(
                                                                        bound.chatType().value().chat().style())),
                                                        tipsyMessage.signature(), GuiMessageSource.PLAYER,
                                                        GuiMessageTag.chatModified(chatMessage.signedContent()));
                                        Minecraft.getInstance().getNarrator()
                                                        .sayChatQueued(boundChat.append(newMessage.copy().withStyle(
                                                                        bound.chatType().value().narration().style())));

                                        if (BnCClientTextUtils.clearDelayAmount <= 0) {
                                                BnCClientTextUtils.tipsyMessageLevel = 0;
                                                BnCClientTextUtils.randomSeed = 0L;
                                                BnCClientTextUtils.generatedRandom = false;
                                        } else {
                                                --BnCClientTextUtils.clearDelayAmount;
                                        }
                                        return false;
                                }
                        }
                        BnCClientTextUtils.clearDelayAmount = 0;
                        BnCClientTextUtils.tipsyMessageLevel = 0;
                        BnCClientTextUtils.randomSeed = 0L;
                        BnCClientTextUtils.generatedRandom = false;
                        return true;
                });
                registerNetwork();
                registerFluidRenderers();
        }

        private static void registerNetwork() {
                ClientPlayNetworking.registerGlobalReceiver(ClearKegFluidContainerComponentsClientboundPacket.TYPE,
                                (payload, context) -> payload.handle());
                ClientPlayNetworking.registerGlobalReceiver(MakeNextPlayerChatTipsyClientboundPacket.TYPE,
                                (payload, context) -> payload.handle());
                ClientPlayNetworking.registerGlobalReceiver(SendRecipeBookValuesClientboundPacket.TYPE,
                                (payload, context) -> payload.handle());
                ClientPlayNetworking.registerGlobalReceiver(SyncNumbedHeartsClientboundPacket.TYPE,
                                (payload, context) -> payload.handle());
                ClientPlayNetworking.registerGlobalReceiver(SyncRagingStacksClientboundPacket.TYPE,
                                (payload, context) -> payload.handle());
        }

        public static void registerFluidRenderers() {
                FluidRenderingRegistry.register(BnCFluidsImpl.MILK, BnCFluidsImpl.FLOWING_MILK,
                                new FluidModel.Unbaked(
                                                new Material(BrewinAndChewin.asResource("block/milk_still")),
                                                new Material(BrewinAndChewin.asResource("block/milk_flowing")),
                                                null,
                                                null));
                FluidRenderingRegistry.register(BnCFluids.HONEY, BnCFluids.FLOWING_HONEY,
                                createHoneyRenderHandler(BnCFluidConstants.Colors.DEFAULT));

                FluidRenderingRegistry.register(BnCFluids.BEER, BnCFluids.FLOWING_BEER,
                                createWaterExtension(BnCFluidConstants.Colors.BEER));
                FluidRenderingRegistry.register(BnCFluids.VODKA, BnCFluids.FLOWING_VODKA,
                                createWaterExtension(BnCFluidConstants.Colors.VODKA));
                FluidRenderingRegistry.register(BnCFluids.MEAD, BnCFluids.FLOWING_MEAD,
                                createHoneyRenderHandler(BnCFluidConstants.Colors.MEAD));
                FluidRenderingRegistry.register(BnCFluids.EGG_GROG, BnCFluids.FLOWING_EGG_GROG,
                                createWaterExtension(BnCFluidConstants.Colors.EGG_GROG));
                FluidRenderingRegistry.register(BnCFluids.STRONGROOT_ALE, BnCFluids.FLOWING_STRONGROOT_ALE,
                                createWaterExtension(BnCFluidConstants.Colors.STRONGROOT_ALE));
                FluidRenderingRegistry.register(BnCFluids.RICE_WINE, BnCFluids.FLOWING_RICE_WINE,
                                createWaterExtension(BnCFluidConstants.Colors.RICE_WINE));
                FluidRenderingRegistry.register(BnCFluids.GLITTERING_GRENADINE, BnCFluids.FLOWING_GLITTERING_GRENADINE,
                                createWaterExtension(BnCFluidConstants.Colors.GLITTERING_GRENADINE));
                FluidRenderingRegistry.register(BnCFluids.STEEL_TOE_STOUT, BnCFluids.FLOWING_STEEL_TOE_STOUT,
                                createWaterExtension(BnCFluidConstants.Colors.STEEL_TOE_STOUT));
                FluidRenderingRegistry.register(BnCFluids.DREAD_NOG, BnCFluids.FLOWING_DREAD_NOG,
                                createWaterExtension(BnCFluidConstants.Colors.DREAD_NOG));
                FluidRenderingRegistry.register(BnCFluids.KOMBUCHA, BnCFluids.FLOWING_KOMBUCHA,
                                createWaterExtension(BnCFluidConstants.Colors.KOMBUCHA));
                FluidRenderingRegistry.register(BnCFluids.SACCHARINE_RUM, BnCFluids.FLOWING_SACCHARINE_RUM,
                                createWaterExtension(BnCFluidConstants.Colors.SACCHARINE_RUM));
                FluidRenderingRegistry.register(BnCFluids.PALE_JANE, BnCFluids.FLOWING_PALE_JANE,
                                createWaterExtension(BnCFluidConstants.Colors.PALE_JANE));
                FluidRenderingRegistry.register(BnCFluids.SALTY_FOLLY, BnCFluids.FLOWING_SALTY_FOLLY,
                                createWaterExtension(BnCFluidConstants.Colors.SALTY_FOLLY));
                FluidRenderingRegistry.register(BnCFluids.BLOODY_MARY, BnCFluids.FLOWING_BLOODY_MARY,
                                createWaterExtension(BnCFluidConstants.Colors.BLOODY_MARY));
                FluidRenderingRegistry.register(BnCFluids.RED_RUM, BnCFluids.FLOWING_RED_RUM,
                                createWaterExtension(BnCFluidConstants.Colors.RED_RUM));
                FluidRenderingRegistry.register(BnCFluids.WITHERING_DROSS, BnCFluids.FLOWING_WITHERING_DROSS,
                                createWaterExtension(BnCFluidConstants.Colors.WITHERING_DROSS));

                FluidRenderingRegistry.register(BnCFluids.FLAXEN_CHEESE, BnCFluids.FLOWING_FLAXEN_CHEESE,
                                new FluidModel.Unbaked(
                                                new Material(BnCFluidConstants.Textures.FLAXEN_STILL_TEXTURE),
                                                new Material(BnCFluidConstants.Textures.FLAXEN_FLOWING_TEXTURE),
                                                null,
                                                null));
                FluidRenderingRegistry.register(BnCFluids.SCARLET_CHEESE, BnCFluids.FLOWING_SCARLET_CHEESE,
                                new FluidModel.Unbaked(
                                                new Material(BnCFluidConstants.Textures.SCARLET_STILL_TEXTURE),
                                                new Material(BnCFluidConstants.Textures.SCARLET_FLOWING_TEXTURE),
                                                null,
                                                null));
        }

        private static FluidModel.Unbaked createHoneyRenderHandler(int color) {
                return new FluidModel.Unbaked(
                                new Material(BnCFluidConstants.Textures.HONEY_FLUID_STILL_TEXTURE),
                                new Material(BnCFluidConstants.Textures.HONEY_FLUID_FLOWING_TEXTURE),
                                null,
                                BlockTintSources.constant(color));
        }

        private static <T extends net.minecraft.core.particles.ParticleOptions> void registerParticle(
                        net.minecraft.core.particles.ParticleType<?> type, BnCClientSetup.ParticleFactory<?> factory) {
                @SuppressWarnings("unchecked")
                net.minecraft.core.particles.ParticleType<T> typed = (net.minecraft.core.particles.ParticleType<T>) type;
                @SuppressWarnings("unchecked")
                BnCClientSetup.ParticleFactory<T> typedFactory = (BnCClientSetup.ParticleFactory<T>) factory;
                ParticleProviderRegistry.getInstance().register(typed, typedFactory::create);
        }

        private static FluidModel.Unbaked createWaterExtension(int color) {
                return new FluidModel.Unbaked(
                                new Material(BnCFluidConstants.Textures.FLUID_STILL_TEXTURE),
                                new Material(BnCFluidConstants.Textures.FLUID_FLOWING_TEXTURE),
                                null,
                                BlockTintSources.constant(color));
        }
}
