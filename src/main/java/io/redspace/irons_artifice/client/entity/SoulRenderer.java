package io.redspace.irons_artifice.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.entity.Soul;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class SoulRenderer extends EntityRenderer<Soul, EntityRenderState> {
    private static final Identifier TEXTURE = IronsArtifice.id("textures/entity/soul.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutout(TEXTURE);
    private static final float SIZE = 0.5f;

    public SoulRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(Soul entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0, SIZE * 0.5f, 0);
        poseStack.scale(SIZE, SIZE, SIZE);
        poseStack.mulPose(camera.orientation);
        submitNodeCollector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, buffer) -> {
            vertex(buffer, pose, state.lightCoords, 0, 0, 0, 1);
            vertex(buffer, pose, state.lightCoords, 1, 0, 1, 1);
            vertex(buffer, pose, state.lightCoords, 1, 1, 1, 0);
            vertex(buffer, pose, state.lightCoords, 0, 1, 0, 0);
        });
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, int lightCoords, float x, float y, int u, int v) {
        buffer.addVertex(pose, x - 0.5f, y - 0.5f, 0)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(pose, 0, 1, 0);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
