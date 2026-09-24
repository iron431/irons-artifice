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
 * GeckoLib 4 exposes the animated bones between the animation tick and the draw only through
 * {@link #setCustomAnimations}, so perspective silencing and the animation adjusters live on the model, and the
 * renderer-side state they need is copied onto the {@link AnimationState} first.
 */
public class GunGeoModel extends DefaultedItemGeoModel<GunItem> {

    public GunGeoModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    public void setCustomAnimations(GunItem animatable, long instanceId, AnimationState<GunItem> animationState) {
        ItemStack stack = animationState.getData(DataTickets.ITEMSTACK);
        if (stack == null) {
            return;
        }
        captureAnimationData(animatable, instanceId, stack, animationState);
        handlePerspectiveAdjustments(animationState);
        handleGunAdjustments(animatable, animationState);
    }

    protected void captureAnimationData(GunItem animatable, long instanceId, ItemStack stack, AnimationState<GunItem> animationState) {
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
        // A bone's rotation is its rest rotation plus the animation delta, so silencing it means restoring the
        // initial snapshot, not zeroing it.
        BoneSnapshot rest = bone.getInitialSnapshot();
        bone.updatePosition(rest.getOffsetX(), rest.getOffsetY(), rest.getOffsetZ());
        bone.updateRotation(rest.getRotX(), rest.getRotY(), rest.getRotZ());
        // Both setters mark the bone transformed, after the processor cleared those marks. Left set, they make
        // the next pass skip restoring the bone, and the silenced render leaks into the hand renders.
        bone.resetStateChanges();
    }

    protected void normalizeThirdPersonGunAnimations(ItemDisplayContext perspective) {
        // Root bone tends to contain large animations that only make sense in first person.
        // Cancel them out in third person viewers
        if (GunInHandRenderer.isFirstPersonPerspective(perspective)) {
            return;
        }
        getBone(GunBones.ROOT).ifPresent(this::silenceBone);
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
