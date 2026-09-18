package io.redspace.irons_artifice.mixin;

import io.redspace.irons_artifice.client.gui.GunScopeOverlay;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.21.1's {@code getFieldOfViewModifier()} takes no arguments: the first-person test that 26.1 passes
 * in is made inside the method, off the camera type, exactly as vanilla's own spyglass branch does it.
 * <p>
 * Returning at the head skips vanilla's {@code fovEffectScale} lerp, which is also what that spyglass
 * branch does -- a scope's zoom is not an effect the player may dial down.
 */
@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    @Inject(method = "getFieldOfViewModifier", at = @At("HEAD"), cancellable = true)
    private void irons_artifice$handleScopingFov(CallbackInfoReturnable<Float> cir) {
        if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            return;
        }
        if (!GunItem.isScoping((AbstractClientPlayer) (Object) this)) {
            return;
        }
        cir.setReturnValue(GunScopeOverlay.FOV_MODIFIER);
    }
}
