package io.redspace.irons_artifice.client.entity;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.entity.Gunslinger;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.Identifier;

public class GunslingerRenderer extends IllagerRenderer<Gunslinger, IllagerRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(IronsArtifice.id("gunslinger"), "main");
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/illager/pillager.png");

    public GunslingerRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(LAYER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public Identifier getTextureLocation(IllagerRenderState state) {
        return TEXTURE;
    }

    @Override
    public IllagerRenderState createRenderState() {
        return new IllagerRenderState();
    }
}
