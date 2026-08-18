package io.redspace.irons_artifice.api;

import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * Common event fired after a shot is composed, but before any side effects (fire delay, recoil, bullet spawning) have triggered. Only cancellable phase in the gunshot process.
 */
public class GunAboutToShootEvent extends LivingEvent implements ICancellableEvent {
    private final ShotProfile shotProfile;

    public GunAboutToShootEvent(LivingEntity entity, ShotProfile shotProfile) {
        super(entity);
        this.shotProfile = shotProfile;
    }

    public ShotProfile getShotProfile() {
        return shotProfile;
    }

}
