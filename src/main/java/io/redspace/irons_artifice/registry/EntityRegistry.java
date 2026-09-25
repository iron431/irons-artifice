package io.redspace.irons_artifice.registry;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.entity.ChainEntity;
import io.redspace.irons_artifice.entity.Gunslinger;
import io.redspace.irons_artifice.entity.Illificer;
import io.redspace.irons_artifice.entity.Soul;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EntityRegistry {
    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(IronsArtifice.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<Bullet>> BULLET = ENTITY_TYPES.registerEntityType(
            "bullet",
            Bullet::new,
            MobCategory.MISC,
            builder -> builder.sized(0.25f, 0.25f)
    );

    public static final DeferredHolder<EntityType<?>, EntityType<ChainEntity>> CHAIN = ENTITY_TYPES.registerEntityType(
            "chain",
            ChainEntity::new,
            MobCategory.MISC,
            builder -> builder.sized(0.5f, 0.5f).clientTrackingRange(64).updateInterval(1)
    );

    public static final DeferredHolder<EntityType<?>, EntityType<Soul>> SOUL = ENTITY_TYPES.registerEntityType(
            "soul",
            Soul::new,
            MobCategory.MISC,
            builder -> builder.sized(0.5f, 0.5f).clientTrackingRange(64).updateInterval(1)
    );

    public static final DeferredHolder<EntityType<?>, EntityType<Gunslinger>> GUNSLINGER = ENTITY_TYPES.registerEntityType(
            "gunslinger",
            Gunslinger::new,
            MobCategory.MONSTER,
            builder -> builder.sized(0.6f, 1.95f).clientTrackingRange(64)
    );

    public static final DeferredHolder<EntityType<?>, EntityType<Illificer>> ILLIFICER = ENTITY_TYPES.registerEntityType(
            "illificer",
            Illificer::new,
            MobCategory.MONSTER,
            builder -> builder.sized(0.6f, 1.95f).clientTrackingRange(64)
    );

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
