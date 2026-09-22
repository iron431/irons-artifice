package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ItemModelDataGenerator extends ItemModelProvider {
    public static final ResourceLocation GECKOLIB_GUN_DISPLAY = IronsArtifice.id("item/gun_display");
    public static final ResourceLocation REVOLVER_GUN_DISPLAY = IronsArtifice.id("item/pistol_display");

    public ItemModelDataGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, IronsArtifice.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (var holder : ItemRegistry.ITEMS.getEntries()) {
            String id = holder.getId().getPath();
            if (holder.get() instanceof GunItem) {
                ResourceLocation displayParent = GECKOLIB_GUN_DISPLAY;
                if (holder == ItemRegistry.BLACKPOWDER_REVOLVER || holder == ItemRegistry.SIX_SHOOTER) {
                    displayParent = REVOLVER_GUN_DISPLAY;
                }
                // Guns render through the GeckoLib item renderer; the model only carries display transforms.
                withExistingParent(id, displayParent);
            } else {
                withExistingParent(id, mcLoc("item/generated")).texture("layer0", modLoc("item/" + id));
            }
        }
    }
}
