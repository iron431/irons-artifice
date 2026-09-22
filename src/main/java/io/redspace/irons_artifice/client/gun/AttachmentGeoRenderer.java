package io.redspace.irons_artifice.client.gun;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtil;

public class AttachmentGeoRenderer extends GeoObjectRenderer<GeoAnimatable> {
    private final GeoAnimatable animatable = new StaticAttachment();

    public AttachmentGeoRenderer(GeoModel<GeoAnimatable> model) {
        super(model);
    }

    public void renderAttachment(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) {
        RenderType renderType = getRenderType(this.animatable, getTextureLocation(this.animatable), bufferSource, partialTick);
        if (renderType == null) {
            return;
        }
        render(poseStack, this.animatable, bufferSource, renderType, bufferSource.getBuffer(renderType), packedLight, partialTick);
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

        @Override
        public double getTick(Object object) {
            return RenderUtil.getCurrentTick();
        }
    }
}
