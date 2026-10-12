package io.redspace.irons_artifice.mixin;

import io.redspace.irons_artifice.advancement.ShotCombatTracker;
import io.redspace.irons_artifice.data.LastHitTarget;
import io.redspace.irons_artifice.data.RecentShots;
import io.redspace.irons_artifice.data.RecoilState;
import io.redspace.irons_artifice.entity.IExtendedLivingEntity;
import io.redspace.irons_artifice.item.FireDelayState;
import io.redspace.irons_artifice.item.PendingShot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements IExtendedLivingEntity {
    @Unique
    private FireDelayState irons_artifice$fireDelay = FireDelayState.NONE;
    @Unique
    private PendingShot irons_artifice$pendingShot = PendingShot.NONE;
    @Unique
    private RecoilState irons_artifice$recoil = RecoilState.NONE;
    @Unique
    private LastHitTarget irons_artifice$lastHitTarget = LastHitTarget.NONE;
    @Unique
    private RecentShots irons_artifice$recentShots = RecentShots.NONE;
    @Unique
    private final ShotCombatTracker irons_artifice$shotCombat = new ShotCombatTracker();

    @Override
    public FireDelayState irons_artifice$getFireDelay() {
        return irons_artifice$fireDelay;
    }

    @Override
    public void irons_artifice$setFireDelay(FireDelayState state) {
        this.irons_artifice$fireDelay = state;
    }

    @Override
    public PendingShot irons_artifice$getPendingShot() {
        return irons_artifice$pendingShot;
    }

    @Override
    public void irons_artifice$setPendingShot(PendingShot shot) {
        this.irons_artifice$pendingShot = shot;
    }

    @Override
    public RecoilState irons_artifice$getRecoil() {
        return irons_artifice$recoil;
    }

    @Override
    public void irons_artifice$setRecoil(RecoilState state) {
        this.irons_artifice$recoil = state;
    }

    @Override
    public LastHitTarget irons_artifice$getLastHitTarget() {
        return irons_artifice$lastHitTarget;
    }

    @Override
    public void irons_artifice$setLastHitTarget(LastHitTarget target) {
        this.irons_artifice$lastHitTarget = target;
    }

    @Override
    public RecentShots irons_artifice$getRecentShots() {
        return irons_artifice$recentShots;
    }

    @Override
    public void irons_artifice$setRecentShots(RecentShots shots) {
        this.irons_artifice$recentShots = shots;
    }

    @Override
    public ShotCombatTracker irons_artifice$getShotCombat() {
        return irons_artifice$shotCombat;
    }
}
