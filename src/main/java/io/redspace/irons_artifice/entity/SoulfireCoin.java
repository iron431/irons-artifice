package io.redspace.irons_artifice.entity;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.api.BulletImpactEvent;
import io.redspace.irons_artifice.damage.DamageSources;
import io.redspace.irons_artifice.data.ParticleStack;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.PostHitEffects;
import io.redspace.irons_artifice.modifier.on_hit_handlers.SoulFirePostHit;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.SoundRegistry;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

@EventBusSubscriber(modid = IronsArtifice.MODID)
public class SoulfireCoin extends Entity {
    public static final int ARMING_TICKS = 5;
    public static final int LIFETIME_TICKS = 10 * 20;
    public static final double LAUNCH_SPEED = 0.6;
    public static final double LAUNCH_SIDE_SPEED = 0.20;
    public static final double LAUNCH_SIDE_VARIANCE = 0.50;
    public static final float REDIRECT_SPREAD_DEGREES = 3f;
    public static final double REDIRECT_MAX_SPEED = 5;
    public static final double DAMAGE_BONUS = 0.5;
    public static final int SOUL_FIRE_TICKS = 5 * 20;

    private @Nullable LivingEntity target;

    public SoulfireCoin(EntityType<? extends SoulfireCoin> type, Level level) {
        super(type, level);
        setInvulnerable(true);
    }

    public static SoulfireCoin launchSoul(ServerLevel level, LivingEntity target, Vec3 incoming) {
        SoulfireCoin soulfireCoin = new SoulfireCoin(EntityRegistry.SOUL.get(), level);
        soulfireCoin.target = target;
        soulfireCoin.setPos(target.getEyePosition());
        RandomSource random = level.getRandom();
        Vec3 side = sideways(incoming, random).scale(random.nextBoolean() ? 1 : -1);
        double sideSpeed = LAUNCH_SIDE_SPEED * (1 - LAUNCH_SIDE_VARIANCE + random.nextDouble() * 2 * LAUNCH_SIDE_VARIANCE);
        soulfireCoin.setDeltaMovement(side.scale(sideSpeed).add(0, LAUNCH_SPEED, 0));
        level.addFreshEntity(soulfireCoin);
        return soulfireCoin;
    }

    private static Vec3 sideways(Vec3 incoming, RandomSource random) {
        Vec3 side = incoming.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < 1.0E-6) {
            double angle = random.nextDouble() * Math.PI * 2;
            return new Vec3(Math.cos(angle), 0, Math.sin(angle));
        }
        return side.normalize();
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level() instanceof ServerLevel serverLevel && event.getSource().getDirectEntity() instanceof Bullet bullet && !event.getSource().is(DamageSources.SOUL_DAMAGE_TYPE)) {
            double soulDamage = bullet.getProfile().components().getOrDefault(ShotComponents.SOUL_DAMAGE).compute();
            if (soulDamage > 0) {
                event.getEntity().hurtServer(serverLevel, DamageSources.soul(serverLevel, bullet, bullet.getOwner()), (float) (soulDamage * event.getAmount()));
            }
        }
    }

    @SubscribeEvent
    public static void onBulletImpact(BulletImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof SoulfireCoin soulfireCoin)) {
            return;
        }
        Bullet bullet = event.getBullet();
        if (bullet.level().isClientSide()) {
            event.setHitState(Bullet.HitState.STOP);
            return;
        }
        redirectBullet(event, hit, soulfireCoin, bullet);
    }

    private static void redirectBullet(BulletImpactEvent event, EntityHitResult hit, SoulfireCoin soulfireCoin, Bullet bullet) {
        LivingEntity target = soulfireCoin.findRedirectTarget(bullet.getOwner());
        if (target == null) {
            event.setHitState(Bullet.HitState.CONTINUE);
        } else {
            Vec3 aim = target.getBoundingBox().getCenter().subtract(hit.getLocation());
            Vec3 direction = Utils.directionWithinCone(aim, REDIRECT_SPREAD_DEGREES, bullet.getRandom());
            bullet.setDeltaMovement(direction.scale(Math.min(bullet.getDeltaMovement().length(), REDIRECT_MAX_SPEED)));
            bullet.needsSync = true;
            bullet.clearPierced();
            event.setHitState(Bullet.HitState.STOP);
        }
        empower(bullet);
        RandomSource random = bullet.getRandom();
        Vec3 location = event.getRayTraceResult().getLocation();
        bullet.level().playSound(null, location.x, location.y, location.z, SoundRegistry.BULLET_IMPACT_RICOCHET, SoundSource.NEUTRAL, 2f, 0.9f + random.nextFloat() * 0.2f);
        bullet.level().playSound(null, location.x, location.y, location.z, SoundRegistry.SOULFIRE_COIN_HIT, SoundSource.NEUTRAL, 2f, 0.9f + random.nextFloat() * 0.2f);
    }

    private static void empower(Bullet bullet) {
        ShotComponentMap components = bullet.getProfile().components();

        components.getOrCreate(ShotComponents.SOUL_DAMAGE).addModifier(new ValueModifier(DAMAGE_BONUS, ValueModifier.Operation.ADD, ValueModifier.Type.BENEFICIAL));

        PostHitEffects postHit = components.getOrDefault(ShotComponents.POST_HIT_EFFECTS).copy();
        postHit.getOrCreate(SoulFirePostHit.class, () -> new SoulFirePostHit(SOUL_FIRE_TICKS));
        components.set(ShotComponents.POST_HIT_EFFECTS, postHit);

        bullet.getProfile().components().getOrCreate(ShotComponents.PARTICLE_TRAIL).addAccent(new ParticleStack.ParticleAccent(ParticleTypes.SOUL, 1));

        components.remove(ShotComponents.SOUL_TOKEN);
    }

    @Override
    public void tick() {
        super.tick();
        setInvulnerable(tickCount < ARMING_TICKS);
        applyGravity();
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(0.9));
        if (level() instanceof ServerLevel serverLevel) {
            if (onGround() || tickCount >= LIFETIME_TICKS) {
                die(serverLevel);
            }
        } else {
            double y = getY() + getBbHeight() * 0.5;
            level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, getX(), y, getZ(), 0, 0.02, 0);
            if (random.nextFloat() < 0.2f) {
                level().addParticle(ParticleTypes.SOUL, getX(), y, getZ(), 0, 0.02, 0);
            }
        }
    }

    public @Nullable LivingEntity findRedirectTarget(@Nullable Entity shooter) {
        float targetRange = 32;
        float fallbackRange = 16;

        if (isValidTarget(target, shooter, targetRange)) {
            return target;
        }
        return level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(fallbackRange),
                        candidate -> isValidTarget(candidate, shooter, fallbackRange))
                .stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    private boolean isValidTarget(@Nullable LivingEntity candidate, @Nullable Entity shooter, double range) {
        return candidate != null
                && candidate.isAlive()
                && distanceToSqr(candidate) <= range * range
                && Utils.canHarm(shooter, candidate)
                && Utils.hasLineOfSight(this, candidate);
    }

    private void die(ServerLevel level) {
        level.sendParticles(ParticleTypes.SOUL, getX(), getY() + getBbHeight() * 0.5, getZ(), 8, 0.15, 0.15, 0.15, 0.06);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + getBbHeight() * 0.5, getZ(), 8, 0.15, 0.15, 0.15, 0.18);
        playSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1f);
        discard();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        die(level);
        return true;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.01;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
