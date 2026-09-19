package io.redspace.irons_artifice.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.redspace.irons_artifice.item.kinetic.UseEffects;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Lets an item declare that using it costs nothing in movement, which is what a kinetic charge needs: you cannot
 * run something through at speed while the game holds you to a fifth of your walk and refuses to let you sprint.
 * <p>
 * One {@code isUsingItem()} guard drives the slowdown and both sprint refusals, so an
 * {@link UseEffects#unrestricting() unrestricting} item reports "not using" at all three reads. Anything without
 * the component reports the truth and behaves as vanilla does.
 */
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    /** Both reads inside {@code aiStep}: the slowdown guard and NeoForge's sprint-key branch. */
    @ModifyExpressionValue(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"
            )
    )
    private boolean irons_artifice$chargeIsNotASlowUse(boolean isUsingItem) {
        return isUsingItem && !UseEffects.useIsUnrestricted((LocalPlayer) (Object) this);
    }

    @ModifyExpressionValue(
            method = "canStartSprinting",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"
            )
    )
    private boolean irons_artifice$chargeDoesNotRefuseSprint(boolean isUsingItem) {
        return isUsingItem && !UseEffects.useIsUnrestricted((LocalPlayer) (Object) this);
    }
}
