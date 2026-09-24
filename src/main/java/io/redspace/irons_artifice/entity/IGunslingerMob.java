package io.redspace.irons_artifice.entity;

import io.redspace.irons_artifice.IronsArtifice;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.registry.AttributeRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public interface IGunslingerMob {
    Identifier MOB_NERF_MODIFIER = IronsArtifice.id("compose/mob_nerf");

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
    @SubscribeEvent
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
        profile.addModifier(AttributeRegistry.GUN_DAMAGE, MOB_NERF_MODIFIER, -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        int difficultyIndex = mob.level().getDifficulty().getId();
        int spread = 4 - difficultyIndex;
        profile.addModifier(AttributeRegistry.BULLET_SPEED, MOB_NERF_MODIFIER, -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        profile.addModifier(AttributeRegistry.BULLET_SPREAD, MOB_NERF_MODIFIER, spread, AttributeModifier.Operation.ADD_VALUE);
    }
}
