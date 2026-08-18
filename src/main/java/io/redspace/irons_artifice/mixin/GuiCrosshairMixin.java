package io.redspace.irons_artifice.mixin;

import io.redspace.irons_artifice.client.gui.CrosshairRenderer;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiCrosshairMixin {

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void irons_artifice$gunCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (CrosshairRenderer.renderGunCrosshair(graphics, deltaTracker)) {
            ci.cancel();
        }
    }
}
