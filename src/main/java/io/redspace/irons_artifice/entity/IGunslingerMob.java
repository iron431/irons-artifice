package io.redspace.irons_artifice.entity;

import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

public interface IGunslingerMob {
    default void customizeMobShot(@NotNull Mob mob, @NotNull ShotProfile shotProfile) {
        applyDefaultMobNerfs(mob, shotProfile);
    }

    default void onVolleyEnd() {

    }

    default void onVolleyStart() {
    }

    /* **********************
     * Static Handlers
     ********************** */

    /**
     * Dispatch for {@link ComposeShotEvent}. Subscribed from
     * {@link io.redspace.irons_artifice.events.GunslingerMobEvents}: this interface cannot carry the
     * {@code @SubscribeEvent} itself, because the event bus refuses to register any listener object
     * whose supertype declares one.
     */
    static void modifyMobGunshots(ComposeShotEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }
        if (event.getEntity() instanceof IGunslingerMob gunslinger) {
            gunslinger.customizeMobShot(mob, event.getShotProfile());
        } else {
            applyDefaultMobNerfs(mob, event.getShotProfile());
        }
    }

    static void applyDefaultMobNerfs(@NotNull Mob mob, @NotNull ShotProfile profile) {
        profile.modifyValue(ShotComponents.DAMAGE, new ValueModifier(-0.25, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL));
        int difficultyIndex = mob.level().getDifficulty().getId();
        int spread = 4 - difficultyIndex;
        profile.modifyValue(ShotComponents.BULLET_SPEED, new ValueModifier(-0.25, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL));
        profile.modifyValue(ShotComponents.SPREAD, new ValueModifier(spread, ValueModifier.Operation.ADD, ValueModifier.Type.HARMFUL));
    }
}
