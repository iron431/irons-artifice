package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ItemModelDataGenerator extends ItemModelProvider {
    public static final ResourceLocation GECKOLIB_GUN_DISPLAY = IronsArtifice.id("item/gun_display");
    public static final ResourceLocation REVOLVER_GUN_DISPLAY = IronsArtifice.id("item/pistol_display");
    private static final ResourceLocation DEMO_GUN_MODEL = IronsArtifice.id("item/gun");

    public ItemModelDataGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, IronsArtifice.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (var item : ItemRegistry.ITEMS.getEntries()) {
            if (item.get() instanceof GunItem) {
                ResourceLocation displayParent = GECKOLIB_GUN_DISPLAY;
                if (item.getId().equals(ItemRegistry.BLACKPOWDER_REVOLVER.getId()) || item.getId().equals(ItemRegistry.SIX_SHOOTER.getId())) {
                    displayParent = REVOLVER_GUN_DISPLAY;
                }
                gunModel(item.getId(), displayParent);
            } else {
                generateTemplatedItem(item.getId(), itemTexture(item));
            }
        }
    }

    public ItemModelBuilder generateTemplatedItem(ResourceLocation item, ResourceLocation layer0Texture) {
        return getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", layer0Texture);
    }

    /**
     * A gun reaches its renderer through the display parent's {@code builtin/entity} root, which bakes the item to
     * a {@code BuiltInModel} and hands rendering to the item's {@code BlockEntityWithoutLevelRenderer}.
     */
    public ItemModelBuilder gunModel(ResourceLocation item, ResourceLocation displayParent) {
        return withExistingParent(item.toString(), displayParent);
    }

    private static ResourceLocation itemTexture(RegistryObject<?> item) {
        return itemTexture(item.getId());
    }

    private static ResourceLocation itemTexture(ResourceLocation identifier) {
        return identifier.withPrefix("item/");
    }
}
