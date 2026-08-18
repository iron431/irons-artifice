package io.redspace.irons_artifice.api;

import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * Server-only event called if ammo is about to be deducted for shooting. Only called after {@link io.redspace.irons_artifice.data.ShotComponents#AMMO_CONSUME_CHANCE} is rolled.
 */
public class ConsumeAmmoEvent extends LivingEvent {
    private final ShotProfile shotProfile;
    private int ammoToConsume;

    public ConsumeAmmoEvent(LivingEntity entity, ShotProfile shotProfile) {
        super(entity);
        this.shotProfile = shotProfile;
        this.ammoToConsume = 1;
    }

    public ShotProfile getShotProfile() {
        return shotProfile;
    }

    public int getAmmoToConsume() {
        return ammoToConsume;
    }

    public void setAmmoToConsume(int ammoToConsume) {
        this.ammoToConsume = ammoToConsume;
    }
}
