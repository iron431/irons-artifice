package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.utils.IronsArtificeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class BlockTagDataGenerator extends IntrinsicHolderTagsProvider<Block> {
    public BlockTagDataGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, Registries.BLOCK, lookupProvider, block -> block.builtInRegistryHolder().key(), IronsArtifice.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(IronsArtificeTags.ALWAYS_BREAK)
                .addTag(Tags.Blocks.GLASS_BLOCKS)
                .addTag(Tags.Blocks.GLASS_PANES)
                .add(Blocks.DECORATED_POT)
        ;
        this.tag(IronsArtificeTags.NEVER_BREAK)
                .add(Blocks.TARGET)
        ;
    }
}
