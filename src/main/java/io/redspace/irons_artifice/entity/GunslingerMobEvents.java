package io.redspace.irons_artifice.entity;

import io.redspace.irons_artifice.api.ComposeShotEvent;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Central game-bus handler for mob gunshot composition.
 * Moved out of IGunslingerMob to avoid @SubscribeEvent on a supertype
 * (NeoForge 8.x EventBus forbids registering a class whose interface/superclass
 * declares a @SubscribeEvent method — see EventBus.checkSupertypes).
 * Illificer implements IGunslingerMob and is itself an @EventBusSubscriber;
 * keeping the handler here avoids the automatic-registration clash.
 */
@EventBusSubscriber
public class GunslingerMobEvents {

    @SubscribeEvent
    public static void modifyMobGunshots(ComposeShotEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }
        if (event.getEntity() instanceof IGunslingerMob gunslinger) {
            gunslinger.customizeMobShot(mob, event.getShotProfile());
        } else {
            IGunslingerMob.applyDefaultMobNerfs(mob, event.getShotProfile());
        }
    }
}
