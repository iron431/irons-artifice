package io.redspace.irons_artifice.client.gun;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.constant.DataTickets;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoObjectRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.jspecify.annotations.NonNull;

public class AttachmentGeoRenderer extends GeoObjectRenderer<GeoAnimatable, Void, GeoRenderState> {
    private final GeoAnimatable animatable = new StaticAttachment();

    public AttachmentGeoRenderer(GeoModel<GeoAnimatable> model) {
        super(model);
    }

    public void performRenderPass(@NonNull RenderPassInfo<?> parentPass, @NonNull SubmitNodeCollector renderTasks) {
        performRenderPass(
                this.animatable,
                null,
                parentPass.poseStack(),
                renderTasks,
                parentPass.cameraState(),
                parentPass.packedLight(),
                parentPass.getOrDefaultGeckolibData(DataTickets.PARTIAL_TICK, 0f)
        );
    }

    @Override
    public void adjustRenderPose(@NonNull RenderPassInfo<GeoRenderState> renderPassInfo) {
        return;
    }

    @Override
    public long getInstanceId(GeoAnimatable animatable, Void relatedObject) {
        return 0L;
    }

    private static class StaticAttachment implements GeoAnimatable {
        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return this.cache;
        }
    }
}
