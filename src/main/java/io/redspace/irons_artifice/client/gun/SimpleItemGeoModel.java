package io.redspace.irons_artifice.client.gun;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;

public class SimpleItemGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
    final ResourceLocation modelResource;
    final ResourceLocation textureResource;
    final ResourceLocation animationResource;

    public SimpleItemGeoModel(String modid, String modelResource, String textureResource, String animationResource) {
        this.modelResource = ResourceLocation.fromNamespaceAndPath(modid, "geo/item/" + modelResource + ".geo.json");
        this.animationResource = ResourceLocation.fromNamespaceAndPath(modid, "animations/item/" + animationResource + ".animation.json");
        this.textureResource = ResourceLocation.fromNamespaceAndPath(modid, "textures/item/" + textureResource + ".png");
    }

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return animationResource;
    }
}
