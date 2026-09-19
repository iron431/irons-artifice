package io.redspace.irons_artifice.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.irons_artifice.client.BayonetAnimations;
import io.redspace.irons_artifice.client.ClientHelper;
import io.redspace.irons_artifice.client.KineticHitFeedback;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.kinetic.KineticWeapon;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void irons_artifice$hideHandsForScoping(AbstractClientPlayer player, float frameInterp, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, MultiBufferSource bufferSource, int lightCoords, CallbackInfo ci) {
        if (GunItem.isScoping(player)) {
            ci.cancel();
        }
    }

    /** The method carries no parameter names, so the equip offset is addressed by its local slot. */
    @ModifyVariable(
            method = "renderArmWithItem",
            at = @At("HEAD"),
            argsOnly = true,
            index = 7)
    private float irons_artifice$zeroGunEquipOffset(
            float inverseArmHeight,
            AbstractClientPlayer player,
            float frameInterp,
            float xRot,
            InteractionHand hand,
            float attack,
            ItemStack itemStack
    ) {
        return itemStack.getItem() instanceof GunItem ? 0.0F : inverseArmHeight;
    }

    @WrapOperation(
            method = "renderHandsWithItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            )
    )
    private void irons_artifice$hideOtherHandForTwoHandedGun(
            ItemInHandRenderer instance,
            AbstractClientPlayer player,
            float frameInterp,
            float xRot,
            InteractionHand hand,
            float attack,
            ItemStack itemStack,
            float inverseArmHeight,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int lightCoords,
            Operation<Void> original
    ) {
        if (player instanceof LocalPlayer localPlayer) {
            InteractionHand occupied = ClientHelper.getHandHoldingTwoHandedGun(localPlayer);
            if (occupied != null && hand != occupied) {
                return;
            }
        }
        original.call(instance, player, frameInterp, xRot, hand, attack, itemStack, inverseArmHeight, poseStack, bufferSource, lightCoords);
    }

    /**
     * A charging kinetic stack declares {@link UseAnim#SPEAR}, but the trident pose that goes with it is not the one
     * the mod wants. Reporting {@link UseAnim#NONE} leaves the bare {@code applyItemArmTransform} standing instead.
     */
    @WrapOperation(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"
            )
    )
    private UseAnim irons_artifice$skipTridentPoseForKineticWeapon(ItemStack itemStack, Operation<UseAnim> original) {
        UseAnim useAnim = original.call(itemStack);
        return useAnim == UseAnim.SPEAR && KineticWeapon.has(itemStack) ? UseAnim.NONE : useAnim;
    }

    /**
     * The mod's own charge pose, applied on top of that translate and just before the item renders. The use-state
     * guard also picks the injection point without an ordinal: the earlier {@code renderItem} call is the crossbow
     * branch, which a kinetic stack never enters.
     */
    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            )
    )
    private void irons_artifice$kineticFirstPersonPose(AbstractClientPlayer player, float frameInterp, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, MultiBufferSource bufferSource, int lightCoords, CallbackInfo ci) {
        if (!KineticWeapon.has(itemStack)) {
            return;
        }
        if (!player.isUsingItem() || player.getUseItemRemainingTicks() <= 0 || player.getUsedItemHand() != hand) {
            return;
        }
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        float timeHeld = itemStack.getUseDuration(player) - (player.getUseItemRemainingTicks() - frameInterp + 1.0F);
        BayonetAnimations.firstPersonUse(KineticHitFeedback.ticksSinceHit(player, frameInterp), poseStack, timeHeld, arm, itemStack);
    }

}
