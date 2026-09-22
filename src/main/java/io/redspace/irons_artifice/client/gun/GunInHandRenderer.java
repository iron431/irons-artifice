package io.redspace.irons_artifice.client.gun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.client.MuzzleFlashEmitter;
import io.redspace.irons_artifice.client.RenderingEntityTracker;
import io.redspace.irons_artifice.data.HandOccupancy;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.item.ReloadState;
import io.redspace.irons_artifice.item.animation_adjuster.AnimationAdjuster;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GunInHandRenderer extends GeoItemRenderer<GunItem> {
    private static final Set<ItemDisplayContext> HAND_PERSPECTIVES = Set.of(
            ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
            ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, ItemDisplayContext.THIRD_PERSON_LEFT_HAND);

    private boolean adjustmentsApplied = false;

    public GunInHandRenderer(GeoModel<GunItem> model) {
        super(model);
    }

    @Override
    public void preRender(PoseStack poseStack, GunItem animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        this.adjustmentsApplied = false;
        if (this.renderPerspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || this.renderPerspective == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            poseStack.last().pose().scale(-1f, 1f, 1f);
            // compensate for weird lighting
            poseStack.last().normal().scale(-1, -1, 1);
        }
    }

    @Override
    public void renderRecursively(PoseStack poseStack, GunItem animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        if (!isReRender && !adjustmentsApplied) {
            adjustmentsApplied = true;
            applyPerspectiveAndGunAdjustments(animatable, partialTick);
        }
        if (!isReRender) {
            ItemStack stack = getCurrentItemStack();
            if (stack != null) {
                handleMuzzleFlashEmission(bone, poseStack);
                handleAttachmentRendering(bone, stack, poseStack, bufferSource, packedLight, partialTick);
                handleFirstPersonHandRendering(bone, stack, poseStack, bufferSource, packedLight);
            }
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    protected void applyPerspectiveAndGunAdjustments(GunItem animatable, float partialTick) {
        if (this.renderPerspective != null) {
            if (HAND_PERSPECTIVES.contains(this.renderPerspective)) {
                normalizeThirdPersonGunAnimations();
            } else {
                silenceAnimations();
            }
        }
        ItemStack stack = getCurrentItemStack();
        if (stack == null) {
            return;
        }
        List<AnimationAdjuster> adjusters = animatable.getGun().animationAdjusters();
        if (adjusters == null || adjusters.isEmpty()) {
            return;
        }
        MagazineContents magazine = MagazineContents.get(stack);
        ReloadState reload = ReloadState.get(stack);
        double reloadProgressSeconds = reload != null ? reload.progress() : 0;
        float reloadPercent = reload != null ? reload.percent(partialTick) : 0f;
        float muzzleOffset = (float) GunplayManager.compose(null, animatable.getGun(), stack).value(ShotComponents.MUZZLE_OFFSET);
        var context = new AnimationAdjuster.AdjustContext(magazine, reloadProgressSeconds, reloadPercent, muzzleOffset, getGeoModel());
        for (AnimationAdjuster adjuster : adjusters) {
            adjuster.adjust(context);
        }
    }

    protected void silenceAnimations() {
        // silence it all (for gui, ground, and other perspectives where animations shouldn't be visible)
        for (GeoBone bone : getGeoModel().getAnimationProcessor().getRegisteredBones()) {
            // allow hammers to still animate, so that we can see whether a gun is cocked or not on the ground
            if (!bone.getName().contains(GunBones.HAMMER)) {
                AnimationAdjuster.restoreInitial(bone);
            }
        }
    }

    protected void normalizeThirdPersonGunAnimations() {
        // Root bone tends to contain large animations that only make sense in first person.
        // Cancel them out in third person viewers
        if (isFirstPersonPerspective()) {
            return;
        }
        getGeoModel().getBone(GunBones.ROOT).ifPresent(AnimationAdjuster::restoreInitialTransform);
    }

    protected void handleMuzzleFlashEmission(GeoBone bone, PoseStack poseStack) {
        if (!bone.getName().equals(GunBones.SOCKET_MUZZLE) || !isHandPerspective()) {
            return;
        }
        // The flash belongs to the entity this gun is being rendered for (the upstream ITEM_OWNER_ID_TICKET).
        // Item renders without an owner (gui, ground) can only be the local player's own hands in hand perspectives.
        int ownerId = RenderingEntityTracker.currentEntityId().orElseGet(() -> {
            var player = Minecraft.getInstance().player;
            return player == null ? -1 : player.getId();
        });
        if (ownerId < 0) {
            return;
        }
        withBonePose(poseStack, bone, () -> MuzzleFlashEmitter.tryEmit(ownerId, poseStack));
    }

    protected void handleAttachmentRendering(GeoBone bone, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) {
        AttachmentMap attachments = stack.getOrDefault(DataComponentRegistry.ATTACHMENT.get(), AttachmentMap.EMPTY);
        if (attachments.isEmpty()) {
            return;
        }
        ResourceLocation rendererId = attachments.attachments().get(bone.getName());
        if (rendererId == null) {
            return;
        }
        Optional<AttachmentGeoRenderer> renderer = AttachmentRenderableRegistry.get(rendererId);
        if (renderer.isEmpty()) {
            return;
        }
        withBonePose(poseStack, bone, () -> renderer.get().renderAttachment(poseStack, bufferSource, packedLight, partialTick));
    }

    protected void handleFirstPersonHandRendering(GeoBone bone, ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Use Marker Bones "right_arm" and "left_arm" to render the player's hands in first person
        if (!isFirstPersonPerspective()) {
            return;
        }
        boolean right = bone.getName().equals(GunBones.RIGHT_ARM);
        boolean left = bone.getName().equals(GunBones.LEFT_ARM);
        if (!right && !left) {
            return;
        }
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer)) {
            return;
        }
        if (left && GunItem.currentOccupancy(player, stack) != HandOccupancy.BOTH) {
            return;
        }
        ModelPart modelPart = right ? renderer.getModel().rightArm : renderer.getModel().leftArm;
        RenderType renderType = RenderType.entityCutoutNoCull(player.getSkin().texture());
        withBonePose(poseStack, bone, () -> renderFirstPersonHand(modelPart, poseStack, renderType, bufferSource, packedLight));
    }

    /** Runs {@code action} with the pose stack transformed into {@code bone}'s local space. */
    protected void withBonePose(PoseStack poseStack, GeoBone bone, Runnable action) {
        poseStack.pushPose();
        RenderUtil.prepMatrixForBone(poseStack, bone);
        action.run();
        poseStack.popPose();
    }

    protected void renderFirstPersonHand(ModelPart modelPart, PoseStack poseStack, RenderType renderType, MultiBufferSource bufferSource, int packedLight) {
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

    protected boolean isHandPerspective() {
        return HAND_PERSPECTIVES.contains(this.renderPerspective);
    }

    protected boolean isFirstPersonPerspective() {
        return this.renderPerspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || this.renderPerspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }
}
