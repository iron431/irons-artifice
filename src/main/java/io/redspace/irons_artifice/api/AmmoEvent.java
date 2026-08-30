package io.redspace.irons_artifice.api;

import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;


public abstract class AmmoEvent extends LivingEvent {
    private final ShotProfile shotProfile;

    public AmmoEvent(LivingEntity entity, ShotProfile shotProfile) {
        super(entity);
        this.shotProfile = shotProfile;
    }

    /**
     * Server-only event called to consume ammo from the shooter's inventory. Can only be canceled, not mutated.
     */
    public static class Consume extends AmmoEvent implements ICancellableEvent {
        private final int ammoToConsume;

        public Consume(LivingEntity entity, ShotProfile shotProfile, int ammoToConsume) {
            super(entity, shotProfile);
            this.ammoToConsume = ammoToConsume;
        }

        public int getAmmoToConsume() {
            return ammoToConsume;
        }
    }

    /**
     * Common event called to calculate the ammo amount required for shooting, and that will be consumed later via {@link AmmoEvent.Consume}.
     */
    public static class Amount extends AmmoEvent {
        private int ammoToConsume;

        public Amount(LivingEntity entity, ShotProfile shotProfile, int ammoToConsume) {
            super(entity, shotProfile);
            this.ammoToConsume = ammoToConsume;
        }

        public int getAmmoToConsume() {
            return ammoToConsume;
        }

        public void setAmmoToConsume(int ammoToConsume) {
            this.ammoToConsume = ammoToConsume;
        }
    }


    public ShotProfile getShotProfile() {
        return shotProfile;
    }


}
