package io.redspace.irons_artifice.mixin;

import io.redspace.irons_artifice.client.gui.GunScopeOverlay;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    @Inject(method = "getFieldOfViewModifier", at = @At("HEAD"), cancellable = true)
    private void irons_artifice$handleScopingFov(boolean firstPerson, float effectScale, CallbackInfoReturnable<Float> cir) {
        if (!firstPerson) {
            return;
        }
        if (!GunItem.isScoping((AbstractClientPlayer) (Object) this)) {
            return;
        }
        cir.setReturnValue(GunScopeOverlay.FOV_MODIFIER);
    }
}
