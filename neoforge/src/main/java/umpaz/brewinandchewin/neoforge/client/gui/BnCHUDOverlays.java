package umpaz.brewinandchewin.neoforge.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import vectorwing.farmersdelight.client.gui.HUDOverlays;
import vectorwing.farmersdelight.common.registry.ModEffects;

import java.util.Random;

public class BnCHUDOverlays {
    protected static int foodIconsOffset;
    private static final ResourceLocation NOURISHMENT_ICONS_TEXTURE = ResourceLocation.fromNamespaceAndPath("farmersdelight", "textures/gui/fd_icons.png");

    public static final ResourceLocation FOOD_EMPTY_INTOXICATION_TEXTURE = BrewinAndChewin.asResource("hud/food_empty_intoxication");
    public static final ResourceLocation FOOD_HALF_INTOXICATION_TEXTURE = BrewinAndChewin.asResource("hud/food_half_intoxication");
    public static final ResourceLocation FOOD_FULL_INTOXICATION_TEXTURE = BrewinAndChewin.asResource("hud/food_full_intoxication");

    private static final ResourceLocation NAUSEA_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/nausea.png");

    private static float tipsyTransparencyModifier = 0.0F;

    public static void init(IEventBus modBus) {
        modBus.addListener(EventPriority.LOW, BnCHUDOverlays::register);
        NeoForge.EVENT_BUS.addListener(BnCHUDOverlays::onRenderGuiOverlayPost);
    }

    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, TipsyOverlay.ID, new TipsyOverlay());
        event.registerBelow(VanillaGuiLayers.FOOD_LEVEL, BrewinAndChewin.asResource( "food_offset"), (guiGraphics, deltaTracker) -> foodIconsOffset = Minecraft.getInstance().gui.rightHeight);
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, IntoxicationOverlay.ID, new IntoxicationOverlay());
    }

    // We want this to run after more important pre events.
    public static void onRenderGuiOverlayPost(RenderGuiLayerEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null)
            return;

        if ((event.getName() == VanillaGuiLayers.FOOD_LEVEL || event.getName() == HUDOverlays.NourishmentOverlay.ID) && BnCConfiguration.client().intoxicationFoodOverlay() &&
                mc.player.hasEffect(BnCEffects.INTOXICATION)) {
            event.setCanceled(true);
        }
    }

    public abstract static class BaseOverlay implements LayeredDraw.Layer {
        public boolean shouldRenderOverlay(Minecraft minecraft, Player player, GuiGraphics gui, DeltaTracker delta) {
            return !minecraft.options.hideGui && minecraft.gameMode != null && minecraft.gameMode.canHurtPlayer();
        }
    }

    public static class TipsyOverlay extends BaseOverlay {
        public static final ResourceLocation ID = BrewinAndChewin.asResource("tipsy");

        @Override
        public void render(GuiGraphics gui, DeltaTracker delta) {
            Minecraft mc = Minecraft.getInstance();
            if (shouldRenderOverlay(mc, mc.player, gui, delta)) {
                MobEffectInstance effect = mc.player.getEffect(BnCEffects.TIPSY);
                float distortionScale = mc.options.screenEffectScale().get().floatValue();
                float tipsyScale = Math.min((1 + effect.getAmplifier()) / 10.0F * 0.4F, 0.4F);
                if (distortionScale < 1.0F && tipsyScale > 0.0F) {
                    renderTipsyOverlay(gui, (1.0F - distortionScale) * tipsyScale * tipsyTransparencyModifier);
                    float partialTickModifier = delta.getGameTimeDeltaTicks() * (effect.endsWithin(60) ? -0.006F : 0.007F);
                    tipsyTransparencyModifier = Mth.clamp(tipsyTransparencyModifier + partialTickModifier, 0.0F, 1.0F);
                } else
                    tipsyTransparencyModifier = 0.0F;
            } else
                tipsyTransparencyModifier = 0.0F;
        }

        @Override
        public boolean shouldRenderOverlay(Minecraft minecraft, Player player, GuiGraphics gui, DeltaTracker delta) {
            return super.shouldRenderOverlay(minecraft, player, gui, delta) && !player.hasEffect(MobEffects.CONFUSION) && player.hasEffect(BnCEffects.TIPSY);
        }
    }

    public static class IntoxicationOverlay extends BaseOverlay {
        public static final ResourceLocation ID = BrewinAndChewin.asResource("intoxication");

        @Override
        public void render(GuiGraphics gui, DeltaTracker deltaTracker) {
            if (!BnCConfiguration.client().intoxicationFoodOverlay())
                return;

            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;

            if (!shouldRenderOverlay(minecraft, player, gui, deltaTracker))
                return;
            int top = minecraft.getWindow().getGuiScaledHeight() - foodIconsOffset;
            int right = minecraft.getWindow().getGuiScaledWidth() / 2 + 91;

            drawIntoxicationOverlay(player, minecraft, gui, right, top);
            minecraft.gui.rightHeight += 10;
        }

        @Override
        public boolean shouldRenderOverlay(Minecraft minecraft, Player player, GuiGraphics guiGraphics, DeltaTracker guiTicks) {
            return super.shouldRenderOverlay(minecraft, player, guiGraphics, guiTicks) && player != null && player.hasEffect(BnCEffects.INTOXICATION);
        }
    }

    public static void renderTipsyOverlay(GuiGraphics guiGraphics, float scalar) {
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        guiGraphics.setColor(scalar, 0.55F * scalar, 0.08F * scalar, 1.0F);
        guiGraphics.blit(NAUSEA_LOCATION, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    public static void drawIntoxicationOverlay(Player player, Minecraft minecraft, GuiGraphics graphics, int right, int top) {
        int ticks = minecraft.gui.getGuiTicks();
        Random rand = new Random();
        rand.setSeed(ticks * 312871L);

        RenderSystem.enableBlend();

        for (int i = 0; i < 10; ++i) {
            int x = (right - i * 8 - 9) + (int) (Mth.cos((ticks + i * 2) * 0.20F) * 2f);
            int y = top + (int) (Mth.sin((ticks + i * 2) * 0.25F) * 2f);

            float effectiveHungerOfBar = (float) player.getFoodData().getFoodLevel() / 2.0F - (float) i;

            ResourceLocation texture = player.hasEffect(ModEffects.NOURISHMENT) ? NOURISHMENT_ICONS_TEXTURE : getIntoxicationSprite(effectiveHungerOfBar >= 0.5F && effectiveHungerOfBar < 1.0F);

            if (player.hasEffect(ModEffects.NOURISHMENT)) {
                boolean isPlayerHealingWithSaturationAndNourishment =
                                player.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)
                                && player.isHurt()
                                && player.getFoodData().getFoodLevel() >= 18;
                int naturalHealingOffset = isPlayerHealingWithSaturationAndNourishment ? 18 : 0;

                graphics.blit(texture, x, y, 0, 0, 9, 9);

                if (effectiveHungerOfBar >= 1.0F) {
                    graphics.blit(texture, x, y, 18 + naturalHealingOffset, 0, 9, 9);
                } else if (effectiveHungerOfBar >= 0.5F) {
                    graphics.blit(texture, x, y, 9 + naturalHealingOffset, 0, 9, 9);
                }
                continue;
            }

            graphics.blitSprite(FOOD_EMPTY_INTOXICATION_TEXTURE, x, y, 9, 9);

            if (effectiveHungerOfBar >= 1.0F) {
                graphics.blitSprite(texture, x, y, 9, 9);
            } else if (effectiveHungerOfBar >= 0.5F) {
                graphics.blitSprite(texture, x, y, 9, 9);
            }
        }

        RenderSystem.disableBlend();
    }

    private static ResourceLocation getIntoxicationSprite(boolean half) {
        if (half)
            return FOOD_HALF_INTOXICATION_TEXTURE;
        return FOOD_FULL_INTOXICATION_TEXTURE;
    }
}