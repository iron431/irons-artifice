package io.redspace.irons_artifice.api;

import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * Server-only event firing right before, and right after, a gunshot takes place. Offers a degree of mutability to the gunshot's parameters.
 */
public abstract class GunShootEvent extends LivingEvent {
    private final ShotProfile shotProfile;

    public GunShootEvent(LivingEntity entity, ShotProfile shotProfile) {
        super(entity);
        this.shotProfile = shotProfile;
    }

    public ShotProfile getShotProfile() {
        return shotProfile;
    }

    public static class Pre extends GunShootEvent {
        private Vec3 origin, direction;

        public Pre(LivingEntity entity, ShotProfile shotProfile, Vec3 origin, Vec3 direction) {
            super(entity, shotProfile);
            this.origin = origin;
            this.direction = direction;
        }

        public Vec3 getDirection() {
            return direction;
        }

        public Vec3 getOrigin() {
            return origin;
        }

        public void setOrigin(Vec3 origin) {
            this.origin = origin;
        }

        public void setDirection(Vec3 direction) {
            this.direction = direction;
        }
    }

    public static class Post extends GunShootEvent {
        public Post(LivingEntity entity, ShotProfile shotProfile) {
            super(entity, shotProfile);
        }
    }
}
