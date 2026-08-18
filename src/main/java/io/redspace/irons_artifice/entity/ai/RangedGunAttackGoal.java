package io.redspace.irons_artifice.entity.ai;

import io.redspace.irons_artifice.entity.IGunslingerMob;
import io.redspace.irons_artifice.data.FireMode;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.FireDelayState;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.function.Consumer;

public class RangedGunAttackGoal<T extends Mob> extends Goal {
    private enum ShootPhase {
        IDLE,
        TELEGRAPHING_VOLLEY,
        VOLLEY
    }

    private final T mob;
    // todo: factor gun spread into effective range
    private AiGunRange bands;
    private final GunCombatMoveControl mover = new GunCombatMoveControl();
    private final float engageRangeSqr;
    private final int telegraphMin;
    private final int telegraphMax;
    private final int volleyIntervalMin;
    private final int volleyIntervalMax;

    private LivingEntity target;
    private boolean hasLos;
    private int seeTime;
    private int noSeeTime;
    private ShootPhase phase = ShootPhase.IDLE;
    private int telegraphRemaining;
    private int shotsRemaining;
    private int volleyCooldown;

    public RangedGunAttackGoal(T mob) {
        this(mob, 24, 15, 45, 40, 80);
    }

    public RangedGunAttackGoal(T mob, float range) {
        this(mob, range, 15, 45, 40, 80);
    }

    public RangedGunAttackGoal(T mob, float range, int telegraphMinTicks, int telegraphMaxTicks,
                               int volleyIntervalMin, int volleyIntervalMax) {
        this.mob = mob;
        this.bands = new AiGunRange(range);
        float follow = Math.max(mob.getAttributes().hasAttribute(Attributes.FOLLOW_RANGE)
                ? (float) mob.getAttributeValue(Attributes.FOLLOW_RANGE)
                : range, range);
        this.engageRangeSqr = Math.max(follow, range) * Math.max(follow, range);
        this.telegraphMin = Math.min(telegraphMinTicks, telegraphMaxTicks);
        this.telegraphMax = Math.max(telegraphMinTicks, telegraphMaxTicks);
        this.volleyIntervalMin = Math.min(volleyIntervalMin, volleyIntervalMax);
        this.volleyIntervalMax = Math.max(volleyIntervalMin, volleyIntervalMax);
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }

    protected void runHook(Consumer<IGunslingerMob> hook) {
        if (mob instanceof IGunslingerMob gunslingerMob) {
            hook.accept(gunslingerMob);
        }
    }

    @Override
    public boolean canUse() {
        LivingEntity next = mob.getTarget();
        if (next == null) {
            next = this.target;
        } else {
            this.target = next;
        }
        if (noSeeTime > 200) {
            this.target = null;
            return false;
        }
        return next != null && next.isAlive() && mob.canAttack(next)
                && isHoldingGun()
                && mob.distanceToSqr(next) <= engageRangeSqr;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        mob.setAggressive(true);
    }

    @Override
    public void stop() {
        mob.setAggressive(false);
        mob.setTarget(null);
        target = null;
        seeTime = 0;
        noSeeTime = 0;
        phase = ShootPhase.IDLE;
        telegraphRemaining = 0;
        shotsRemaining = 0;
        volleyCooldown = 0;
        mover.reset();
        mob.getNavigation().stop();
        mob.getMoveControl().strafe(0, 0);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) {
            return;
        }

        ItemStack gun = mob.getMainHandItem();
        if (!(gun.getItem() instanceof GunItem gunItem)) {
            return;
        }
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        hasLos = mob.getSensing().hasLineOfSight(target);
        if (hasLos) {
            seeTime++;
            noSeeTime = 0;
        } else {
            seeTime = 0;
            noSeeTime++;
        }

        double distSqr = mob.distanceToSqr(target);

        if (phase == ShootPhase.IDLE) {
            mover.tick(mob, target, bands, hasLos);
            if (volleyCooldown > 0) {
                volleyCooldown--;
            }
            if (GunItem.getMagazine(gun).isEmpty() && !GunItem.isReloading(gun)) {
                GunplayManager.attemptStartReload(mob, gun);
            }
        } else {
            mob.getNavigation().stop();
            mob.getMoveControl().strafe(0, 0);
            holdPlant();
        }

        switch (phase) {
            case IDLE -> {
                if (canStartVolley(gun, distSqr)) {
                    runHook(IGunslingerMob::onVolleyStart);
                    phase = ShootPhase.TELEGRAPHING_VOLLEY;
                    telegraphRemaining = randomBetween(telegraphMin, telegraphMax);
                    mob.getNavigation().stop();
                }
            }
            case TELEGRAPHING_VOLLEY -> {
                if (shouldCancelVolley(distSqr)) {
                    phase = ShootPhase.IDLE;
                    break;
                }
                if (--telegraphRemaining <= 0) {
                    ShotProfile profile = GunplayManager.compose(mob, gunItem.getGun(), gun);
                    shotsRemaining = rollVolleyShots(gun, gunItem, profile, mob.getRandom());
                    phase = ShootPhase.VOLLEY;
                }
            }
            case VOLLEY -> {
                if (shouldCancelVolley(distSqr)) {
                    endVolley();
                    break;
                }
                if (shotsRemaining > 0 && !FireDelayState.isActive(gun) && !GunItem.isReloading(gun)
                        && !GunItem.getMagazine(gun).isEmpty()) {
                    Vec3 aim = target.getEyePosition().subtract(mob.getEyePosition());
                    if (aim.lengthSqr() > 1.0E-6 && GunplayManager.tryFire(mob, aim.normalize())) {
                        shotsRemaining--;
                        mob.setNoActionTime(0);
                    }
                }
                if (shotsRemaining <= 0 || GunItem.getMagazine(gun).isEmpty()) {
                    endVolley();
                    if (GunItem.getMagazine(gun).isEmpty() && !GunItem.isReloading(gun)) {
                        GunplayManager.attemptStartReload(mob, gun);
                    }
                }
            }
        }
    }

    private void endVolley() {
        phase = ShootPhase.IDLE;
        shotsRemaining = 0;
        volleyCooldown = randomBetween(volleyIntervalMin, volleyIntervalMax);
        runHook(IGunslingerMob::onVolleyEnd);
    }

    private void holdPlant() {
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        mob.setYRot(mob.yHeadRot);
    }

    private boolean canStartVolley(ItemStack gun, double distSqr) {
        return volleyCooldown <= 0
                && hasLos
                && seeTime >= 20
                && distSqr <= bands.maxRangeSqr()
                && distSqr >= bands.panicSqr()
                && !GunItem.isReloading(gun)
                && !GunItem.getMagazine(gun).isEmpty();
    }

    private boolean shouldCancelVolley(double distSqr) {
        return !target.isAlive() || distSqr < bands.panicSqr() || noSeeTime > 40;
    }

    private int rollVolleyShots(ItemStack gun, GunItem gunItem, ShotProfile profile, RandomSource random) {
        int mag = GunItem.getMagazine(gun).count();
        int capacity = Math.max(1, gunItem.magazineCapacity());
        float minFrac = 0.20f;
        float maxFrac = 0.40f;
        if (profile.fireMode() == FireMode.AUTO) {
            minFrac *= 2;
            maxFrac *= 2;
        }
        float frac = Mth.lerp(random.nextFloat(), minFrac, maxFrac);
        int fromPercent = Math.max(1, Math.round(capacity * frac));
        return Math.min(mag, fromPercent);
    }

    private int randomBetween(int min, int max) {
        return Mth.randomBetweenInclusive(mob.getRandom(), min, max);
    }

    private boolean isHoldingGun() {
        return mob.getMainHandItem().getItem() instanceof GunItem;
    }
}
