package io.redspace.irons_artifice.client.entity.illificer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.irons_artifice.client.gun.GunArmPoses;
import io.redspace.irons_artifice.entity.Illificer;
import io.redspace.irons_artifice.item.FireDelayState;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public class IllificerModel extends IllagerModel<Illificer> {
    enum MobGunPose {
        NONE,
        IDLE,
        HUMANOID
    }

    protected final ModelPart rightArm;
    protected final ModelPart leftArm;
    /**
     * The arm poses want a {@link HumanoidModel}, which an illager model is not. The proxy owns no geometry: it is a
     * humanoid-shaped view over this model's parts, so posing it poses this model.
     */
    private final HumanoidModel<Illificer> humanoidProxy;
    private final float restRightArmX, restRightArmY, restRightArmZ;
    private final float restLeftArmX, restLeftArmY, restLeftArmZ;
    private MobGunPose mobGunPose = MobGunPose.NONE;
    private HumanoidArm mainArm = HumanoidArm.RIGHT;

    public IllificerModel(ModelPart root) {
        super(root);
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.restRightArmX = this.rightArm.x;
        this.restRightArmY = this.rightArm.y;
        this.restRightArmZ = this.rightArm.z;
        this.restLeftArmX = this.leftArm.x;
        this.restLeftArmY = this.leftArm.y;
        this.restLeftArmZ = this.leftArm.z;
        this.humanoidProxy = new HumanoidModel<>(humanoidView(root));
        this.getHat().visible = true;
    }

    /**
     * {@link HumanoidModel#setupAnim} reassigns both arms' x, y and z every frame, which is why a pose transformer
     * may offset them with {@code +=}. {@link IllagerModel#setupAnim} reassigns only the rotations, so without this
     * the offsets pile up frame on frame and the arms walk away from the body.
     */
    private void restoreArmOffsets() {
        this.rightArm.x = this.restRightArmX;
        this.rightArm.y = this.restRightArmY;
        this.rightArm.z = this.restRightArmZ;
        this.leftArm.x = this.restLeftArmX;
        this.leftArm.y = this.restLeftArmY;
        this.leftArm.z = this.restLeftArmZ;
    }

    private static ModelPart humanoidView(ModelPart root) {
        ModelPart head = root.getChild("head");
        return new ModelPart(List.of(), Map.of(
                "head", head,
                "hat", head.getChild("hat"),
                "body", root.getChild("body"),
                "right_arm", root.getChild("right_arm"),
                "left_arm", root.getChild("left_arm"),
                "right_leg", root.getChild("right_leg"),
                "left_leg", root.getChild("left_leg")
        ));
    }

    @Override
    public void setupAnim(Illificer entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        restoreArmOffsets();
        this.mainArm = entity.getMainArm();
        this.mobGunPose = MobGunPose.NONE;
        ItemStack weapon = entity.getWeaponItem();
        if (!(weapon.getItem() instanceof GunItem gun)) {
            return;
        }
        if (entity.isAggressive() || FireDelayState.isActive(entity, weapon) || GunItem.isReloading(weapon)) {
            this.mobGunPose = MobGunPose.HUMANOID;
            GunArmPoses.poseFor(gun).applyTransform(this.humanoidProxy, entity, this.mainArm);
        } else {
            this.mobGunPose = MobGunPose.IDLE;
            var arm = getArm(this.mainArm);
            arm.xRot *= 0.25f;
            arm.xRot -= Mth.PI / 6f;
        }
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        super.translateToHand(arm, poseStack);
        if (this.mobGunPose == MobGunPose.IDLE && arm == this.mainArm) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.translate(0,-1,0);
        }
    }

    protected ModelPart getArm(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
    }
}
