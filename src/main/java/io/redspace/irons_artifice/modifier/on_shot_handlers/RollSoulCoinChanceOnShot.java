package io.redspace.irons_artifice.modifier.on_shot_handlers;

import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.SoulToken;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.modifier.OnShotEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public final class RollSoulCoinChanceOnShot implements OnShotEffect {
    @Override
    public void onShot(ServerLevel level, LivingEntity shooter, ShotProfile profile) {
        double soulChance = profile.value(ShotComponents.SOUL_CHANCE);
        if (soulChance > 0 && level.getRandom().nextDouble() < soulChance) {
            profile.components().set(ShotComponents.SOUL_TOKEN, SoulToken.available());
        }
    }
}
