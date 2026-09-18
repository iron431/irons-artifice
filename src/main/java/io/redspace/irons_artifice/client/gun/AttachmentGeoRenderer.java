package io.redspace.irons_artifice.client.gun;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AttachmentGeoRenderer extends GeoObjectRenderer<GeoAnimatable> {
    private final GeoAnimatable attachment = new StaticAttachment();

    public AttachmentGeoRenderer(GeoModel<GeoAnimatable> model) {
        super(model);
    }

    public void renderAttachment(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, float partialTick) {
        render(poseStack, this.attachment, bufferSource, null, null, packedLight, partialTick);
    }

    @Override
    public void preRender(@NotNull PoseStack poseStack, @NotNull GeoAnimatable animatable, @NotNull BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        // The attachment is drawn straight onto the socket bone's pose, so none of GeoObjectRenderer's default
        // block-centering translation applies here.
        this.objectRenderTranslations = new Matrix4f(poseStack.last().pose());
    }

    @Override
    public long getInstanceId(GeoAnimatable animatable) {
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

        @Override
        public double getTick(Object object) {
            return RenderUtil.getCurrentTick();
        }
    }
}
