package io.redspace.irons_artifice.item;

import software.bernie.geckolib.GeckoLibConstants;
import io.redspace.irons_artifice.data.FireCycleCueStack;
import io.redspace.irons_artifice.registry.DataAttachmentRegistry;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * @param gunId           GeoId of the gun this cycle belongs to, or {@link #UNKEYED}
 * @param progress        ticks elapsed
 * @param duration        total ticks; zero means no cycle is running
 * @param cueIndex        next fire-cycle cue to play
 * @param pitchMultiplier cue pitch scaling for this gun's fire rate
 */
public record FireDelayState(long gunId, int progress, int duration, int cueIndex, float pitchMultiplier) {

    public static final FireDelayState NONE = new FireDelayState(0L, 0, 0, 0, 1f);

    public static final long UNKEYED = Long.MIN_VALUE;

    public static FireDelayState get(LivingEntity living) {
        return living.getData(DataAttachmentRegistry.FIRE_DELAY.get());
    }

    /**
     * Whether a cycle is running that blocks this particular gun from firing.
     */
    public static boolean isActive(LivingEntity living, ItemStack heldGun) {
        FireDelayState state = get(living);
        if (state.duration <= 0) {
            return false;
        }
        long held = gunIdOf(heldGun);
        if (state.gunId == UNKEYED || held == UNKEYED) {
            return true;
        }
        return state.gunId == held;
    }

    /**
     * Ticks left before the current cycle finishes, or zero if not firing
     */
    public static int remaining(LivingEntity living) {
        FireDelayState state = get(living);
        return state.duration <= 0 ? 0 : Math.max(0, state.duration - state.progress);
    }

    public static void start(LivingEntity living, ItemStack heldGun, int durationTicks, float pitchMultiplier) {
        if (durationTicks <= 0) {
            return;
        }
        living.setData(DataAttachmentRegistry.FIRE_DELAY.get(),
                new FireDelayState(gunIdOf(heldGun), 0, durationTicks, 0, pitchMultiplier));
    }

    public static void clear(LivingEntity living) {
        living.setData(DataAttachmentRegistry.FIRE_DELAY.get(), NONE);
    }

    /**
     * Advances the cycle by one tick and plays any cues crossed.
     *
     * @return whether the cycle has finished
     */
    public static boolean tick(LivingEntity living, GunItem gun, Level level) {
        FireDelayState state = get(living);
        if (state.duration <= 0) {
            return true;
        }
        int progress = state.progress + 1;
        float percent = (float) progress / state.duration;

        FireCycleCueStack cues = gun.getGun().fireCycleCues();
        int nextCue = cues.playDueCues(living, living.position(), SoundSource.PLAYERS, percent,
                state.cueIndex, state.pitchMultiplier);

        if (progress >= state.duration) {
            clear(living);
            return true;
        }
        living.setData(DataAttachmentRegistry.FIRE_DELAY.get(),
                new FireDelayState(state.gunId, progress, state.duration, nextCue, state.pitchMultiplier));
        return false;
    }

    /**
     * reuse geckolib id'ing
     */
    private static long gunIdOf(ItemStack stack) {
        Long id = stack.get(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get());
        return id == null ? UNKEYED : id;
    }
}
