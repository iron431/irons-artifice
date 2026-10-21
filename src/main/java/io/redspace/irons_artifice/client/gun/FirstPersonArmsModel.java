package io.redspace.irons_artifice.client.gun;

import io.redspace.irons_artifice.IronsArtifice;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.resources.PlayerSkin;

import javax.annotation.Nullable;

import java.util.Map;

public class FirstPersonArmsModel {
    public static final ModelLayerLocation WIDE_LAYER = new ModelLayerLocation(IronsArtifice.id("first_person_arms"), "wide");
    public static final ModelLayerLocation SLIM_LAYER = new ModelLayerLocation(IronsArtifice.id("first_person_arms"), "slim");
    private static final CubeDeformation SLEEVE = new CubeDeformation(0.25F);
    private static Map<PlayerSkin.Model, FirstPersonArmsModel> baked = Map.of();

    public final ModelPart rightArm;
    public final ModelPart leftArm;
    private final ModelPart rightSleeve;
    private final ModelPart leftSleeve;

    private FirstPersonArmsModel(ModelPart root) {
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightSleeve = rightArm.getChild("right_sleeve");
        this.leftSleeve = leftArm.getChild("left_sleeve");
    }

    public static LayerDefinition createLayer(boolean slim) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        float width = slim ? 3.0F : 4.0F;
        float rightX = slim ? -2.0F : -3.0F;
        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(rightX, -2.0F, -2.0F, width, 12.0F, 4.0F), PartPose.ZERO);
        rightArm.addOrReplaceChild("right_sleeve",
                CubeListBuilder.create().texOffs(40, 32).addBox(rightX, -2.0F, -2.0F, width, 12.0F, 4.0F, SLEEVE), PartPose.ZERO);
        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, width, 12.0F, 4.0F), PartPose.ZERO);
        leftArm.addOrReplaceChild("left_sleeve",
                CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, width, 12.0F, 4.0F, SLEEVE), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static void bake(EntityModelSet models) {
        baked = Map.of(
                PlayerSkin.Model.WIDE, new FirstPersonArmsModel(models.bakeLayer(WIDE_LAYER)),
                PlayerSkin.Model.SLIM, new FirstPersonArmsModel(models.bakeLayer(SLIM_LAYER))
        );
    }

    @Nullable
    public static FirstPersonArmsModel forSkin(PlayerSkin.Model type) {
        return baked.get(type);
    }

    public void showSleeves(boolean right, boolean left) {
        rightSleeve.visible = right;
        leftSleeve.visible = left;
    }
}
