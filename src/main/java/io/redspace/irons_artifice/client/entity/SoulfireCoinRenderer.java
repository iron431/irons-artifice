package io.redspace.irons_artifice.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.irons_artifice.entity.SoulfireCoin;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoulfireCoinRenderer extends EntityRenderer<SoulfireCoin> {

    private static final int VERTEX_STRIDE = 8;

    private final ItemRenderer itemRenderer;
    private final RandomSource random = RandomSource.create();

    public SoulfireCoinRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    protected int getBlockLightLevel(SoulfireCoin entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    public void render(SoulfireCoin entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        ItemStack stack = new ItemStack(ItemRegistry.SOULFIRE_COIN.get());
        BakedModel model = itemRenderer.getModel(stack, entity.level(), null, 0);
        AABB box = groundBoundingBox(model, random);
        Vec3 centre = box.getCenter();
        float ageInTicks = entity.tickCount + partialTick;
        poseStack.pushPose();
        poseStack.translate(0, box.getYsize() * 0.5f, 0);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.XP.rotationDegrees(flipDegrees(ageInTicks, 20) + flipDegrees(ageInTicks, 50)));
        float wobble = Mth.sin(ageInTicks * 0.25f) * 25f;
        poseStack.mulPose(Axis.YP.rotationDegrees(wobble));
        poseStack.translate(-centre.x, -centre.y, -centre.z);
        itemRenderer.render(stack, ItemDisplayContext.GROUND, false, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, model);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static float flipDegrees(float ageInTicks, int duration) {
        float t = Mth.clamp(ageInTicks / duration, 0f, 1f);
        float eased = 1f - (1f - t) * (1f - t) * (1f - t);
        return 360f * eased;
    }

    private static AABB groundBoundingBox(BakedModel model, RandomSource random) {
        PoseStack poseStack = new PoseStack();
        model.getTransforms().getTransform(ItemDisplayContext.GROUND).apply(false, poseStack);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        Matrix4f transform = poseStack.last().pose();

        List<Direction> faces = new ArrayList<>(Arrays.asList(Direction.values()));
        faces.add(null);
        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);
        Vector3f vertex = new Vector3f();
        for (Direction face : faces) {
            random.setSeed(42L);
            for (BakedQuad quad : model.getQuads(null, face, random)) {
                int[] vertices = quad.getVertices();
                for (int i = 0; i < vertices.length; i += VERTEX_STRIDE) {
                    vertex.set(Float.intBitsToFloat(vertices[i]), Float.intBitsToFloat(vertices[i + 1]), Float.intBitsToFloat(vertices[i + 2]));
                    transform.transformPosition(vertex);
                    min.min(vertex);
                    max.max(vertex);
                }
            }
        }
        if (min.x > max.x) {
            return AABB.ofSize(Vec3.ZERO, 0, 0, 0);
        }
        return new AABB(min.x, min.y, min.z, max.x, max.y, max.z);
    }

    @Override
    public ResourceLocation getTextureLocation(SoulfireCoin entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
