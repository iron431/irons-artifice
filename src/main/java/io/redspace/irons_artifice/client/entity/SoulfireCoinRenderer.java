package io.redspace.irons_artifice.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.irons_artifice.entity.SoulfireCoin;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SoulfireCoinRenderer extends EntityRenderer<SoulfireCoin, SoulfireCoinRenderer.SoulRenderState> {

    private final ItemModelResolver itemModelResolver;

    public SoulfireCoinRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    protected int getBlockLightLevel(SoulfireCoin entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    public void extractRenderState(SoulfireCoin entity, SoulRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        itemModelResolver.updateForTopItem(state.item, new ItemStack(ItemRegistry.SOULFIRE_COIN.get()), ItemDisplayContext.GROUND, entity.level(), null, 0);
    }

    @Override
    public void submit(SoulRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        AABB box = state.item.getModelBoundingBox();
        Vec3 centre = box.getCenter();
        poseStack.pushPose();
        poseStack.translate(0, box.getYsize() * 0.5f, 0);
        poseStack.mulPose(camera.orientation);
        poseStack.mulPose(Axis.XP.rotationDegrees(flipDegrees(state.ageInTicks, 20) + flipDegrees(state.ageInTicks, 50)));
        float wobble = Mth.sin(state.ageInTicks * 0.25) * 25f;
        poseStack.mulPose(Axis.YP.rotationDegrees(wobble));
        poseStack.translate(-centre.x, -centre.y, -centre.z);
        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    private static float flipDegrees(float ageInTicks, int duration) {
        float t = Mth.clamp(ageInTicks / duration, 0f, 1f);
        float eased = 1f - (1f - t) * (1f - t) * (1f - t);
        return 360f * eased;
    }

    @Override
    public SoulRenderState createRenderState() {
        return new SoulRenderState();
    }

    public static class SoulRenderState extends EntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
    }
}
