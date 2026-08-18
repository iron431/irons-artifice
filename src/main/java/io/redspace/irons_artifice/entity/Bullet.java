package io.redspace.irons_artifice.entity;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.damage.DamageSources;
import io.redspace.irons_artifice.data.ParticleStack;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.gun.BlockDamageManager;
import io.redspace.irons_artifice.gun.Guns;
import io.redspace.irons_artifice.gun.HitEntityAccumulator;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.modifier.OnHitEffect;
import io.redspace.irons_artifice.modifier.PostHitEffect;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.network.packets.ClientboundBulletImpactPacket;
import io.redspace.irons_artifice.network.packets.ClientboundBulletTrailPacket;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class Bullet extends Projectile {
    private static final EntityDataAccessor<Float> DATA_GRAVITY =
            SynchedEntityData.defineId(Bullet.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_RICOCHET =
            SynchedEntityData.defineId(Bullet.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DRAG =
            SynchedEntityData.defineId(Bullet.class, EntityDataSerializers.FLOAT);

    public static final double BASE_SPEED = 12;
    public static final double TRAIL_DENSITY = 3.0;

    private ShotProfile profile = new ShotProfile(ItemStack.EMPTY, Guns.MUSKET, MagazineContents.EMPTY, new ShotComponentMap());
    private int piercingRemaining = 0;
    private final Set<Integer> piercedEntities = new HashSet<>();
    private HitState hitState = HitState.CONTINUE;
    private static final int TRAIL_COMPENSATION_TICKS = 5;

    public Bullet(EntityType<? extends Bullet> type, Level level) {
        super(type, level);
    }

    enum HitState {
        CONTINUE,
        STOP,
        DISCARD;
    }

    public void applyProfile(ShotProfile profile) {
        this.profile = profile;
        this.piercingRemaining = (int) profile.value(ShotComponents.PIERCING);
        // synced parameters for movement parity
        this.entityData.set(DATA_GRAVITY, (float) profile.value(ShotComponents.GRAVITY));
        this.entityData.set(DATA_RICOCHET, (int) profile.value(ShotComponents.RICOCHET));
        this.entityData.set(DATA_DRAG, (float) profile.value(ShotComponents.BULLET_DRAG));
    }

    public ShotProfile getProfile() {
        return profile;
    }

    public void markPierced(Entity entity) {
        piercedEntities.add(entity.getId());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_GRAVITY, 0.05f);
        builder.define(DATA_RICOCHET, 0);
        builder.define(DATA_DRAG, 0.98f);
    }

    @Override
    protected boolean canHitEntity(@NonNull Entity entity) {
        return super.canHitEntity(entity) && !piercedEntities.contains(entity.getId()) && Utils.canHarm(getOwner(), entity);
    }

    public float resolveDamage() {
        return (float) (profile.value(ShotComponents.DAMAGE) / Math.max(1, profile.value(ShotComponents.PROJECTILE_COUNT)));
    }

    public static @Nullable EntityHitResult getEntityHitResult(
            Level level, Entity source, Vec3 from, Vec3 to, AABB targetSearchArea, Predicate<Entity> matching, float entityMargin, float motionMargin
    ) {
        double nearest = Double.MAX_VALUE;
        Optional<Vec3> nearestLocation = Optional.empty();
        Entity hitEntity = null;

        for (Entity entity : level.getEntities(source, targetSearchArea, matching)) {
            AABB bb = entity.getBoundingBox().inflate(entityMargin).expandTowards(entity.getDeltaMovement().scale(motionMargin));
            Optional<Vec3> location = bb.clip(from, to);
            if (location.isPresent()) {
                double dd = from.distanceToSqr(location.get());
                if (dd < nearest) {
                    hitEntity = entity;
                    nearest = dd;
                    nearestLocation = location;
                }
            }
        }
        return hitEntity == null ? null : new EntityHitResult(hitEntity, nearestLocation.get());
    }

    private static final double SEEK_LOOK_AHEAD = 40.0;
    private static final double SEEK_FORWARD_DOT = 0.2;

    protected void handleSeeking() {
        double seeking = profile.value(ShotComponents.SEEKING);
        if (seeking <= 0) {
            return;
        }
        Vec3 motion = getDeltaMovement();
        double speed = motion.length();
        float strength = Mth.clamp((float) seeking, 0.0F, 1.0F);
        double range = 7 * (1 + strength);
        Vec3 start = position();
        Vec3 direction = motion.scale(1.0 / speed);
        Vec3 end = level().clip(new ClipContext(
                start,
                start.add(direction.scale(SEEK_LOOK_AHEAD)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                CollisionContext.empty()
        )).getLocation();
        AABB targetSearchArea = new AABB(start, end).inflate(range);

        Entity closest = null;
        double closestDistSqr = Double.MAX_VALUE;
        for (Entity entity : level().getEntities(this, targetSearchArea, this::canHitEntity)) {
            Vec3 targetPos = entity.getBoundingBox().getCenter();
            Vec3 toTarget = targetPos.subtract(start);
            double toTargetLengthSqr = toTarget.lengthSqr();
            if (toTargetLengthSqr < 1.0E-6) {
                continue;
            }
            if (toTarget.normalize().dot(direction) < SEEK_FORWARD_DOT) {
                continue;
            }
            if (distanceToSegmentSqr(targetPos, start, end) > range * range) {
                continue;
            }
            if (!Utils.hasLineOfSight(this, entity)) {
                continue;
            }
            if (toTargetLengthSqr < closestDistSqr) {
                closest = entity;
                closestDistSqr = toTargetLengthSqr;
            }
        }
        if (closest == null) {
            return;
        }
        setDeltaMovement(homeTowards(closest.getBoundingBox().getCenter(), strength));
    }

    private static double distanceToSegmentSqr(Vec3 point, Vec3 from, Vec3 to) {
        Vec3 segment = to.subtract(from);
        double lengthSqr = segment.lengthSqr();
        if (lengthSqr < 1.0E-6) {
            return point.distanceToSqr(from);
        }
        double t = Mth.clamp(point.subtract(from).dot(segment) / lengthSqr, 0.0, 1.0);
        return point.distanceToSqr(from.add(segment.scale(t)));
    }

    protected Vec3 homeTowards(Vec3 target, float strength) {
        double speed = this.getDeltaMovement().length();
        Vec3 currentMotion = this.getDeltaMovement().normalize();
        Vec3 wantedMotion = target.subtract(this.position()).normalize();
        if (wantedMotion.lengthSqr() < 1.0E-6) {
            return this.getDeltaMovement();
        }
        return slerp(strength, currentMotion, wantedMotion).scale(speed);
    }

    public static Vec3 slerp(double t, Vec3 from, Vec3 to) {
        t = Mth.clamp(t, 0.0, 1.0);
        from = from.normalize();
        to = to.normalize();
        double dot = Mth.clamp(from.dot(to), -1.0, 1.0);
        if (dot > 0.9995) {
            return from.lerp(to, t).normalize();
        }
        double theta = Math.acos(dot) * t;
        Vec3 relative = to.subtract(from.scale(dot));
        if (relative.lengthSqr() < 1.0E-6) {
            return from;
        }
        relative = relative.normalize();
        return from.scale(Math.cos(theta)).add(relative.scale(Math.sin(theta)));
    }

    @Override
    public void tick() {
        super.tick();

        handleSeeking();
        Vec3 delta = getDeltaMovement();
        Vec3 position = position();
        Vec3 destination = position.add(delta);

        int i = 64;
        this.hitState = HitState.CONTINUE;
        while (hitState == HitState.CONTINUE && --i > 0) {
            HitResult blockHit = level().clip(new ClipContext(
                    position, destination, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
            Vec3 blockClip = blockHit.getLocation();
            EntityHitResult entityHit = getEntityHitResult(level(), this, position, blockClip, getBoundingBox().expandTowards(delta).inflate(1),
                    this::canHitEntity, 0.25f, 3f);
            if (entityHit != null) {
                onHit(entityHit);
                destination = entityHit.getLocation();
            } else if (blockHit.getType() != HitResult.Type.MISS) {
                onHit(blockHit);
                destination = blockHit.getLocation();
            } else {
                hitState = HitState.STOP;
            }
            if (hitState == HitState.STOP) {
                break;
            } else if (hitState == HitState.DISCARD) {
                discard();
                break;
            }
        }
        if (i == 0) {
            IronsArtifice.LOGGER.warn("Bullet ran to max iterations! Did something break, or did you just shoot 64 things?");
        }

        if (level() instanceof ServerLevel serverLevel) {
            Vec3 particleStart = position;
            Vec3 particleEnd = destination;
            if (tickCount < TRAIL_COMPENSATION_TICKS) {
                Vec3 right = particleEnd.subtract(particleStart).cross(new Vec3(0, 1, 0)).normalize();
                Vec3 offset = new Vec3(0, -0.25, 0).add(right.scale(0.25));
                particleStart = position.add(offset.lerp(Vec3.ZERO, this.tickCount / (float) TRAIL_COMPENSATION_TICKS));
                if (tickCount == 1) {
                    particleStart = particleStart.add(delta.scale(0.1));
                }
                particleEnd = destination.add(offset.lerp(Vec3.ZERO, (this.tickCount + 1) / (float) TRAIL_COMPENSATION_TICKS));
            }
            emitTrail(serverLevel, particleStart, particleEnd);
        }

        this.setDeltaMovement(getDeltaMovement().scale(getDrag()));
        this.applyGravity();
        this.setPos(destination);
        if (this.getDeltaMovement().lengthSqr() < 1) {
            discard();
        }
    }

    private double distribution(RandomSource randomSource) {
        double d = randomSource.nextDouble() - randomSource.nextDouble();
//        return (d * d) * Math.signum(d);
        return d;
    }

    @Override
    public @NonNull Vec3 getMovementToShoot(double xd, double yd, double zd, float pow, float uncertainty) {
        return Utils.directionWithinCone(new Vec3(xd, yd, zd), uncertainty, this.random).scale(pow);
    }

    @Override
    protected void onHit(@NonNull HitResult hitResult) {
        hitState = HitState.DISCARD; // setup default hit state
        super.onHit(hitResult);
        if (level() instanceof ServerLevel serverLevel) {
            HitEntityAccumulator accumulator = new HitEntityAccumulator();
            if (hitResult instanceof EntityHitResult entityHit) {
                accumulator.add(entityHit.getEntity());
            }
            for (OnHitEffect effect : profile.get(ShotComponents.ON_HIT).all()) {
                effect.onHit(serverLevel, this, hitResult, accumulator);
            }
            var postHitEffects = profile.get(ShotComponents.POST_HIT_EFFECTS).all();
            for (Entity entity : accumulator.all()) {
                for (PostHitEffect effect : postHitEffects) {
                    effect.postHit(serverLevel, this, hitResult, entity);
                }
            }
            profile.get(ShotComponents.IMPACT_SOUND).playImpactSound(serverLevel, hitResult.getLocation(), hitResult.getType() == HitResult.Type.ENTITY);
        }
        if (hitState != HitState.CONTINUE && hitResult instanceof BlockHitResult blockHitResult) {
            // normally this would be handled in block hit, but we want to wait until bullet onhit effects have run before mutating entity direction
            // on solid block impact, attempt to ricochet
            int ricochet = this.entityData.get(DATA_RICOCHET);
            if (ricochet > 0) {
                this.entityData.set(DATA_RICOCHET, ricochet - 1);
                reflectMotion(blockHitResult.getDirection());
                hitState = HitState.STOP; // stop in place and resume next tick, raycaster doesn't handle bent segments
            }
        }
    }

    @Override
    protected void onHitEntity(@NonNull EntityHitResult result) {
        super.onHitEntity(result);
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Entity target = result.getEntity();
        if (!piercedEntities.add(target.getId())) {
            return;
        }
        Entity owner = getOwner();
        float damage = resolveDamage();
        DamageSource source = DamageSources.bullet(level(), this, owner);
        target.hurtServer(serverLevel, source, damage);

        float knockback = (float) profile.value(ShotComponents.KNOCKBACK);
        if (target instanceof LivingEntity living && knockback > 0.0F) {
            Vec3 v = getDeltaMovement();
            living.knockback(knockback, -v.x, -v.z);
        }
        if (piercingRemaining > 0) {
            this.hitState = HitState.CONTINUE;
            piercingRemaining--;
        }
    }

    protected void playBlockHitEffects(BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        level().playSound(null, pos, level().getBlockState(pos).getSoundType(level(), pos, null).getBreakSound(), SoundSource.BLOCKS, .75f, 1f);
        if (level() instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, this.chunkPosition(), new ClientboundBulletImpactPacket(hitResult.getLocation(), this.getDeltaMovement(), hitResult.getDirection().getUnitVec3(), this.resolveDamage()));
        }
    }

    /**
     * @return whether the block was completed destroyed
     */
    protected boolean attemptApplyBlockDamage(BlockHitResult hitResult) {
        if (!(level() instanceof ServerLevel serverLevel) || !profile.get(ShotComponents.BREAKS_BLOCKS)) {
            return false;
        }

        if (getOwner() instanceof Mob
                && !serverLevel.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return false;
        }
        var pos = hitResult.getBlockPos();
        var state = level().getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        float damage = (float) (this.profile.value(ShotComponents.BLOCK_DAMAGE_MULTIPLIER) * resolveDamage());
        return BlockDamageManager.applyDamage(serverLevel, pos, state, damage, this);
    }

    @Override
    protected void onHitBlock(@NonNull BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        playBlockHitEffects(hitResult);
        boolean brokeThroughBlock = attemptApplyBlockDamage(hitResult);
        if (brokeThroughBlock) {
            // if we break through block, continue forward, and maybe pierce for additional buffs
            hitState = HitState.CONTINUE;
            if (piercingRemaining > 0) {
                piercingRemaining--;
            } else {
                // allow the bullet to continue without piercing, but do not allow additional block damage thereafter
                profile.remove(ShotComponents.BREAKS_BLOCKS);
            }
        }
    }

    private void reflectMotion(Direction face) {
        setDeltaMovement(Utils.reflect(getDeltaMovement(), face.getUnitVec3()));
        this.piercedEntities.clear();
    }

    @Override
    protected double getDefaultGravity() {
        return this.entityData.get(DATA_GRAVITY);
    }

    public float getDrag() {
        return this.entityData.get(DATA_DRAG);
    }

    private void emitTrail(ServerLevel level, Vec3 from, Vec3 to) {
        ParticleStack particles = profile.get(ShotComponents.PARTICLE_TRAIL);
        if (particles == null) {
            return;
        }
        List<ParticleOptions> palette = particles.getParticles();
        List<ParticleStack.ParticleAccent> accents = new ArrayList<>(particles.getAccents());
        if (level.isFluidAtPosition(BlockPos.containing(from), s -> s.is(FluidTags.WATER))) {
            accents.add(new ParticleStack.ParticleAccent(ParticleTypes.BUBBLE, 1.0));
        }
        int steps = (int) Math.round(from.distanceTo(to) * TRAIL_DENSITY);
        if (steps <= 0 || palette.isEmpty()) {
            return;
        }
        ClientboundBulletTrailPacket payload = new ClientboundBulletTrailPacket(from, to, palette, accents);
        if (this.isRemoved()) {
            PacketDistributor.sendToPlayersTrackingChunk(level, this.chunkPosition(), payload);
        } else {
            PacketDistributor.sendToPlayersTrackingEntity(this, payload);
        }
    }

    @Override
    public void checkDespawn() {
        if (this.level() instanceof ServerLevel serverLevel && !serverLevel.getChunkSource().chunkMap.getDistanceManager().inEntityTickingRange(this.chunkPosition().pack())) {
            this.discard();
        }
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
