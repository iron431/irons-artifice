package io.redspace.irons_artifice.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.redspace.irons_artifice.data.FireCycleCueStack;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public record FireDelayState(int progress, int duration, int cueIndex, float pitchMultiplier) {
    @Override
    public boolean equals(Object o) {
        // hacky workaround for letting a live tick state component not spam the network -- omit ticking volatiles from hashing/equals
        if (this == o) return true;
        if (!(o instanceof FireDelayState that)) return false;
        return this.duration == that.duration
                && Float.compare(this.pitchMultiplier, that.pitchMultiplier) == 0;
    }
    @Override
    public int hashCode() {
        // hacky workaround for letting a live tick state component not spam the network -- omit ticking volatiles from hashing/equals
        int result = Integer.hashCode(duration);
        result = 31 * result + Float.hashCode(pitchMultiplier);
        return result;
    }
    public static final FireDelayState EMPTY = new FireDelayState(0, 0, 0, 1);

    public static final Codec<FireDelayState> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("progress").forGetter(FireDelayState::progress),
            Codec.INT.fieldOf("duration").forGetter(FireDelayState::duration),
            Codec.INT.optionalFieldOf("cue_index", 0).forGetter(FireDelayState::cueIndex),
            Codec.FLOAT.optionalFieldOf("pitch_multiplier", 1f).forGetter(FireDelayState::pitchMultiplier)
    ).apply(builder, FireDelayState::new));

    public static final StreamCodec<ByteBuf, FireDelayState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FireDelayState::progress,
            ByteBufCodecs.VAR_INT, FireDelayState::duration,
            ByteBufCodecs.VAR_INT, FireDelayState::cueIndex,
            ByteBufCodecs.FLOAT, FireDelayState::pitchMultiplier,
            FireDelayState::new
    );

    public static @Nullable FireDelayState get(ItemStack stack) {
        return stack.get(DataComponentRegistry.FIRE_DELAY_STATE);
    }

    public static void set(ItemStack stack, FireDelayState state) {
        stack.set(DataComponentRegistry.FIRE_DELAY_STATE, state);
    }

    public static boolean has(ItemStack stack) {
        return stack.has(DataComponentRegistry.FIRE_DELAY_STATE);
    }

    public static boolean isActive(ItemStack stack) {
        return has(stack);
    }

    public static void remove(ItemStack stack) {
        stack.remove(DataComponentRegistry.FIRE_DELAY_STATE);
    }

    public static void start(ItemStack stack, int durationTicks, float pitchMultiplier) {
        set(stack, new FireDelayState(0, Math.max(0, durationTicks), 0, pitchMultiplier));
    }

    public boolean isFinished() {
        return this.progress >= duration;
    }

    public float percent(float partialTick) {
        if (duration <= 0) {
            return 1f;
        }
        return (progress + partialTick) / duration;
    }

    public FireDelayState increment(int ticks) {
        return new FireDelayState(progress + ticks, duration, cueIndex, pitchMultiplier);
    }

    public FireDelayState withCueIndex(int cueIndex) {
        return new FireDelayState(progress, duration, cueIndex, pitchMultiplier);
    }

    /**
     * Advances fire-delay progress and plays due cycle cues.
     *
     * @return whether the delay has completed
     */
    public static boolean tick(ItemStack stack, GunItem gun, Level level, Entity owner) {
        FireDelayState state = get(stack);
        if (state == null) {
            return true;
        }
        state = state.increment(1);

        FireCycleCueStack cues = gun.getGun().fireCycleCues();
        int nextCue = cues.playDueCues(owner, owner.position(), SoundSource.PLAYERS, state.percent(0), state.cueIndex(), state.pitchMultiplier);
        state = state.withCueIndex(nextCue);

        if (state.isFinished()) {
            remove(stack);
            return true;
        }
        set(stack, state);
        return false;
    }
}
