package io.redspace.irons_artifice.client;

import io.redspace.irons_artifice.item.kinetic.KineticWeapon;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * How long ago a charging entity last ran something through, which the first-person charge animation eases the
 * weapon down and back up on. Keyed by entity id and fed by
 * {@link io.redspace.irons_artifice.network.packets.ClientboundKineticHitPacket}.
 * <p>
 * The throttle stays on this side of the wire: the server sends one signal per affected tick, and a charge that
 * stays in contact would otherwise machine-gun the sound.
 */
public final class KineticHitFeedback {

    private KineticHitFeedback() {
    }

    private static final long FORGET_AFTER_TICKS = 200L;

    private static final Map<Integer, Long> LAST_HIT_TIME = new HashMap<>();

    /**
     * Records a landed stab and plays the weapon's hit sound, no more often than
     * {@link KineticWeapon#HIT_FEEDBACK_TICKS} apart. The sound comes off the wire because this side may hold a
     * different copy of the stack, or for another player only a synced flag saying it is using something.
     */
    public static void onKineticHit(int attackerId, Optional<Holder<SoundEvent>> hitSound) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        long now = minecraft.level.getGameTime();
        Long last = LAST_HIT_TIME.get(attackerId);
        if (last != null && now - last <= KineticWeapon.HIT_FEEDBACK_TICKS) {
            return;
        }
        LAST_HIT_TIME.put(attackerId, now);
        forgetStaleEntries(now);

        Entity attacker = minecraft.level.getEntity(attackerId);
        if (attacker == null) {
            return;
        }
        hitSound.ifPresent(sound -> minecraft.level.playLocalSound(
                attacker, sound.value(), attacker.getSoundSource(), 1.0F, 1.0F));
    }

    /**
     * Ticks since {@code entity} last landed a stab, or a number past the end of the animation if it never has. The
     * partial tick is added so the ease reads smoothly between ticks.
     */
    public static float ticksSinceHit(Entity entity, float partialTick) {
        Long last = LAST_HIT_TIME.get(entity.getId());
        if (last == null || entity.level() == null) {
            return Float.MAX_VALUE;
        }
        return (float) (entity.level().getGameTime() - last) + partialTick;
    }

    public static void reset() {
        LAST_HIT_TIME.clear();
    }

    private static void forgetStaleEntries(long now) {
        LAST_HIT_TIME.values().removeIf(stamp -> now - stamp > FORGET_AFTER_TICKS);
    }
}
