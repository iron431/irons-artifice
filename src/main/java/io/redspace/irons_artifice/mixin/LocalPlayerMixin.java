package io.redspace.irons_artifice.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    /**
     * Backport of the use effects the vanilla spear (and upstream's bayonet, via
     * {@code UseEffects(canSprint=true, speedMultiplier=1)}) gets while charging.
     * <p>
     * 1.21.1 consults {@code isUsingItem()} in {@code aiStep} to scale movement input to 20% and to
     * block sprint (re)starts from the sprint key. A kinetic charge needs full-speed movement, so
     * report "not using" exclusively for bayonet charges at those checks.
     *
     * @see io.redspace.irons_artifice.item.BayonetLunge
     */
    @WrapOperation(
            method = {"aiStep", "canStartSprinting"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z")
    )
    private boolean irons_artifice$bayonetChargeKeepsMomentum(LocalPlayer player, Operation<Boolean> original) {
        return original.call(player) && !GunItem.isChargingBayonet(player);
    }
}
