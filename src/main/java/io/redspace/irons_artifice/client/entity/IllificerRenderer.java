package io.redspace.irons_artifice.client.entity;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.client.gun.GunArmPoses;
import io.redspace.irons_artifice.entity.Illificer;
import io.redspace.irons_artifice.gun.ArmPoseKind;
import io.redspace.irons_artifice.item.FireDelayState;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.illager.AbstractIllager;

public class IllificerRenderer extends IllagerRenderer<Illificer, IllificerRenderer.GunIllagerRenderState> {
    enum MobGunPose {
        NONE,
        IDLE,
        HUMANOID
    }

    public static class GunIllagerRenderState extends IllagerRenderState {
        MobGunPose mobGunPose;
        HumanoidModel.ArmPose humanoidPose;
    }

    private static final Identifier TEXTURE = IronsArtifice.id("textures/entity/illificer.png");

    public IllificerRenderer(EntityRendererProvider.Context context) {
        super(context, new IllificerModel(context.bakeLayer(ModelLayers.EVOKER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public Identifier getTextureLocation(GunIllagerRenderState state) {
        return TEXTURE;
    }

    @Override
    public GunIllagerRenderState createRenderState() {
        return new GunIllagerRenderState();
    }

    @Override
    public void extractRenderState(Illificer entity, GunIllagerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.mobGunPose = MobGunPose.NONE;
        state.humanoidPose = HumanoidModel.ArmPose.EMPTY;
        if (entity.getWeaponItem().getItem() instanceof GunItem gun) {
            state.armPose = AbstractIllager.IllagerArmPose.NEUTRAL;
            if (entity.isAggressive() || FireDelayState.isActive(entity.getWeaponItem()) || GunItem.isReloading(entity.getWeaponItem())) {
                state.mobGunPose = MobGunPose.HUMANOID;
                state.humanoidPose = gun.getGun().armPoseKind() == ArmPoseKind.PISTOL ? GunArmPoses.PISTOL.getValue() : GunArmPoses.RIFLE.getValue();
            } else {
                state.mobGunPose = MobGunPose.IDLE;
            }
        }
    }
}
