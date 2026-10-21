package io.redspace.irons_artifice.client.entity;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.entity.Gunslinger;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class GunslingerRenderer extends IllagerRenderer<Gunslinger> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(IronsArtifice.id("gunslinger"), "main");
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/illager/pillager.png");

    public GunslingerRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(LAYER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(Gunslinger entity) {
        return TEXTURE;
    }
}
