package umpaz.brewinandchewin.fabric.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.brewinandchewin.fabric.access.PlayerPreHurtAttackStrengthAccess;

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerPreHurtAttackStrengthAccess {
    @Shadow public abstract float getAttackStrengthScale(float adjustTicks);

    @Unique
    private float brewinandchewin$preHurtAttackStrengthScale;

    @Inject(method = "attack", at = @At("HEAD"))
    private void brewinandchewin$storeAttackStrengthScale(Entity entity, CallbackInfo ci) {
        brewinandchewin$preHurtAttackStrengthScale = getAttackStrengthScale(0.0F);
    }

    @Override
    public float brewinandchewin$getPreHurtAttackStrengthScale() {
        return brewinandchewin$preHurtAttackStrengthScale;
    }

    @Override
    public void brewinandchewin$resetPreHurtAttackStrengthScale() {
        brewinandchewin$preHurtAttackStrengthScale = 0.0F;
    }
}
