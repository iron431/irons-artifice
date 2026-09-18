package io.redspace.irons_artifice.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.redspace.irons_artifice.item.kinetic.UseEffects;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Lets an item declare that using it costs nothing in movement, which is what a kinetic charge needs:
 * you cannot run something through at speed while the game is holding you to a fifth of your walk and
 * refusing to let you sprint.
 * <p>
 * 1.21.1 hardcodes both. {@code aiStep} scales the movement impulse by {@code 0.2F} and clears
 * {@code sprintTriggerTime} behind a plain {@code isUsingItem()} guard, and both
 * {@code canStartSprinting} and NeoForge's own added sprint-key branch in {@code aiStep} refuse on
 * {@code isUsingItem()}. 26.1 replaced each of those reads with the
 * {@code minecraft:use_effects} component: {@code itemUseSpeedMultiplier()} in place of the constant
 * and {@code isSlowDueToUsingItem()} -- {@code isUsingItem() && !canSprint()} -- in place of the
 * guard. The 26.1 source this was ported from set
 * {@code new UseEffects(true, false, 1)} on the kinetic weapon, so a charge was free of both.
 * <p>
 * {@link UseEffects} carries that data here. Because 1.21.1 shares one guard between the slowdown and
 * the sprint refusal, the two are answered together: a use item that is
 * {@link UseEffects#unrestricting() unrestricting} reports "not using" at all three reads, which
 * leaves the impulse untouched, {@code sprintTriggerTime} running and the sprint allowed. Anything
 * without the component reports the truth and behaves exactly as vanilla does.
 */
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    /**
     * Both reads inside {@code aiStep}: vanilla's slowdown guard and NeoForge's sprint-key branch.
     */
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
