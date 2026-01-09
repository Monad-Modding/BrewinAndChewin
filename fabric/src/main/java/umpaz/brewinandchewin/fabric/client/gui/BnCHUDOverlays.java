package umpaz.brewinandchewin.fabric.client.gui;

/* decided to shelve appleskin compat for now
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import squeek.appleskin.client.HUDOverlayHandler;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import vectorwing.farmersdelight.common.registry.ModEffects;

import java.util.Random;

public class BnCHUDOverlays {
    public static int foodIconsOffset;
    private static final ResourceLocation NOURISHMENT_ICONS_TEXTURE = new ResourceLocation("farmersdelight", "textures/gui/fd_icons.png");

    public static final ResourceLocation FOOD_EMPTY_INTOXICATION_TEXTURE = BrewinAndChewin.asResource("hud/food_empty_intoxication");
    public static final ResourceLocation FOOD_HALF_INTOXICATION_TEXTURE = BrewinAndChewin.asResource("hud/food_half_intoxication");
    public static final ResourceLocation FOOD_FULL_INTOXICATION_TEXTURE = BrewinAndChewin.asResource("hud/food_full_intoxication");

    private static final ResourceLocation NAUSEA_LOCATION = new ResourceLocation("textures/misc/nausea.png");

    private static float tipsyTransparencyModifier = 0.0F;

    public static void init() {
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            IntoxicationOverlay.INSTANCE.render(graphics, tickDelta);
            TipsyOverlay.INSTANCE.render(graphics, tickDelta);
        });
    }

    public abstract static class BaseOverlay{
        public boolean shouldRenderOverlay(Minecraft minecraft, Player player, GuiGraphics gui, float f) {
            return !minecraft.options.hideGui && minecraft.gameMode != null && minecraft.gameMode.canHurtPlayer();
        }
    }

    public static class TipsyOverlay extends BaseOverlay {
        public static final TipsyOverlay INSTANCE = new TipsyOverlay();

        protected TipsyOverlay() {}

        public void render(GuiGraphics gui, float f) {
            Minecraft mc = Minecraft.getInstance();
            if (shouldRenderOverlay(mc, mc.player, gui, f)) {
                MobEffectInstance effect = mc.player.getEffect(BnCEffects.TIPSY.value());
                float distortionScale = mc.options.screenEffectScale().get().floatValue();
                float tipsyScale = Math.min((1 + effect.getAmplifier()) / 10.0F * 0.4F, 0.4F);
                if (distortionScale < 1.0F && tipsyScale > 0.0F) {
                    renderTipsyOverlay(gui, (1.0F - distortionScale) * tipsyScale * tipsyTransparencyModifier);
                    float partialTickModifier = f * (effect.endsWithin(60) ? -0.006F : 0.007F);
                    tipsyTransparencyModifier = Mth.clamp(tipsyTransparencyModifier + partialTickModifier, 0.0F, 1.0F);
                } else
                    tipsyTransparencyModifier = 0.0F;
            } else
                tipsyTransparencyModifier = 0.0F;
        }

        @Override
        public boolean shouldRenderOverlay(Minecraft minecraft, Player player, GuiGraphics gui, float f) {
            return !player.hasEffect(MobEffects.CONFUSION) && player.hasEffect(BnCEffects.TIPSY.value());
        }
    }

    public static class IntoxicationOverlay extends BaseOverlay {
        public static final IntoxicationOverlay INSTANCE = new IntoxicationOverlay();

        protected IntoxicationOverlay() {}

        public void render(GuiGraphics gui, float f) {
            // used to be a config
            if (!true)
                return;

            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;

            if (!shouldRenderOverlay(minecraft, player, gui, f))
                return;
            int top = foodIconsOffset;
            int right = minecraft.getWindow().getGuiScaledWidth() / 2 + 91;

            drawIntoxicationOverlay(player, minecraft, gui, right, top);
        }

        @Override
        public boolean shouldRenderOverlay(Minecraft minecraft, Player player, GuiGraphics guiGraphics, float f) {
            return super.shouldRenderOverlay(minecraft, player, guiGraphics, f) && player != null && player.hasEffect(BnCEffects.INTOXICATION.value());
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

            ResourceLocation texture = player.hasEffect(ModEffects.NOURISHMENT.get()) ? NOURISHMENT_ICONS_TEXTURE : getIntoxicationSprite(effectiveHungerOfBar >= 0.5F && effectiveHungerOfBar < 1.0F);

            if (player.hasEffect(ModEffects.NOURISHMENT.get())) {
                boolean isPlayerHealingWithSaturationAndNourishment =
                                player.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)
                                && player.isHurt()
                                && player.getFoodData().getFoodLevel() >= 18;
                int naturalHealingOffset = isPlayerHealingWithSaturationAndNourishment ? 18 : 0;

                RenderSystem.setShaderTexture(0, texture);
                graphics.blit(texture, x, y, 0, 0, 9, 9);

                if (effectiveHungerOfBar >= 1.0F) {
                    RenderSystem.setShaderTexture(0, texture);
                    graphics.blit(texture, x, y, 18 + naturalHealingOffset, 0, 9, 9);
                } else if (effectiveHungerOfBar >= 0.5F) {
                    RenderSystem.setShaderTexture(0, texture);
                    graphics.blit(texture, x, y, 9 + naturalHealingOffset, 0, 9, 9);
                }
                continue;
            }

            RenderSystem.setShaderTexture(0, FOOD_EMPTY_INTOXICATION_TEXTURE);
            graphics.blit(FOOD_EMPTY_INTOXICATION_TEXTURE, x, y, 0, 0, 9, 9);

            if (effectiveHungerOfBar >= 1.0F) {
                RenderSystem.setShaderTexture(0, texture);
                graphics.blit(texture, x, y, 0, 0, 9, 9);
            } else if (effectiveHungerOfBar >= 0.5F) {
                RenderSystem.setShaderTexture(0, texture);
                graphics.blit(texture, x, y, 0, 0, 9, 9);
            }
        }

        RenderSystem.disableBlend();

        if (FabricLoader.getInstance().isModLoaded("appleskin"))
            HUDOverlayHandler.INSTANCE.onRenderFood(graphics, player, top, right);
    }

    private static ResourceLocation getIntoxicationSprite(boolean half) {
        if (half)
            return FOOD_HALF_INTOXICATION_TEXTURE;
        return FOOD_FULL_INTOXICATION_TEXTURE;
    }
}

 */