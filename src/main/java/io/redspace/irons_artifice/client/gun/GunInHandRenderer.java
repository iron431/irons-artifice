package io.redspace.irons_artifice.client.gun;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.data.HandOccupancy;
import io.redspace.irons_artifice.item.AnimationAdjuster;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix3f;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GunInHandRenderer extends GeoItemRenderer<GunItem> {
    public static final DataTicket<MagazineContents> MAGAZINE_ANIMATION_TICKET = DataTicket.create(IronsArtifice.id("magazine_state").toString(), MagazineContents.class);

    public GunInHandRenderer(GeoModel<GunItem> model) {
        super(model);

    }

    @Override
    public void preRenderPass(@NonNull RenderPassInfo<GeoRenderState> renderPassInfo, @NonNull SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);
        handleAttachmentRendering(renderPassInfo, renderTasks);
        handleFirstPersonHandRendering(renderPassInfo, renderTasks);
    }

    private void handleAttachmentRendering(@NonNull RenderPassInfo<GeoRenderState> renderPassInfo, @NonNull SubmitNodeCollector renderTasks) {
        AttachmentMap attachments = renderPassInfo.getGeckolibData(GunItem.ATTACHMENTS);
        if (attachments == null || attachments.isEmpty()) {
            return;
        }
        for (var entry : attachments.attachments().entrySet()) {
            Optional<AttachmentGeoRenderer> renderer = AttachmentRenderableRegistry.get(entry.getValue());
            if (renderer.isEmpty()) {
                continue;
            }
            Optional<GeoBone> attachmentOpt = renderPassInfo.model().getBone(entry.getKey());
            if (attachmentOpt.isEmpty()) {
                continue;
            }
            renderPassInfo.addPerBoneRender(attachmentOpt.get(), (opticPass, bone, opticTasks) ->
                    renderer.get().performRenderPass(opticPass, opticTasks)
            );
        }
    }

    private void handleFirstPersonHandRendering(@NonNull RenderPassInfo<GeoRenderState> renderPassInfo, @NonNull SubmitNodeCollector renderTasks) {
        // Use Marker Bones "right_arm" and "left_arm" to render the player's hands in first person
        if (!isFirstPersonPerspective(renderPassInfo.renderState())) {
            return;
        }

        final GeoRenderState renderState = renderPassInfo.renderState();
        AbstractClientPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player) instanceof AvatarRenderer renderer)) {
            return;
        }
        Identifier skinTexture = player.getSkin().body().texturePath();
        final RenderType renderType = getRenderType(renderState, skinTexture);
        renderPassInfo.model().getBone("right_arm").ifPresent(bone ->
                renderPassInfo.addPerBoneRender(bone, (renderPassInfo1, bone1, renderTasks1) -> {
                            var modelPart = ((PlayerModel) renderer.getModel()).rightArm;
                            renderFirstPersonHand(renderTasks, renderType, modelPart, renderPassInfo.poseStack().last(), renderPassInfo);
                        }
                )
        );
        HandOccupancy occupancy = renderPassInfo.getOrDefaultGeckolibData(GunItem.HAND_OCCUPANCY_TICKET, HandOccupancy.BOTH);
        if (occupancy == HandOccupancy.BOTH) {
            renderPassInfo.model().getBone("left_arm").ifPresent(bone ->
                    renderPassInfo.addPerBoneRender(bone, (renderPassInfo1, bone1, renderTasks1) -> {
                                var modelPart = ((PlayerModel) renderer.getModel()).leftArm;
                                renderFirstPersonHand(renderTasks, renderType, modelPart, renderPassInfo.poseStack().last(), renderPassInfo);
                            }
                    )
            );
        }
    }

    @Override
    @SuppressWarnings("all")
    public void captureDefaultRenderState(GunItem animatable, RenderData renderData, GeoRenderState renderState, float partialTick) {
        super.captureDefaultRenderState(animatable, renderData, renderState, partialTick);
        if (MagazineContents.has(renderData.itemStack())) {
            renderState.addGeckolibData(GunItem.MAGAZINE_ANIMATION_TICKET, MagazineContents.get(renderData.itemStack()));
        }
        var controller = animatable.getAnimatableInstanceCache().getManagerForId(GeoItem.getId(renderData.itemStack())).getAnimationControllers().get(GunItem.TRIGGERED_ANIMATION_CONTROLLER);
        renderState.addGeckolibData(GunItem.RELOAD_PROGRESS_SECONDS_TICKET, controller.isTriggeredAnimation("reload") ? controller.getCurrentAnimationTime() : 0.0);
        renderState.addGeckolibData(GunItem.ANIMATION_ADJUSTER_TICKET, animatable.getGun().animationAdjuster());
        renderState.addGeckolibData(
                GunItem.ATTACHMENTS,
                renderData.itemStack().getOrDefault(DataComponentRegistry.ATTACHMENT, AttachmentMap.EMPTY)
        );LivingEntity owner = renderData.itemOwner() instanceof LivingEntity living
                ? living
                : Minecraft.getInstance().player;
        HandOccupancy occupancy = owner != null
                ? GunItem.currentOccupancy(owner, renderData.itemStack())
                : animatable.occupancyForCurrentAnimation(renderData.itemStack());
        renderState.addGeckolibData(GunItem.HAND_OCCUPANCY_TICKET, occupancy);
    }

    @Override
    public void adjustRenderPose(@NonNull RenderPassInfo<GeoRenderState> renderPassInfo) {
        super.adjustRenderPose(renderPassInfo);
        var perspective = renderPassInfo.renderState().getGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE);
        if (perspective != null && perspective.leftHand()) {
            PoseStack.Pose last = renderPassInfo.poseStack().last();
            last.pose().scale(-1f, 1f, 1f);
            // compensate for weird lighting
            Matrix3f normal = last.normal();
            normal.scale(-1, -1, 1);
        }
    }

    @Override
    public void adjustModelBonesForRender(@NonNull RenderPassInfo<GeoRenderState> renderPassInfo, @NonNull BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        handlePerspectiveAdjustments(renderPassInfo, snapshots);
        handleGunAdjustments(renderPassInfo, snapshots);
    }

    private void handlePerspectiveAdjustments(@NonNull RenderPassInfo<GeoRenderState> renderPassInfo, @NonNull BoneSnapshots snapshots) {
        var perspective = renderPassInfo.renderState().getGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE);
        if (perspective == null) {
            return;
        }
        Set<ItemDisplayContext> allowed = Set.of(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        if (allowed.contains(perspective)) {
            normalizeThirdPersonGunAnimations(renderPassInfo, snapshots);
        } else {
            silenceAnimations(renderPassInfo, snapshots);
        }
    }

    private void silenceAnimations(@NonNull RenderPassInfo<GeoRenderState> renderPassInfo, @NonNull BoneSnapshots snapshots) {
        // silence it all (for gui, ground, and other perspectives where animations shouldn't be visible)
        List<String> bones = collectBones(renderPassInfo.model().topLevelBones(), new ArrayList<>());
        bones.forEach(boneName -> {
            if (!boneName.contains("hammer")) {
                silenceBone(boneName, snapshots);
            }
        });
    }

    private List<String> collectBones(GeoBone[] bones, List<String> collector) {
        for (GeoBone bone : bones) {
            if (bone == null) continue;
            collector.add(bone.name());
            collectBones(bone.children(), collector);
        }
        return collector;
    }

    private void silenceBone(String name, BoneSnapshots snapshots) {
        Optional<BoneSnapshot> opt = snapshots.get(name);
        if (opt.isEmpty()) {
            return;
        }
        BoneSnapshot root = opt.get();
        root.setTranslation(0, 0, 0);
        root.setRotation(0, 0, 0);
    }

    private void handleGunAdjustments(@NonNull RenderPassInfo<GeoRenderState> renderPassInfo, @NonNull BoneSnapshots snapshots) {
        AnimationAdjuster adjuster = renderPassInfo.getGeckolibData(GunItem.ANIMATION_ADJUSTER_TICKET);
        if (adjuster == null || adjuster == AnimationAdjuster.NONE) {
            return;
        }
        // todo: this really shouldnt be an enum. requires hardcode and is non-addon-extensible
        switch (adjuster) {
            case HARMONICA_MAGAZINE -> {
                double reloadProgress = renderPassInfo.getOrDefaultGeckolibData(GunItem.RELOAD_PROGRESS_SECONDS_TICKET, 0.0);
                MagazineContents magazineContents = renderPassInfo.getGeckolibData(GunItem.MAGAZINE_ANIMATION_TICKET);
                Optional<BoneSnapshot> magazineOpt = snapshots.get("magazine");
                if (magazineOpt.isEmpty() || magazineContents == null) {
                    return;
                }
                boolean ignoreForReload = reloadProgress > 0.42;
                if (!ignoreForReload) {
                    float percent = 1 - magazineContents.count() / 10f;
                    BoneSnapshot magazine = magazineOpt.get();
                    magazine.setTranslation(4 * percent, 0, 0);
                }
            }
            case LOWER_HAMMER -> {
                MagazineContents magazineContents = renderPassInfo.getGeckolibData(GunItem.MAGAZINE_ANIMATION_TICKET);
                double reloadProgress = renderPassInfo.getOrDefaultGeckolibData(GunItem.RELOAD_PROGRESS_SECONDS_TICKET, 0.0);
                Optional<BoneSnapshot> boneOpt = snapshots.get("hammer");
                if (boneOpt.isEmpty() || magazineContents == null) {
                    return;
                } else if (magazineContents.isEmpty() && reloadProgress <= 0) {
                    boneOpt.get().setRotation(0, 0, 0);
                }
            }
            case DOUBLE_BARREL_HAMMER -> {
                MagazineContents magazineContents = renderPassInfo.getGeckolibData(GunItem.MAGAZINE_ANIMATION_TICKET);
                double reloadProgress = renderPassInfo.getOrDefaultGeckolibData(GunItem.RELOAD_PROGRESS_SECONDS_TICKET, 0.0);
                Optional<BoneSnapshot> leftOpt = snapshots.get("hammer_left");
                Optional<BoneSnapshot> rightOpt = snapshots.get("hammer_right");
                if (leftOpt.isEmpty() || rightOpt.isEmpty() || magazineContents == null) {
                    return;
                } else if (reloadProgress <= 1.17) {
                    if (magazineContents.count() <= 1) {
                        leftOpt.get().setRotation(0, 0, 0);
                    }
                    if (magazineContents.isEmpty()) {
                        rightOpt.get().setRotation(0, 0, 0);
                    }
                }
            }
        }
    }

    private void normalizeThirdPersonGunAnimations(@NonNull RenderPassInfo<GeoRenderState> renderPassInfo, @NonNull BoneSnapshots snapshots) {
        // Root bone tends to contain large animations that only make sense in first person.
        // Cancel them out in third person viewers
        if (isFirstPersonPerspective(renderPassInfo.renderState())) {
            return;
        }
        Optional<BoneSnapshot> rootOpt = snapshots.get("root");
        if (rootOpt.isEmpty()) {
            return;
        }
        BoneSnapshot root = rootOpt.get();
        root.setTranslation(0, 0, 0);
        root.setRotation(0, 0, 0);
    }

    private void renderFirstPersonHand(SubmitNodeCollector renderTasks, RenderType renderType, ModelPart modelPart, PoseStack.Pose pose, RenderPassInfo<GeoRenderState> renderPassInfo) {
        modelPart.x = 0;
        modelPart.y = 0;
        modelPart.z = 0;
        modelPart.xRot = 0;
        modelPart.yRot = 0;
        modelPart.zRot = 0;
        final PoseStack poseStack = new PoseStack();
        poseStack.last().set(pose);
        poseStack.scale(-1, -1, 1);
        // no clue where these numbers come from (manually lined up from block bench)
        poseStack.translate(1 / 16f, -10 / 16f, 0 / 16f);
        renderTasks.submitModelPart(
                modelPart,
                poseStack,
                renderType,
                renderPassInfo.packedLight(),
                OverlayTexture.NO_OVERLAY,
                null
        );
    }

    private boolean isFirstPersonPerspective(GeoRenderState renderState) {
        var perspective = renderState.getGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE);
        return perspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || perspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }
}

