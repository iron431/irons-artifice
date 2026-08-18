package io.redspace.irons_artifice.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * In-progress reload, stored on the gun.
 * <p>
 * {@code progress} and {@code duration} are seconds on the reload animation timeline.
 * Top-off skips unused insert loops by snapping {@code progress} from {@code skipAt} to {@code skipTo}.
 */
public record ReloadState(double progress, double duration, double speed, int roundsToLoad, double skipAt,
                          double skipTo) {
    @Override
    public boolean equals(Object o) {
        // hacky workaround for letting a live tick state component not spam the network -- omit ticking volatiles from hashing/equals
        if (this == o) return true;
        if (!(o instanceof ReloadState that)) return false;
        return Double.compare(this.duration, that.duration) == 0
                && Double.compare(this.speed, that.speed) == 0
                && this.roundsToLoad == that.roundsToLoad
                && Double.compare(this.skipAt, that.skipAt) == 0
                && Double.compare(this.skipTo, that.skipTo) == 0;
    }

    @Override
    public int hashCode() {
        // hacky workaround for letting a live tick state component not spam the network -- omit ticking volatiles from hashing/equals
        int result = Double.hashCode(duration);
        result = 31 * result + Double.hashCode(speed);
        result = 31 * result + roundsToLoad;
        result = 31 * result + Double.hashCode(skipAt);
        result = 31 * result + Double.hashCode(skipTo);
        return result;
    }

    public static final ReloadState EMPTY = new ReloadState(0, 0, 1, 0, 0, 0);

    public static final Codec<ReloadState> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.DOUBLE.fieldOf("progress").forGetter(ReloadState::progress),
            Codec.DOUBLE.fieldOf("duration").forGetter(ReloadState::duration),
            Codec.DOUBLE.fieldOf("speed").forGetter(ReloadState::speed),
            Codec.INT.fieldOf("rounds_to_load").forGetter(ReloadState::roundsToLoad),
            Codec.DOUBLE.optionalFieldOf("skip_at", 0d).forGetter(ReloadState::skipAt),
            Codec.DOUBLE.optionalFieldOf("skip_to", 0d).forGetter(ReloadState::skipTo)
    ).apply(builder, ReloadState::new));

    public static final StreamCodec<ByteBuf, ReloadState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, ReloadState::progress,
            ByteBufCodecs.DOUBLE, ReloadState::duration,
            ByteBufCodecs.DOUBLE, ReloadState::speed,
            ByteBufCodecs.VAR_INT, ReloadState::roundsToLoad,
            ByteBufCodecs.DOUBLE, ReloadState::skipAt,
            ByteBufCodecs.DOUBLE, ReloadState::skipTo,
            ReloadState::new
    );

    public static @Nullable ReloadState get(ItemStack stack) {
        return stack.get(DataComponentRegistry.RELOAD_STATE);
    }

    public static void set(ItemStack stack, ReloadState state) {
        stack.set(DataComponentRegistry.RELOAD_STATE, state);
    }

    public static boolean has(ItemStack stack) {
        return stack.has(DataComponentRegistry.RELOAD_STATE);
    }

    public static void remove(ItemStack stack) {
        stack.remove(DataComponentRegistry.RELOAD_STATE);
    }

    public static ReloadState start(ItemStack stack, int reloadTimeTicks, double speed, int roundsToLoad, @Nullable TopLoadConfig topLoad) {
        double skipAt = 0;
        double skipTo = 0;
        if (topLoad != null && roundsToLoad > 0) {
            skipAt = topLoad.loopStart();
            skipTo = topLoad.resumeFrom(roundsToLoad);
        }
        ReloadState state = new ReloadState(0, reloadTimeTicks / 20.0, speed, roundsToLoad, skipAt, skipTo);
        set(stack, state);
        return state;
    }

    public boolean isFinished() {
        return this.progress >= duration;
    }

    public boolean hasSkip() {
        return skipTo > skipAt;
    }

    public float pitchMultiplier() {
        return (float) ((speed + 2) / 3);
    }

    /**
     * Effective completion percent for reloads and top-loads
     */
    public float percent(float partialTick) {
        double skip = Math.max(0, skipTo - skipAt);
        double effectiveDuration = duration - skip;
        if (effectiveDuration <= 0) {
            return 1f;
        }
        double timeline = applySkipTo(progress + partialTick / 20.0);
        double effectiveTime = hasSkip() && timeline >= skipTo ? timeline - skip : timeline;
        return Mth.clamp((float) (effectiveTime / effectiveDuration), 0f, 1f);
    }

    /**
     * Effective length of this reload, in ticks.
     */
    public int durationTicks() {
        double skip = Math.max(0, skipTo - skipAt);
        double wallSeconds = Math.max(0, (duration - skip) / Math.max(speed, 1.0e-6));
        return Math.max(1, (int) Math.round(wallSeconds * 20.0));
    }

    public ReloadState increment(int ticks) {
        return new ReloadState(progress + ticks * speed / 20.0, duration, speed, roundsToLoad, skipAt, skipTo);
    }

    public ReloadState applySkip() {
        double skipped = applySkipTo(progress);
        return skipped == progress ? this : new ReloadState(skipped, duration, speed, roundsToLoad, skipAt, skipTo);
    }

    private double applySkipTo(double timeline) {
        if (hasSkip() && timeline >= skipAt && timeline < skipTo) {
            return skipTo;
        }
        return timeline;
    }

    /**
     * Advances reload progress and plays due sound cues.
     *
     * @return the completed state if the reload finished this tick, otherwise {@code null}
     */
    public static @Nullable ReloadState tickReload(ItemStack stack, GunItem gun, Entity owner) {
        ReloadState state = get(stack);
        if (state == null) {
            return null;
        }
        double previous = state.progress;
        state = state.increment(1);
        if (!owner.level().isClientSide()) {
            gun.getGun().reloadCues().playCuesBetween(owner, owner.position(), SoundSource.PLAYERS, previous, state.progress, state.pitchMultiplier());
        }
        state = state.applySkip();
        if (state.isFinished()) {
            remove(stack);
            return state;
        }
        set(stack, state);
        return null;
    }
}
