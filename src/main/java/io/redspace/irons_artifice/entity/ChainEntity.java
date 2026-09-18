package io.redspace.irons_artifice.entity;

import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ChainEntity extends Entity {
    public static final float SPAWN_RANGE = 8f;
    public static final float BREAK_RANGE = 16f;
    public static final int DURATION = 200;
    public static final float STRENGTH = 0.05f;
    public static final int VISUAL_WARMUP_TIME = 5;

    public void setMaxRange(float maxRange) {
        this.maxRange = maxRange;
    }

    public void setPrimaryStrength(float primaryStrength) {
        this.primaryStrength = primaryStrength;
    }

    public void setSecondaryStrength(float secondaryStrength) {
        this.secondaryStrength = secondaryStrength;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    private float maxRange, primaryStrength, secondaryStrength;
    private int duration;

    private static final EntityDataAccessor<Integer> DATA_FIRST_ID =
            SynchedEntityData.defineId(ChainEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SECOND_ID =
            SynchedEntityData.defineId(ChainEntity.class, EntityDataSerializers.INT);

    @Nullable
    private UUID firstUuid;
    @Nullable
    private UUID secondUuid;

    public int warmup;

    public ChainEntity(EntityType<? extends ChainEntity> type, Level level) {
        super(type, level);
        this.maxRange = BREAK_RANGE;
        this.primaryStrength = STRENGTH;
        this.secondaryStrength = STRENGTH;
        this.duration = DURATION;
        this.noPhysics = true;
    }

    public ChainEntity(Level level, LivingEntity first, LivingEntity second) {
        this(EntityRegistry.CHAIN.get(), level);
        setFirst(first);
        setSecond(second);
        setPos(midpoint(first, second));
    }

    public void setFirst(LivingEntity entity) {
        this.firstUuid = entity.getUUID();
        this.entityData.set(DATA_FIRST_ID, entity.getId());
    }

    public void setSecond(LivingEntity entity) {
        this.secondUuid = entity.getUUID();
        this.entityData.set(DATA_SECOND_ID, entity.getId());
    }

    @Nullable
    public LivingEntity getFirst() {
        return resolveBound(entityData.get(DATA_FIRST_ID), firstUuid);
    }

    @Nullable
    public LivingEntity getSecond() {
        return resolveBound(entityData.get(DATA_SECOND_ID), secondUuid);
    }

    @Nullable
    private LivingEntity resolveBound(int entityId, @Nullable UUID uuid) {
        if (entityId != 0) {
            Entity entity = level().getEntity(entityId);
            if (entity instanceof LivingEntity living && !living.isRemoved()) {
                return living;
            }
        }
        return resolveByUuid(uuid);
    }

    @Nullable
    private LivingEntity resolveByUuid(@Nullable UUID uuid) {
        if (uuid != null && level() instanceof ServerLevel serverLevel
                && serverLevel.getEntity(uuid) instanceof LivingEntity living && !living.isRemoved()) {
            return living;
        }
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if (warmup < VISUAL_WARMUP_TIME) {
            warmup++;
        }

        LivingEntity first = getFirst();
        LivingEntity second = getSecond();

        if (first == null || second == null) {
            if (!level().isClientSide()) {
                discard();
            }
            return;
        }

        setPos(midpoint(first, second));
        if (!level().isClientSide()) {
            Vec3 firstCenter = first.getBoundingBox().getCenter();
            Vec3 secondCenter = second.getBoundingBox().getCenter();
            double distSq = firstCenter.distanceToSqr(secondCenter);
            if (tickCount > duration || distSq > maxRange * maxRange) {
                breakWithEffects(firstCenter, secondCenter);
                return;
            }
            if (primaryStrength != 0) {
                applySpring(first, position(), primaryStrength);
            }
            if (secondaryStrength != 0) {
                applySpring(second, position(), secondaryStrength);
            }
        }
    }

    private void applySpring(LivingEntity entity, Vec3 center, float strength) {
        Vec3 entityCenter = entity.getBoundingBox().getCenter();
        Vec3 delta = center.subtract(entityCenter);
        if (delta.lengthSqr() < 1.0E-8) {
            return;
        }
        entity.setDeltaMovement(entity.getDeltaMovement().add(delta.multiply(Math.abs(delta.x), Math.abs(delta.y), Math.abs(delta.z)).scale(strength)));
        entity.hurtMarked = true;
        if (entity.getDeltaMovement().y >= 0) {
            entity.resetFallDistance();
        }
    }

    private void breakWithEffects(Vec3 from, Vec3 to) {
        playSound(SoundEvents.CHAIN_BREAK, 1f, 1f);
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CHAIN.defaultBlockState());
        int count = 12;
        for (int i = 0; i < count; i++) {
            Vec3 pos = from.lerp(to, i / (float) (count - 1));
            Utils.spawnParticles(level(), particle, pos.x, pos.y, pos.z, 2, 0.1, 0.1, 0.1, 0.02, false);
        }
        discard();
    }

    private static Vec3 midpoint(LivingEntity a, LivingEntity b) {
        return a.getBoundingBox().getCenter().add(b.getBoundingBox().getCenter()).scale(0.5);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_FIRST_ID, 0);
        builder.define(DATA_SECOND_ID, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.firstUuid = tag.hasUUID("First") ? tag.getUUID("First") : null;
        this.secondUuid = tag.hasUUID("Second") ? tag.getUUID("Second") : null;
        this.tickCount = tag.getInt("Age");
        if (level() instanceof ServerLevel) {
            LivingEntity first = resolveByUuid(firstUuid);
            if (first != null) {
                entityData.set(DATA_FIRST_ID, first.getId());
            }
            LivingEntity second = resolveByUuid(secondUuid);
            if (second != null) {
                entityData.set(DATA_SECOND_ID, second.getId());
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (firstUuid != null) {
            tag.putUUID("First", firstUuid);
        }
        if (secondUuid != null) {
            tag.putUUID("Second", secondUuid);
        }
        tag.putInt("Age", tickCount);
    }
}
