package io.redspace.irons_artifice.events;

import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.entity.IGunslingerMob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class GunslingerMobEvents {

    @SubscribeEvent
    public static void onComposeShot(ComposeShotEvent event) {
        IGunslingerMob.modifyMobGunshots(event);
    }
}
