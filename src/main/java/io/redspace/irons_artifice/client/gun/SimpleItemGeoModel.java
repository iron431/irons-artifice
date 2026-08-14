package io.redspace.irons_artifice.client.gun;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class SimpleItemGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
    final Identifier modelResource;
    final Identifier textureResource;
    final Identifier animationResource;

    public SimpleItemGeoModel(String modid, String modelResource, String textureResource, String animationResource) {
        this.modelResource = Identifier.fromNamespaceAndPath(modid, "item/" + modelResource);
        this.animationResource = Identifier.fromNamespaceAndPath(modid, "item/" + animationResource);
        this.textureResource = Identifier.fromNamespaceAndPath(modid, "textures/item/" + textureResource + ".png");
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return modelResource;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return textureResource;
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return animationResource;
    }
}
