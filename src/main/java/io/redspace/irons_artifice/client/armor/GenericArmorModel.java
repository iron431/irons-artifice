package io.redspace.irons_artifice.client.armor;

import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import io.redspace.irons_artifice.IronsArtifice;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class GenericArmorModel<T extends Item & GeoItem> extends DefaultedItemGeoModel<T> {

    private final ResourceLocation model;

    private final ResourceLocation texture;

    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(IronsArtifice.MODID, "animations/empty.animation.json");

    public GenericArmorModel(String modid, String name) {
        this(
                ResourceLocation.fromNamespaceAndPath(modid, String.format("geo/armor/%s.geo.json", name)),
                ResourceLocation.fromNamespaceAndPath(modid, String.format("textures/models/armor/%s.png", name))
        );
    }

    public GenericArmorModel(ResourceLocation model, ResourceLocation texture) {
        super(ResourceLocation.fromNamespaceAndPath(model.getNamespace(), ""));
        this.model = model;
        this.texture = texture;
    }

    public GenericArmorModel(String name) {
        this(IronsArtifice.MODID, name);
    }

    @Override
    public @NotNull ResourceLocation getModelResource(T animatable) {
        return model;
    }

    @Override
    public @NotNull ResourceLocation getTextureResource(T animatable) {
        return texture;
    }

    @Override
    public @NotNull ResourceLocation getAnimationResource(T animatable) {
        return ANIMATION;
    }
}
