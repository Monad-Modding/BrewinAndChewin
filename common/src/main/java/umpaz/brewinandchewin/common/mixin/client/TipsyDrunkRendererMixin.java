package umpaz.brewinandchewin.common.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import umpaz.brewinandchewin.common.registry.BnCEffects;

@Mixin(GameRenderer.class)
public class TipsyDrunkRendererMixin {

    @Shadow @Final
    Minecraft minecraft;

    @ModifyVariable(method = "renderLevel", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;mul(Lorg/joml/Matrix4fc;)Lorg/joml/Matrix4f;"), name = "posestack")
    private PoseStack brewinandchewin$renderTipsySpin(PoseStack pose, @Local(argsOnly = true) DeltaTracker delta) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.hasEffect(BnCEffects.TIPSY)) {
            float distortionScale = minecraft.options.screenEffectScale().get().floatValue();
            if (distortionScale > 0) {
                float ticks = ((LevelRendererAccessor)minecraft.levelRenderer).brewinandchewin$getTicks() + delta.getGameTimeDeltaPartialTick(false);
                int strength = Math.min(player.getEffect(BnCEffects.TIPSY).getAmplifier(), 11);
                float scaledStrength = strength * distortionScale;

                // left and right
                pose.rotateAround(new Quaternionf().fromAxisAngleDeg(0, 1, 0, Mth.cos(3 + ticks * 0.0295f) * scaledStrength), 0.5f, 0.5f, 0.5f);
                // up and down
                pose.rotateAround(new Quaternionf().fromAxisAngleDeg(1, 0, 1, Mth.sin(27 + ticks * 0.0132f) * scaledStrength), 0.5f, 0.5f, 0.5f);
                // circle
                float xDiff = (Mth.sin(ticks * 0.00253f) * (scaledStrength / 100F));
                float zDiff = (Mth.cos(ticks * 0.00784f) * (scaledStrength / 100F));
                pose.translate(xDiff, 0, zDiff);
            }
        }
        return pose;
    }

    @ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lnet/minecraft/world/phys/Vec3;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"), index = 1)
    private Matrix4f brewinandchewin$cullWithTipsySpin(Matrix4f original, @Local(argsOnly = true) DeltaTracker delta) {
        Player player = Minecraft.getInstance().player;
        // FIXME: It's not perfect, but this'll do.
        if (player != null && player.hasEffect(BnCEffects.TIPSY)) {
            float distortionScale = minecraft.options.screenEffectScale().get().floatValue();
            if (distortionScale > 0) {
                PoseStack pose = new PoseStack();
                float ticks = ((LevelRendererAccessor)minecraft.levelRenderer).brewinandchewin$getTicks() + delta.getGameTimeDeltaPartialTick(false);
                int strength = Math.min(player.getEffect(BnCEffects.TIPSY).getAmplifier(), 11);
                float scaledStrength = strength * distortionScale;

                // left and right
                pose.rotateAround(new Quaternionf().fromAxisAngleDeg(0, 1, 0, Mth.cos(3 + ticks * 0.0295f) * scaledStrength), 0.5f, 0.5f, 0.5f);
                // up and down
                pose.rotateAround(new Quaternionf().fromAxisAngleDeg(1, 0, 1, Mth.sin(27 + ticks * 0.0132f) * scaledStrength), 0.5f, 0.5f, 0.5f);
                // circle
                float xDiff = (Mth.sin(ticks * 0.00253f) * (scaledStrength / 100F));
                float zDiff = (Mth.cos(ticks * 0.00784f) * (scaledStrength / 100F));
                pose.translate(xDiff, 0, zDiff);

                original.mul(pose.last().pose());
            }
        }
        return original;
    }
}
