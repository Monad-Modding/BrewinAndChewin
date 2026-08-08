package umpaz.brewinandchewin.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import umpaz.brewinandchewin.client.BnCClientSetup;
import umpaz.brewinandchewin.client.BrewinAndChewinClient;
import umpaz.brewinandchewin.client.gui.AgingCaskScreen;
import umpaz.brewinandchewin.client.gui.DistilleryScreen;
import umpaz.brewinandchewin.client.gui.KegScreen;
import umpaz.brewinandchewin.client.gui.KegTooltip;
import umpaz.brewinandchewin.common.mixin.client.ModelBakeryAccessor;
import umpaz.brewinandchewin.common.registry.BnCMenuTypes;
import umpaz.brewinandchewin.neoforge.client.gui.BnCHUDOverlays;
import umpaz.brewinandchewin.neoforge.client.model.CoasterWrappedModel;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.renderer.CoasterBlockEntityRenderer;
import umpaz.brewinandchewin.common.fluid.BnCFluidConstants;
import umpaz.brewinandchewin.neoforge.client.platform.BnCClientPlatfomHelperNeoForge;
import umpaz.brewinandchewin.neoforge.registry.BnCFluidTypes;

import java.util.ArrayList;
import java.util.List;

@Mod(value = BrewinAndChewin.MODID, dist = Dist.CLIENT)
public class BrewinAndChewinNeoForgeClient {
    public BrewinAndChewinNeoForgeClient(IEventBus eventBus) {
        BrewinAndChewinClient.init(new BnCClientPlatfomHelperNeoForge());
        BnCHUDOverlays.init(eventBus);
        BrewinAndChewin.isClient = true;
    }

    @EventBusSubscriber(modid = BrewinAndChewin.MODID, value = Dist.CLIENT)
    public static class GameEvents {
        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
            BnCClientSetup.appendLabelTooltip(event.getItemStack(), event.getToolTip());
        }
    }

    @EventBusSubscriber(modid = BrewinAndChewin.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(BnCMenuTypes.KEG, KegScreen::new);
            event.register(BnCMenuTypes.DISTILLERY, DistilleryScreen::new);
            event.register(BnCMenuTypes.AGING_CASK, AgingCaskScreen::new);
        }

        @SubscribeEvent
        public static void registerItemProperties(FMLClientSetupEvent event) {
            event.enqueueWork(BnCClientSetup::registerItemProperties);
        }

        @SubscribeEvent
        public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
            BnCClientSetup.registerItemColorHandlers(event::register);
        }

        @SubscribeEvent
        public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
            event.registerFluidType(createHoneyExtension(BnCFluidConstants.Colors.DEFAULT), BnCFluidTypes.HONEY);

            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.BEER), BnCFluidTypes.BEER);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.VODKA), BnCFluidTypes.VODKA);
            event.registerFluidType(createHoneyExtension(BnCFluidConstants.Colors.MEAD), BnCFluidTypes.MEAD);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.EGG_GROG), BnCFluidTypes.EGG_GROG);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.STRONGROOT_ALE), BnCFluidTypes.STRONGROOT_ALE);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.RICE_WINE), BnCFluidTypes.RICE_WINE);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.GLITTERING_GRENADINE), BnCFluidTypes.GLITTERING_GRENADINE);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.STEEL_TOE_STOUT), BnCFluidTypes.STEEL_TOE_STOUT);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.DREAD_NOG), BnCFluidTypes.DREAD_NOG);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.KOMBUCHA), BnCFluidTypes.KOMBUCHA);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.SACCHARINE_RUM), BnCFluidTypes.SACCHARINE_RUM);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.PALE_JANE), BnCFluidTypes.PALE_JANE);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.SALTY_FOLLY), BnCFluidTypes.SALTY_FOLLY);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.BLOODY_MARY), BnCFluidTypes.BLOODY_MARY);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.RED_RUM), BnCFluidTypes.RED_RUM);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.WITHERING_DROSS), BnCFluidTypes.WITHERING_DROSS);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.RED_WINE), BnCFluidTypes.RED_WINE);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.WHITE_WINE), BnCFluidTypes.WHITE_WINE);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.CURRANT_WINE), BnCFluidTypes.CURRANT_WINE);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.VERRUCA_WINE), BnCFluidTypes.VERRUCA_WINE);
            event.registerFluidType(createAlcoholExtension(BnCFluidConstants.Colors.TWISTED_WINE), BnCFluidTypes.TWISTED_WINE);

            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return BnCFluidConstants.Textures.FLAXEN_STILL_TEXTURE;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return BnCFluidConstants.Textures.FLAXEN_FLOWING_TEXTURE;
                }
            }, BnCFluidTypes.FLAXEN_CHEESE);
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return BnCFluidConstants.Textures.SCARLET_STILL_TEXTURE;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return BnCFluidConstants.Textures.SCARLET_FLOWING_TEXTURE;
                }
            }, BnCFluidTypes.SCARLET_CHEESE);
        }

        private static IClientFluidTypeExtensions createHoneyExtension(int color) {
            return new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return BnCFluidConstants.Textures.HONEY_FLUID_STILL_TEXTURE;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return BnCFluidConstants.Textures.HONEY_FLUID_FLOWING_TEXTURE;
                }

                @Override
                public int getTintColor() {
                    return color;
                }
            };
        }

        private static IClientFluidTypeExtensions createAlcoholExtension(int color) {
            return new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return BnCFluidConstants.Textures.FLUID_STILL_TEXTURE;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return BnCFluidConstants.Textures.FLUID_FLOWING_TEXTURE;
                }

                @Override
                public int getTintColor() {
                    return color;
                }
            };
        }

        private static final List<ResourceLocation> MODELS = new ArrayList<>();

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            BnCClientSetup.registerBlockEntityRenderers(event::registerBlockEntityRenderer);
        }

        @SubscribeEvent
        public static void registerParticles(RegisterParticleProvidersEvent event) {
            BnCClientSetup.registerParticles(event::registerSpriteSet);
        }

        @SubscribeEvent
        public static void registerKegTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(KegTooltip.KegTooltipComponent.class, KegTooltip::new);
        }

        @SubscribeEvent
        public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
            BnCClientSetup.registerReloadListeners(event::registerReloadListener);
        }

        @SubscribeEvent
        public static void registerColorHandlers(RegisterColorHandlersEvent.Block event) {
            BnCClientSetup.registerColorHandlers(event::register);
        }

        @SubscribeEvent
        public static void registerModels(ModelEvent.RegisterAdditional event) {
            CoasterBlockEntityRenderer.resetCache();
            MODELS.addAll(BnCClientSetup.getModels(Minecraft.getInstance().getResourceManager(), Runnable::run).join());
            event.register(ModelResourceLocation.standalone(BrewinAndChewin.asResource("block/coaster")));
            event.register(ModelResourceLocation.standalone(BrewinAndChewin.asResource("block/coaster_tray")));
            for (ResourceLocation model : BnCClientSetup.getBottleRackModels()) {
                event.register(ModelResourceLocation.standalone(model));
            }
        }

        @SubscribeEvent
        public static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
            for (ResourceLocation entry : MODELS) {
                event.getModels().put(ModelResourceLocation.standalone(entry.withPath(path -> "brewinandchewin/coaster/" + path)), new CoasterWrappedModel(bakeModel(event, entry)));
            }
            MODELS.clear();
        }

        private static BakedModel bakeModel(ModelEvent.ModifyBakingResult event, ResourceLocation path) {
            UnbakedModel unbaked = ((ModelBakeryAccessor)event.getModelBakery()).brewinandchewin$getModel(path);
            unbaked.resolveParents(location -> ((ModelBakeryAccessor)event.getModelBakery()).brewinandchewin$getModel(location));
            ModelResourceLocation modelResource = ModelResourceLocation.standalone(path);
            return unbaked.bake(event.getModelBakery().new ModelBakerImpl((rl, material) -> material.sprite(), modelResource), event.getTextureGetter(), BlockModelRotation.X0_Y0);
        }
    }
}
