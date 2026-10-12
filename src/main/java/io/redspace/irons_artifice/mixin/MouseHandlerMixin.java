package io.redspace.irons_artifice.mixin;

import io.redspace.irons_artifice.client.gui.GunScopeOverlay;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0))
    private Object irons_artifice$scopedSensitivity(OptionInstance<Double> sensitivity) {
        return GunScopeOverlay.scopedSensitivity(sensitivity.get());
    }
}
