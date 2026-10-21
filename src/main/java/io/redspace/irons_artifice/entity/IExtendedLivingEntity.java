package io.redspace.irons_artifice.entity;

import io.redspace.irons_artifice.advancement.ShotCombatTracker;
import io.redspace.irons_artifice.data.LastHitTarget;
import io.redspace.irons_artifice.data.RecentShots;
import io.redspace.irons_artifice.data.RecoilState;
import io.redspace.irons_artifice.item.FireDelayState;
import io.redspace.irons_artifice.item.PendingShot;

/**
 * Transient per-entity mod data carried by {@link io.redspace.irons_artifice.mixin.LivingEntityMixin}.
 * Cast any {@link net.minecraft.world.entity.LivingEntity} to reach it.
 */
public interface IExtendedLivingEntity {
    FireDelayState irons_artifice$getFireDelay();

    void irons_artifice$setFireDelay(FireDelayState state);

    PendingShot irons_artifice$getPendingShot();

    void irons_artifice$setPendingShot(PendingShot shot);

    RecoilState irons_artifice$getRecoil();

    void irons_artifice$setRecoil(RecoilState state);

    LastHitTarget irons_artifice$getLastHitTarget();

    void irons_artifice$setLastHitTarget(LastHitTarget target);

    RecentShots irons_artifice$getRecentShots();

    void irons_artifice$setRecentShots(RecentShots shots);

    ShotCombatTracker irons_artifice$getShotCombat();

    int irons_artifice$getBulletSelection();

    void irons_artifice$setBulletSelection(int index);
}
