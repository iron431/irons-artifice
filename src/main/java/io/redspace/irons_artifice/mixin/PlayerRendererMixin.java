package io.redspace.irons_artifice.mixin;

import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes the mod's own arm pose win for a kinetic stack, whatever use animation it declares.
 * <p>
 * NeoForge appends its {@code IClientItemExtensions.of(stack).getArmPose(...)} call after vanilla's branches, one
 * of which turns {@code UseAnim.SPEAR} into {@link HumanoidModel.ArmPose#THROW_SPEAR} and swings the arm ninety
 * degrees up. Hoisting the call to the head changes only the stacks {@link GunItem#posedAsKineticWeapon} accepts;
 * every other state already fell through to the extension. One side effect: the pose's two-handed flag now holds
 * during a charge, suppressing the off-hand item the way it does at rest.
 */
@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private static void irons_artifice$modPoseWinsOverUseAnimation(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!GunItem.posedAsKineticWeapon(itemStack)) {
            return;
        }
        HumanoidModel.ArmPose pose = IClientItemExtensions.of(itemStack).getArmPose(player, hand, itemStack);
        if (pose != null) {
            cir.setReturnValue(pose);
        }
    }
}
