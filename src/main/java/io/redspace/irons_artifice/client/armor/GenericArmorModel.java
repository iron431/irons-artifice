package io.redspace.irons_artifice.client.armor;

import io.redspace.irons_artifice.IronsArtifice;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public class GenericArmorModel<T extends Item & GeoItem> extends GeoModel<T> {

    private final ResourceLocation model;

    private final ResourceLocation texture;

    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(IronsArtifice.MODID, "empty");

    public GenericArmorModel(String modid, String name) {
        this(
                ResourceLocation.fromNamespaceAndPath(modid, String.format("geo/armor/%s.geo.json", name)),
                ResourceLocation.fromNamespaceAndPath(modid, String.format("textures/models/armor/%s.png", name))
        );
    }

    public GenericArmorModel(ResourceLocation model, ResourceLocation texture) {
        this.model = model;
        this.texture = texture;
    }

    public GenericArmorModel(String name) {
        this(IronsArtifice.MODID, name);
    }

    @Override
    public ResourceLocation getModelResource(T animatable, @Nullable GeoRenderer<T> renderer) {
        return model;
    }

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable, @Nullable GeoRenderer<T> renderer) {
        return texture;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return ANIMATION;
    }
}
