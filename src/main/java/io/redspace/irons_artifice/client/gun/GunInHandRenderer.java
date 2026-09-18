package io.redspace.irons_artifice.client.gun;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.client.MuzzleFlashEmitter;
import io.redspace.irons_artifice.data.HandOccupancy;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class GunInHandRenderer extends GeoItemRenderer<GunItem> {

    public GunInHandRenderer(GeoModel<GunItem> model) {
        super(model);
    }

    @Override
    public void preRender(@NotNull PoseStack poseStack, GunItem animatable, @NotNull BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (isLeftHandPerspective(this.renderPerspective)) {
            PoseStack.Pose last = poseStack.last();
            last.pose().scale(-1f, 1f, 1f);
            // compensate for weird lighting
            Matrix3f normal = last.normal();
            normal.scale(-1, -1, 1);
        }
    }

    @Override
    public void renderRecursively(@NotNull PoseStack poseStack, GunItem animatable, @NotNull GeoBone bone, @NotNull RenderType renderType, @NotNull MultiBufferSource bufferSource, @NotNull VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (isReRender) {
            return;
        }
        handleAttachmentRendering(poseStack, bone, bufferSource, partialTick, packedLight);
        handleMuzzleFlashEmission(poseStack, bone);
        handleFirstPersonHandRendering(poseStack, bone, bufferSource, partialTick, packedLight);
    }

    protected void handleAttachmentRendering(@NotNull PoseStack poseStack, @NotNull GeoBone bone, @NotNull MultiBufferSource bufferSource, float partialTick, int packedLight) {
        AttachmentMap attachments = currentAttachments();
        if (attachments.isEmpty()) {
            return;
        }
        ResourceLocation attachmentId = attachments.attachments().get(bone.getName());
        if (attachmentId == null) {
            return;
        }
        Optional<AttachmentGeoRenderer> renderer = AttachmentRenderableRegistry.get(attachmentId);
        if (renderer.isEmpty()) {
            return;
        }
        atBonePivot(poseStack, bone, pose -> renderer.get().renderAttachment(pose, bufferSource, packedLight, partialTick));
    }

    protected void handleMuzzleFlashEmission(@NotNull PoseStack poseStack, @NotNull GeoBone bone) {
        if (!GunBones.SOCKET_MUZZLE.equals(bone.getName()) || !isHandPerspective(this.renderPerspective)) {
            return;
        }
        LivingEntity owner = currentItemOwner();
        if (owner == null) {
            return;
        }
        int ownerId = owner.getId();
        atBonePivot(poseStack, bone, pose -> MuzzleFlashEmitter.tryEmit(ownerId, pose));
    }

    protected void handleFirstPersonHandRendering(@NotNull PoseStack poseStack, @NotNull GeoBone bone, @NotNull MultiBufferSource bufferSource, float partialTick, int packedLight) {
        // Use Marker Bones "right_arm" and "left_arm" to render the player's hands in first person
        boolean leftArm = GunBones.LEFT_ARM.equals(bone.getName());
        if (!leftArm && !GunBones.RIGHT_ARM.equals(bone.getName())) {
            return;
        }
        if (!isFirstPersonPerspective(this.renderPerspective)) {
            return;
        }

        AbstractClientPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer)) {
            return;
        }
        if (leftArm && currentOccupancy(player) != HandOccupancy.BOTH) {
            return;
        }
        ResourceLocation skinTexture = player.getSkin().texture();
        final RenderType renderType = getRenderType(this.animatable, skinTexture, bufferSource, partialTick);
        ModelPart modelPart = leftArm ? renderer.getModel().leftArm : renderer.getModel().rightArm;
        atBonePivot(poseStack, bone, pose -> renderFirstPersonHand(bufferSource, renderType, modelPart, pose, packedLight));
    }

    protected void renderFirstPersonHand(@NotNull MultiBufferSource bufferSource, @NotNull RenderType renderType, @NotNull ModelPart modelPart, @NotNull PoseStack poseStack, int packedLight) {
        modelPart.x = 0;
        modelPart.y = 0;
        modelPart.z = 0;
        modelPart.xRot = 0;
        modelPart.yRot = 0;
        modelPart.zRot = 0;
        poseStack.pushPose();
        poseStack.scale(-1, -1, 1);
        // no clue where these numbers come from (manually lined up from block bench)
        poseStack.translate(1 / 16f, -10 / 16f, 0 / 16f);
        modelPart.render(poseStack, bufferSource.getBuffer(renderType), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    /**
     * Run {@code action} with the pose stack sitting on the given bone's pivot.
     * <p>
     * {@code renderRecursively} has already popped back to the parent bone by the time this is reached, so the bone's
     * own matrix has to be re-prepped; and prepping ends back at the model origin, so the pivot has to be re-applied
     * or everything hung off the bone is displaced by the pivot vector.
     */
    protected void atBonePivot(@NotNull PoseStack poseStack, @NotNull GeoBone bone, @NotNull Consumer<PoseStack> action) {
        poseStack.pushPose();
        RenderUtil.prepMatrixForBone(poseStack, bone);
        RenderUtil.translateToPivotPoint(poseStack, bone);
        action.accept(poseStack);
        poseStack.popPose();
    }

    protected AttachmentMap currentAttachments() {
        ItemStack stack = this.currentItemStack;
        return stack == null ? AttachmentMap.EMPTY : stack.getOrDefault(DataComponentRegistry.ATTACHMENT, AttachmentMap.EMPTY);
    }

    protected @Nullable HandOccupancy currentOccupancy(@NotNull LivingEntity owner) {
        ItemStack stack = this.currentItemStack;
        return stack == null ? null : GunItem.currentOccupancy(owner, stack);
    }

    /**
     * The item renderer is handed a stack and nothing else, so the only holder that can be identified is the local
     * player. Shots fired by anyone else fall back to the muzzle flash's own broadcast position.
     */
    protected @Nullable LivingEntity currentItemOwner() {
        AbstractClientPlayer player = Minecraft.getInstance().player;
        ItemStack stack = this.currentItemStack;
        if (player == null || stack == null) {
            return null;
        }
        return stack == player.getMainHandItem() || stack == player.getOffhandItem() ? player : null;
    }

    public static boolean isHandPerspective(@Nullable ItemDisplayContext perspective) {
        return perspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || perspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || perspective == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || perspective == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }

    public static boolean isFirstPersonPerspective(@Nullable ItemDisplayContext perspective) {
        return perspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || perspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }

    public static boolean isLeftHandPerspective(@Nullable ItemDisplayContext perspective) {
        return perspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || perspective == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }
}
