package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.entity.ChainEntity;
import io.redspace.irons_artifice.entity.Gunslinger;
import io.redspace.irons_artifice.entity.Illificer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, IronsArtifice.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<Bullet>> BULLET = ENTITY_TYPES.register("bullet",
            () -> EntityType.Builder.of(Bullet::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .build("bullet"));

    public static final DeferredHolder<EntityType<?>, EntityType<ChainEntity>> CHAIN = ENTITY_TYPES.register("chain",
            () -> EntityType.Builder.of(ChainEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(64).updateInterval(1)
                    .build("chain"));

    public static final DeferredHolder<EntityType<?>, EntityType<Gunslinger>> GUNSLINGER = ENTITY_TYPES.register("gunslinger",
            () -> EntityType.Builder.of(Gunslinger::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).clientTrackingRange(64)
                    .build("gunslinger"));

    public static final DeferredHolder<EntityType<?>, EntityType<Illificer>> ILLIFICER = ENTITY_TYPES.register("illificer",
            () -> EntityType.Builder.of(Illificer::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).clientTrackingRange(64)
                    .build("illificer"));

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
