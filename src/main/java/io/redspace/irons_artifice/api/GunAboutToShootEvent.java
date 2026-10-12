package io.redspace.irons_artifice.api;

import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * Common event fired after a shot is composed, but before any side effects (fire delay, recoil, bullet spawning) have triggered. Only cancellable phase in the gunshot process.
 */
@Cancelable
public class GunAboutToShootEvent extends LivingEvent {
    private final ShotProfile shotProfile;

    public GunAboutToShootEvent(LivingEntity entity, ShotProfile shotProfile) {
        super(entity);
        this.shotProfile = shotProfile;
    }

    public ShotProfile getShotProfile() {
        return shotProfile;
    }

}
