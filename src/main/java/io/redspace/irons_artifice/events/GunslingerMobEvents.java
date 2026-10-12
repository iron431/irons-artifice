package io.redspace.irons_artifice.events;

import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.entity.IGunslingerMob;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class GunslingerMobEvents {

    @SubscribeEvent
    public static void onComposeShot(ComposeShotEvent event) {
        IGunslingerMob.modifyMobGunshots(event);
    }
}
