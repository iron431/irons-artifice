package io.redspace.irons_artifice.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Drowned;
import org.jetbrains.annotations.NotNull;

public class DrownedRangedGunAttackGoal extends RangedGunAttackGoal<Drowned> {
    public DrownedRangedGunAttackGoal(Drowned mob, float range, int telegraphMinTicks, int telegraphMaxTicks, int volleyIntervalMin, int volleyIntervalMax) {
        super(mob, range, telegraphMinTicks, telegraphMaxTicks, volleyIntervalMin, volleyIntervalMax);
    }

    public DrownedRangedGunAttackGoal(Drowned mob, float range, int telegraphMinTicks, int telegraphMaxTicks, int volleyIntervalMin, int volleyIntervalMax, double chargeSpeed, int chargeMaxTicks, int chargeCooldownTicks) {
        super(mob, range, telegraphMinTicks, telegraphMaxTicks, volleyIntervalMin, volleyIntervalMax, chargeSpeed, chargeMaxTicks, chargeCooldownTicks);
    }

    private static class SwimmingGunMoveControl extends GunCombatMoveControl {
        @Override
        public void tick(Mob mob, LivingEntity target, AiGunRange bands, PositionMode mode, double chargeSpeed) {
            super.tick(mob, target, bands, mode, chargeSpeed);
            if (mob.isInWater() && target != null && target.getY() - mob.getY() > 2.5) {
                mob.setDeltaMovement(mob.getDeltaMovement().add(0.0, 0.01, 0.0));
            }
        }

        @Override
        protected void charge(Mob mob, LivingEntity target, double chargeSpeed) {
            if (mob.isInWater()) {

                mob.getNavigation().moveTo(target, chargeSpeed * 4);
            } else {
                super.charge(mob, target, chargeSpeed);
            }

        }

        @Override
        protected void orbit(Mob mob, LivingEntity target, double distSqr, AiGunRange bands) {
            if (mob.isInWater()) {
                return;
            } else {
                super.backpedal(mob, target, distSqr, bands);
            }
        }

        @Override
        protected void backpedal(Mob mob, LivingEntity target, double distSqr, AiGunRange bands) {
            if (mob.isInWater()) {
                // strafe doesnt really work underwater
                flee(mob, target, bands);
            } else {
                super.backpedal(mob, target, distSqr, bands);
            }
        }
    }

    @Override
    protected @NotNull GunCombatMoveControl createMoveControl() {
        return new SwimmingGunMoveControl();
    }
}
