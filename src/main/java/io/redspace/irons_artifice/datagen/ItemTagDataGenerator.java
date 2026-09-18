package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.modifier.ModifierItem;
import io.redspace.irons_artifice.registry.ItemRegistry;
import io.redspace.irons_artifice.utils.IronsArtificeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ItemTagDataGenerator extends IntrinsicHolderTagsProvider<Item> {
    public ItemTagDataGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, Registries.ITEM, lookupProvider, item -> item.builtInRegistryHolder().key(), IronsArtifice.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(ItemTags.HEAD_ARMOR)
                .add(ItemRegistry.COWBOY_HAT.get())
                .add(ItemRegistry.TRICORNE_HAT.get())
        ;

        var guns = this.tag(IronsArtificeTags.GUNS);
        for (var holder : ItemRegistry.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof GunItem) {
                guns.add(item);
            }
        }
        this.tag(IronsArtificeTags.AMMO).add(ItemRegistry.BULLET.get());
    }
}
