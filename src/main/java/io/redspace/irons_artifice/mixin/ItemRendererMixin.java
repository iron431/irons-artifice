package io.redspace.irons_artifice.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.irons_artifice.client.RenderingEntityTracker;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    private static final String RENDER_STATIC = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V";

    @Inject(method = RENDER_STATIC, at = @At("HEAD"))
    private void irons_artifice$pushRenderingEntity(LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, Level level, int packedLight, int packedOverlay, int seed, CallbackInfo ci) {
        RenderingEntityTracker.push(entity);
    }

    @Inject(method = RENDER_STATIC, at = @At("RETURN"))
    private void irons_artifice$popRenderingEntity(LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, Level level, int packedLight, int packedOverlay, int seed, CallbackInfo ci) {
        RenderingEntityTracker.pop();
    }
}
