package io.redspace.irons_artifice.api;

import io.redspace.irons_artifice.gun.GunProfile;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * Common event fired after a {@code ShotProfile} is composed via {@link io.redspace.irons_artifice.item.GunplayManager#compose(LivingEntity, GunProfile, ItemStack)}. Can mutate parameters.
 */
public class ComposeShotEvent extends LivingEvent {
    private final ShotProfile shotProfile;

    public ComposeShotEvent(LivingEntity entity, ShotProfile shotProfile) {
        super(entity);

        this.shotProfile = shotProfile;
    }

    public ShotProfile getShotProfile() {
        return shotProfile;
    }
}
