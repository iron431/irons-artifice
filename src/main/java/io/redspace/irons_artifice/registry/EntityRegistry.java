package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.entity.ChainEntity;
import io.redspace.irons_artifice.entity.Gunslinger;
import io.redspace.irons_artifice.entity.Illificer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @deprecated Use {@link IronsArtificeRegistries.Entities}.
 */
@Deprecated
public final class EntityRegistry {
    private EntityRegistry() {
    }

    @Deprecated
    public static final DeferredRegister.Entities ENTITY_TYPES = IronsArtificeRegistries.Entities.ENTITY_TYPES;

    @Deprecated
    public static final DeferredHolder<EntityType<?>, EntityType<Bullet>> BULLET = IronsArtificeRegistries.Entities.BULLET;

    @Deprecated
    public static final DeferredHolder<EntityType<?>, EntityType<ChainEntity>> CHAIN = IronsArtificeRegistries.Entities.CHAIN;

    @Deprecated
    public static final DeferredHolder<EntityType<?>, EntityType<Gunslinger>> GUNSLINGER = IronsArtificeRegistries.Entities.GUNSLINGER;

    @Deprecated
    public static final DeferredHolder<EntityType<?>, EntityType<Illificer>> ILLIFICER = IronsArtificeRegistries.Entities.ILLIFICER;
}
