package io.redspace.irons_artifice.client.gun;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public class SimpleItemGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
    final ResourceLocation modelResource;
    final ResourceLocation textureResource;
    final ResourceLocation animationResource;

    public SimpleItemGeoModel(String modid, String name) {
        this.modelResource = ResourceLocation.fromNamespaceAndPath(modid, "geo/item/" + name + ".geo.json");
        this.animationResource = ResourceLocation.fromNamespaceAndPath(modid, "animations/item/" + name + ".animation.json");
        this.textureResource = ResourceLocation.fromNamespaceAndPath(modid, "textures/item/model/" + name + ".png");
    }

    @Override
    public ResourceLocation getModelResource(T animatable, @Nullable GeoRenderer<T> renderer) {
        return modelResource;
    }

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable, @Nullable GeoRenderer<T> renderer) {
        return textureResource;
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
