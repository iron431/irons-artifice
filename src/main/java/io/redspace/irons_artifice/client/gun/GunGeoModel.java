package io.redspace.irons_artifice.client.gun;

import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import io.redspace.irons_artifice.api.GunAnimations;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.item.ReloadState;
import io.redspace.irons_artifice.item.animation_adjuster.AnimationAdjuster;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Bone adjustments have to land after the animation processor has ticked and before the model is drawn, which is
 * exactly what {@link #setCustomAnimations} is for. Everything the adjusters read is put on the
 * {@link AnimationState} here, since that is the only object that reaches them.
 */
public class GunGeoModel extends DefaultedItemGeoModel<GunItem> {

    public GunGeoModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    public void setCustomAnimations(GunItem animatable, long instanceId, AnimationState<GunItem> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        captureAnimationData(animatable, instanceId, animationState);
        handlePerspectiveAdjustments(animationState);
        handleGunAdjustments(animatable, animationState);
    }

    protected void captureAnimationData(GunItem animatable, long instanceId, AnimationState<GunItem> animationState) {
        ItemStack stack = animationState.getData(DataTickets.ITEMSTACK);
        if (stack == null) {
            return;
        }
        if (MagazineContents.has(stack)) {
            animationState.setData(GunItem.MAGAZINE_ANIMATION_TICKET, MagazineContents.get(stack));
        }
        AnimationController<?> controller = animatable.getAnimatableInstanceCache().getManagerForId(instanceId)
                .getAnimationControllers().get(GunItem.TRIGGERED_ANIMATION_CONTROLLER);
        double reloadProgress = controller instanceof GunItem.OffsetableAnimationController<?> actionController
                && actionController.isTriggeredAnimation(GunAnimations.RELOAD)
                ? actionController.getCurrentAnimationTime()
                : 0.0;
        animationState.setData(GunItem.RELOAD_PROGRESS_SECONDS_TICKET, reloadProgress);
        ReloadState reload = ReloadState.get(stack);
        animationState.setData(GunItem.RELOAD_PERCENT_TICKET, reload != null ? reload.percent(animationState.getPartialTick()) : 0f);
        animationState.setData(
                GunItem.MUZZLE_OFFSET_TICKET,
                (float) GunplayManager.compose(null, animatable.getGun(), stack).value(ShotComponents.MUZZLE_OFFSET)
        );
    }

    protected void handlePerspectiveAdjustments(AnimationState<GunItem> animationState) {
        ItemDisplayContext perspective = animationState.getData(DataTickets.ITEM_RENDER_PERSPECTIVE);
        if (perspective == null) {
            return;
        }
        if (GunInHandRenderer.isHandPerspective(perspective)) {
            normalizeThirdPersonGunAnimations(perspective);
        } else {
            silenceAnimations();
        }
    }

    protected void silenceAnimations() {
        // silence it all (for gui, ground, and other perspectives where animations shouldn't be visible)
        for (GeoBone bone : getAnimationProcessor().getRegisteredBones()) {
            // allow hammers to still animate, so that we can see whether a gun is cocked or not on the ground
            if (!bone.getName().contains(GunBones.HAMMER)) {
                silenceBone(bone);
            }
        }
    }

    protected void silenceBone(GeoBone bone) {
        BoneSnapshot rest = bone.getInitialSnapshot();
        bone.updatePosition(rest.getOffsetX(), rest.getOffsetY(), rest.getOffsetZ());
        bone.updateRotation(rest.getRotX(), rest.getRotY(), rest.getRotZ());
        bone.updateScale(rest.getScaleX(), rest.getScaleY(), rest.getScaleZ());
        // The bones are shared model-wide; leaving the changed markers set would make the next pass skip their reset
        bone.resetStateChanges();
    }

    protected void normalizeThirdPersonGunAnimations(ItemDisplayContext perspective) {
        // Root bone tends to contain large animations that only make sense in first person.
        // Cancel them out in third person viewers
        if (GunInHandRenderer.isFirstPersonPerspective(perspective)) {
            return;
        }
        getBone(GunBones.ROOT).ifPresent(bone -> {
            BoneSnapshot rest = bone.getInitialSnapshot();
            bone.updatePosition(rest.getOffsetX(), rest.getOffsetY(), rest.getOffsetZ());
            bone.updateRotation(rest.getRotX(), rest.getRotY(), rest.getRotZ());
            bone.resetStateChanges();
        });
    }

    protected void handleGunAdjustments(GunItem animatable, AnimationState<GunItem> animationState) {
        List<AnimationAdjuster> adjusters = animatable.getGun().animationAdjusters();
        if (adjusters == null || adjusters.isEmpty()) {
            return;
        }
        for (AnimationAdjuster adjuster : adjusters) {
            adjuster.adjust(animationState, this);
        }
    }
}
