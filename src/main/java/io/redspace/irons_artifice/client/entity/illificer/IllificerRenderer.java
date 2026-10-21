package io.redspace.irons_artifice.client.entity.illificer;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.entity.Illificer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class IllificerRenderer extends IllagerRenderer<Illificer> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(IronsArtifice.id("illificer"), "main");
    private static final ResourceLocation TEXTURE = IronsArtifice.id("textures/entity/illificer.png");

    public IllificerRenderer(EntityRendererProvider.Context context) {
        super(context, new IllificerModel(context.bakeLayer(LAYER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(Illificer entity) {
        return TEXTURE;
    }
}
