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
 * Makes the mod's own arm pose authoritative for a kinetic stack, whatever use animation it declares.
 * <p>
 * NeoForge's {@code PlayerRenderer} patch appends its
 * {@code IClientItemExtensions.of(stack).getArmPose(...)} call <em>after</em> vanilla's branches, so
 * vanilla wins every tie. One of those branches turns {@code UseAnim.SPEAR} into
 * {@link HumanoidModel.ArmPose#THROW_SPEAR}, and {@code HumanoidModel.poseRightArm} then sets
 * {@code xRot = xRot * 0.5F - PI} -- the arm swings about ninety degrees up. A charging kinetic weapon
 * declares SPEAR (vanilla's own animation for a kinetic stack), so the mod's own arm pose was being
 * bypassed for exactly as long as the charge lasted.
 * <p>
 * Hoisting the extension call to the head changes only that one case, and only for the stacks
 * {@link GunItem#posedAsKineticWeapon} accepts: for every other state such an item can be in, vanilla's
 * method already fell through to the extension (the stack is not empty, is not {@code Items.CROSSBOW},
 * and reports no use animation but SPEAR), so the pose it returns is unchanged. A side effect worth
 * knowing: the pose's two-handed flag now holds during a charge too, which suppresses the off-hand
 * item the way it does at rest.
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
