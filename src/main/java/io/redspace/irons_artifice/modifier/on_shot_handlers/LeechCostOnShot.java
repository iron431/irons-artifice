package io.redspace.irons_artifice.modifier.on_shot_handlers;

import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.modifier.OnShotEffect;
import io.redspace.irons_artifice.modifier.modifiers.LeechModifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public final class LeechCostOnShot implements OnShotEffect {
    @Override
    public void onShot(ServerLevel level, LivingEntity shooter, ShotProfile profile) {
        if (!LeechModifier.canLeech(shooter, profile) || LeechModifier.hasInfiniteMaterials(shooter)) {
            return;
        }
        shooter.setHealth(Math.max(0.1f, shooter.getHealth() - LeechModifier.HEALTH_COST));
        shooter.hurtMarked = true;
    }
}
