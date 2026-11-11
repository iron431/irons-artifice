package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.entity.ChainEntity;
import io.redspace.irons_artifice.entity.Gunslinger;
import io.redspace.irons_artifice.entity.Illificer;
import io.redspace.irons_artifice.entity.SoulfireCoin;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

/**
 * @deprecated Use {@link IronsArtificeEntities}.
 */
@Deprecated
public final class EntityRegistry {
    private EntityRegistry() {
    }

    @Deprecated
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = IronsArtificeEntities.ENTITY_TYPES;

    @Deprecated
    public static final RegistryObject<EntityType<Bullet>> BULLET = IronsArtificeEntities.BULLET;

    @Deprecated
    public static final RegistryObject<EntityType<ChainEntity>> CHAIN = IronsArtificeEntities.CHAIN;

    @Deprecated
    public static final RegistryObject<EntityType<SoulfireCoin>> SOUL = IronsArtificeEntities.SOUL;

    @Deprecated
    public static final RegistryObject<EntityType<Gunslinger>> GUNSLINGER = IronsArtificeEntities.GUNSLINGER;

    @Deprecated
    public static final RegistryObject<EntityType<Illificer>> ILLIFICER = IronsArtificeEntities.ILLIFICER;
}
