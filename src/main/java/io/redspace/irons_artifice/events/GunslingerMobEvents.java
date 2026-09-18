package io.redspace.irons_artifice.events;

import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.entity.IGunslingerMob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Owns the {@link ComposeShotEvent} subscription for {@link IGunslingerMob}. The handler cannot live on
 * the interface itself: the event bus refuses to register a listener object -- such as {@code Illificer}
 * -- when one of its supertypes declares a {@code @SubscribeEvent} method.
 */
@EventBusSubscriber
public class GunslingerMobEvents {

    @SubscribeEvent
    public static void onComposeShot(ComposeShotEvent event) {
        IGunslingerMob.modifyMobGunshots(event);
    }
}
