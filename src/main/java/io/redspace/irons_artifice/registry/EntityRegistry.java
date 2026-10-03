package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.entity.ChainEntity;
import io.redspace.irons_artifice.entity.Gunslinger;
import io.redspace.irons_artifice.entity.Illificer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @deprecated Use {@link IronsArtificeEntities}.
 */
@Deprecated
public final class EntityRegistry {
    private EntityRegistry() {
    }

    @Deprecated
    public static final DeferredRegister.Entities ENTITY_TYPES = IronsArtificeEntities.ENTITY_TYPES;

    @Deprecated
    public static final DeferredHolder<EntityType<?>, EntityType<Bullet>> BULLET = IronsArtificeEntities.BULLET;

    @Deprecated
    public static final DeferredHolder<EntityType<?>, EntityType<ChainEntity>> CHAIN = IronsArtificeEntities.CHAIN;

    @Deprecated
    public static final DeferredHolder<EntityType<?>, EntityType<Gunslinger>> GUNSLINGER = IronsArtificeEntities.GUNSLINGER;

    @Deprecated
    public static final DeferredHolder<EntityType<?>, EntityType<Illificer>> ILLIFICER = IronsArtificeEntities.ILLIFICER;
}
