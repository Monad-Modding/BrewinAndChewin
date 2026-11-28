package umpaz.brewinandchewin.fabric.client.gui;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import vectorwing.farmersdelight.common.registry.ModEffects;

import java.util.Random;

public class BnCHUDOverlays {
    public static int foodIconsOffset;
    private static final Identifier NOURISHMENT_ICONS_TEXTURE = Identifier.fromNamespaceAndPath("farmersdelight", "textures/gui/fd_icons.png");

    public static final Identifier FOOD_EMPTY_INTOXICATION_TEXTURE = BrewinAndChewin.asResource("hud/food_empty_intoxication");
    public static final Identifier FOOD_HALF_INTOXICATION_TEXTURE = BrewinAndChewin.asResource("hud/food_half_intoxication");
    public static final Identifier FOOD_FULL_INTOXICATION_TEXTURE = BrewinAndChewin.asResource("hud/food_full_intoxication");

    private static final Identifier NAUSEA_LOCATION = Identifier.withDefaultNamespace("textures/misc/nausea.png");

    private static float tipsyTransparencyModifier = 0.0F;

    public static void init() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.MISC_OVERLAYS, BrewinAndChewin.asResource("tipsy"), TipsyOverlay.INSTANCE::render);
        HudElementRegistry.attachElementAfter(VanillaHudElements.FOOD_BAR, BrewinAndChewin.asResource("intoxication"), IntoxicationOverlay.INSTANCE::render);
    }

    public abstract static class BaseOverlay {
        public boolean shouldRenderOverlay(Minecraft minecraft, Player player, GuiGraphicsExtractor gui, DeltaTracker delta) {
            return !minecraft.gui.hud.isHidden() && minecraft.gameMode != null && minecraft.gameMode.canHurtPlayer();
        }
    }

    public static class TipsyOverlay extends BaseOverlay {
        public static final TipsyOverlay INSTANCE = new TipsyOverlay();

        protected TipsyOverlay() {}

        public void render(GuiGraphicsExtractor gui, DeltaTracker delta) {
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
        public boolean shouldRenderOverlay(Minecraft minecraft, Player player, GuiGraphicsExtractor gui, DeltaTracker delta) {
            return !player.hasEffect(MobEffects.NAUSEA) && player.hasEffect(BnCEffects.TIPSY);
        }
    }

    public static class IntoxicationOverlay extends BaseOverlay {
        public static final IntoxicationOverlay INSTANCE = new IntoxicationOverlay();

        protected IntoxicationOverlay() {}

        public void render(GuiGraphicsExtractor gui, DeltaTracker deltaTracker) {
            if (!BnCConfiguration.client().intoxicationFoodOverlay())
                return;

            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;

            if (!shouldRenderOverlay(minecraft, player, gui, deltaTracker))
                return;
            int top = foodIconsOffset;
            int right = minecraft.getWindow().getGuiScaledWidth() / 2 + 91;

            drawIntoxicationOverlay(player, minecraft, gui, right, top);
        }

        @Override
        public boolean shouldRenderOverlay(Minecraft minecraft, Player player, GuiGraphicsExtractor guiGraphics, DeltaTracker guiTicks) {
            return super.shouldRenderOverlay(minecraft, player, guiGraphics, guiTicks) && player != null && player.hasEffect(BnCEffects.INTOXICATION);
        }
    }

    public static void renderTipsyOverlay(GuiGraphicsExtractor guiGraphics, float scalar) {
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        int color = ARGB.colorFromFloat(1.0F, scalar, 0.55F * scalar, 0.08F * scalar);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, NAUSEA_LOCATION, 0, 0, 0.0F, 0.0F, width, height, width, height, color);
    }

    public static void drawIntoxicationOverlay(Player player, Minecraft minecraft, GuiGraphicsExtractor graphics, int right, int top) {
        int ticks = minecraft.gui.hud.getGuiTicks();
        Random rand = new Random();
        rand.setSeed(ticks * 312871L);

        for (int i = 0; i < 10; ++i) {
            int x = (right - i * 8 - 9) + (int) (Mth.cos((ticks + i * 2) * 0.20F) * 2f);
            int y = top + (int) (Mth.sin((ticks + i * 2) * 0.25F) * 2f);

            float effectiveHungerOfBar = (float) player.getFoodData().getFoodLevel() / 2.0F - (float) i;

            Identifier texture = player.hasEffect(ModEffects.NOURISHMENT) ? NOURISHMENT_ICONS_TEXTURE : getIntoxicationSprite(effectiveHungerOfBar >= 0.5F && effectiveHungerOfBar < 1.0F);

            if (player.hasEffect(ModEffects.NOURISHMENT)) {
                boolean isPlayerHealingWithSaturationAndNourishment =
                        player.isHurt() && player.getFoodData().getFoodLevel() >= 18;
                int naturalHealingOffset = isPlayerHealingWithSaturationAndNourishment ? 18 : 0;

                graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, 9, 9, 256, 256);

                if (effectiveHungerOfBar >= 1.0F) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) (18 + naturalHealingOffset), 0.0F, 9, 9, 256, 256);
                } else if (effectiveHungerOfBar >= 0.5F) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) (9 + naturalHealingOffset), 0.0F, 9, 9, 256, 256);
                }
                continue;
            }

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FOOD_EMPTY_INTOXICATION_TEXTURE, x, y, 9, 9);

            if (effectiveHungerOfBar >= 1.0F) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, 9, 9);
            } else if (effectiveHungerOfBar >= 0.5F) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, 9, 9);
            }
        }
    }

    private static Identifier getIntoxicationSprite(boolean half) {
        if (half)
            return FOOD_HALF_INTOXICATION_TEXTURE;
        return FOOD_FULL_INTOXICATION_TEXTURE;
    }
}