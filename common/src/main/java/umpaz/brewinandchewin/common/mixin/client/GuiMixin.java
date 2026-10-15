package umpaz.brewinandchewin.common.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.utility.BnCHudIcons;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.attachment.TipsyHeartsAttachment;
import umpaz.brewinandchewin.common.registry.BnCEffects;

import java.util.Random;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow @Final
    private Minecraft minecraft;

    @Unique
    private int brewinandchewin$remainingHealth = 0;
    @Unique
    private float brewinandchewin$numbedAlpha = 1.0F;
    @Unique
    private boolean brewinandchewin$increaseNumbedAlpha = false;
    @Unique
    private boolean brewinandchewin$completedAbsorption = false;

    // TODO: Create an event for this overlay.
    @WrapOperation(method = "renderHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIZZZ)V", ordinal = 3))
    private void brewinandchewin$renderTipsyHearts(Gui instance,
                                                   GuiGraphics graphics,
                                                   Gui.HeartType heartType,
                                                   int heartX,
                                                   int heartY,
                                                   boolean hardcore,
                                                   boolean blinking,
                                                   boolean halfHeart,
                                                   Operation<Void> operation,
                                                   @Local(argsOnly = true) Player player,
                                                   @Local(argsOnly = true) float maxHealth,
                                                   @Local(argsOnly = true, ordinal = 4) int currentHealth,
                                                   @Local(argsOnly = true, ordinal = 5) int displayHealth,
                                                   @Local(argsOnly = true, ordinal = 6) int absorptionAmount,
                                                   @Local(name = "l") int heartIndex,
                                                   @Local(name = "i2") int fullHeart) {
        if (absorptionAmount <= 0)
            brewinandchewin$completedAbsorption = false;

        TipsyHeartsAttachment attachment = BrewinAndChewin.getHelper().getTipsyHeartsAttachment(player);
        if (!player.hasEffect(BnCEffects.TIPSY) || attachment == null || attachment.getNumbedHealth() <= 0 || absorptionAmount > 0 && brewinandchewin$completedAbsorption) {
            brewinandchewin$remainingHealth = 0;
            brewinandchewin$numbedAlpha = 1.0F;
            brewinandchewin$increaseNumbedAlpha = true;
            operation.call(instance, graphics, heartType, heartX, heartY, hardcore, blinking, halfHeart);
            return;
        }

        int ticks = minecraft.gui.getGuiTicks();
        Random rand = new Random();
        rand.setSeed(ticks * 312871L);

        float renderHealth = player.getHealth();

        int healthStart = Mth.ceil(renderHealth / 2) - 1;
        int healthEnd = Math.max(Mth.floor((renderHealth - attachment.getNumbedHealth()) / 2), -1);

        if (heartIndex > healthStart) {
            brewinandchewin$remainingHealth = 0;
            brewinandchewin$numbedAlpha = 1.0F;
            brewinandchewin$increaseNumbedAlpha = true;
            operation.call(instance, graphics, heartType, heartX, heartY, hardcore, blinking, halfHeart);
            return;
        }

        if (heartIndex == healthStart && absorptionAmount <= 0)
            brewinandchewin$remainingHealth = Math.min(Mth.ceil(attachment.getNumbedHealth()) - ((float) displayHealth % 1 < attachment.getNumbedHealth() % 1 ? 1 : 0), Mth.ceil((float) displayHealth));

        operation.call(instance, graphics, heartType, heartX, heartY, hardcore, blinking, halfHeart);

        if (brewinandchewin$remainingHealth <= 0)
            return;

        if (BnCConfiguration.client().numbedHeartFlickering() && attachment.getNumbedHealth() > 1 && attachment.getTicksUntilDamage() < 80 && heartIndex == healthStart && absorptionAmount == 0) {
            if (!Minecraft.getInstance().isPaused()) {
                float increase = Mth.lerp((float) (80 - attachment.getTicksUntilDamage()) / 80, 0.0F, 0.06F);
                brewinandchewin$numbedAlpha = Mth.clamp(brewinandchewin$numbedAlpha + (brewinandchewin$increaseNumbedAlpha ? increase : -increase), -0.01F, 1.01F);
                if (brewinandchewin$numbedAlpha < 0.0F)
                    brewinandchewin$increaseNumbedAlpha = true;
                if (brewinandchewin$numbedAlpha > 1.0F)
                    brewinandchewin$increaseNumbedAlpha = false;
            }
        } else if (heartIndex == healthStart) {
            brewinandchewin$numbedAlpha = 1.0F;
            brewinandchewin$increaseNumbedAlpha = true;
        }
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        graphics.setColor(1.0F, 1.0F, 1.0F, brewinandchewin$numbedAlpha);

        if (heartIndex == healthStart && halfHeart) {
            graphics.blitSprite(BnCHudIcons.getTipsyHalfHeartTexture(false, hardcore), heartX, heartY, 9, 9);
            brewinandchewin$remainingHealth -= 1;
        } else if (heartIndex == healthStart && renderHealth % 2 < 1 && brewinandchewin$remainingHealth == 1 || heartIndex == healthEnd && brewinandchewin$remainingHealth == 1) {
            graphics.blitSprite(BnCHudIcons.getTipsyRightHeartTexture(false, hardcore), heartX, heartY, 9, 9);
            brewinandchewin$remainingHealth -= 1;
        } else {
            graphics.blitSprite(BnCHudIcons.getTipsyFullHeartTexture(false, hardcore), heartX, heartY, 9, 9);
            brewinandchewin$remainingHealth -= 2;
        }

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @WrapOperation(method = "renderHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIZZZ)V", ordinal = 1))
    private void brewinandchewin$renderAbsorbingTipsyHearts(Gui instance,
                                                            GuiGraphics graphics,
                                                            Gui.HeartType heartType,
                                                            int heartX,
                                                            int heartY,
                                                            boolean hardcore,
                                                            boolean blinking,
                                                            boolean halfHeart,
                                                            Operation<Void> operation,
                                                            @Local(argsOnly = true) Player player,
                                                            @Local(argsOnly = true) float maxHealth,
                                                            @Local(argsOnly = true, ordinal = 4) int currentHealth,
                                                            @Local(argsOnly = true, ordinal = 5) int displayHealth,
                                                            @Local(argsOnly = true, ordinal = 6) int absoprtionAmount,
                                                            @Local(ordinal = 10) int heartIndex,
                                                            @Local(ordinal = 15) int fullHeart) {
        TipsyHeartsAttachment attachment = BrewinAndChewin.getHelper().getTipsyHeartsAttachment(player);
        if (!player.hasEffect(BnCEffects.TIPSY) || attachment == null || attachment.getNumbedHealth() <= 0) {
            brewinandchewin$remainingHealth = 0;
            brewinandchewin$numbedAlpha = 1.0F;
            brewinandchewin$increaseNumbedAlpha = true;
            operation.call(instance, graphics, heartType, heartX, heartY, hardcore, blinking, halfHeart);
            return;
        }

        int ticks = minecraft.gui.getGuiTicks();
        Random rand = new Random();
        rand.setSeed(ticks * 312871L);

        float renderHealth = player.getMaxHealth() + player.getAbsorptionAmount();

        int healthStart = Mth.ceil(renderHealth / 2) - 1;
        int healthEnd = Math.max(Mth.floor((renderHealth - attachment.getNumbedHealth() - 1) / 2), -1);

        if (heartIndex > healthStart) {
            brewinandchewin$remainingHealth = 0;
            brewinandchewin$numbedAlpha = 1.0F;
            brewinandchewin$increaseNumbedAlpha = true;
            operation.call(instance, graphics, heartType, heartX, heartY, hardcore, blinking, halfHeart);
            return;
        }

        if (heartIndex == healthStart) {
            brewinandchewin$completedAbsorption = false;
            brewinandchewin$remainingHealth = Math.min(Mth.ceil(attachment.getNumbedHealth()) - ((float) displayHealth % 1 < attachment.getNumbedHealth() % 1 ? 1 : 0), Mth.ceil((float) displayHealth));
        } else if (brewinandchewin$remainingHealth <= 0) {
            brewinandchewin$completedAbsorption = true;
            operation.call(instance, graphics, heartType, heartX, heartY, hardcore, blinking, halfHeart);
            return;
        }

        operation.call(instance, graphics, heartType, heartX, heartY, hardcore, blinking, halfHeart);

        if (BnCConfiguration.client().numbedHeartFlickering() && attachment.getNumbedHealth() > 1 && attachment.getTicksUntilDamage() < 80 && heartIndex == healthStart) {
            if (!Minecraft.getInstance().isPaused()) {
                float increase = Mth.lerp((float) (80 - attachment.getTicksUntilDamage()) / 80, 0.0F, 0.08F);
                brewinandchewin$numbedAlpha = Mth.clamp(brewinandchewin$numbedAlpha + (brewinandchewin$increaseNumbedAlpha ? increase : -increase), -0.01F, 1.01F);
                if (brewinandchewin$numbedAlpha < 0.0F)
                    brewinandchewin$increaseNumbedAlpha = true;
                if (brewinandchewin$numbedAlpha > 1.0F)
                    brewinandchewin$increaseNumbedAlpha = false;
            }
        } else if (heartIndex == healthStart) {
            brewinandchewin$numbedAlpha = 1.0F;
            brewinandchewin$increaseNumbedAlpha = true;
        }
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        graphics.setColor(1.0F, 1.0F, 1.0F, brewinandchewin$numbedAlpha);

        if (heartIndex == healthStart && halfHeart) {
            graphics.blitSprite(BnCHudIcons.getTipsyHalfHeartTexture(true, hardcore), heartX, heartY, 9, 9);
            brewinandchewin$remainingHealth -= 1;
        } else if (heartIndex == healthStart && renderHealth % 2 < 1 && brewinandchewin$remainingHealth == 1 || heartIndex == healthEnd && brewinandchewin$remainingHealth == 1) {
            graphics.blitSprite(BnCHudIcons.getTipsyRightHeartTexture(true, hardcore), heartX, heartY, 9, 9);
            brewinandchewin$remainingHealth -= 1;
        } else {
            graphics.blitSprite(BnCHudIcons.getTipsyFullHeartTexture(true, hardcore), heartX, heartY, 9, 9);
            brewinandchewin$remainingHealth -= 2;
        }

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
