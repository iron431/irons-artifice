package io.redspace.irons_artifice.modifier;

import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public interface OnShotEffect {
    /**
     * Called once per trigger pull on the server, after the shot profile is final and before any pellet spawns.
     * Anything set on the profile here is shared by every pellet of the shot, because pellets take a shallow copy.
     */
    void onShot(ServerLevel level, LivingEntity shooter, ShotProfile profile);
}
