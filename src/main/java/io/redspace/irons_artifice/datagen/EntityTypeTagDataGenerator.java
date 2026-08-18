package io.redspace.irons_artifice.datagen;

import io.redspace.irons_artifice.registry.EntityRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.CompletableFuture;

public class EntityTypeTagDataGenerator extends IntrinsicHolderTagsProvider<EntityType<?>> {
    public EntityTypeTagDataGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ENTITY_TYPE, lookupProvider, entityType -> entityType.builtInRegistryHolder().key());
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(EntityTypeTags.RAIDERS).add(EntityRegistry.ILLIFICER.get());
        this.tag(EntityTypeTags.ILLAGER).add(EntityRegistry.ILLIFICER.get());
    }
}
