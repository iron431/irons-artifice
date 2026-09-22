package io.redspace.irons_artifice.data;

import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public record MuzzleFlashSettings(
        Set<MuzzleFlashType> types,
        List<Vector3f> tints,
        Set<ParticleBurst> airBursts,
        Set<ParticleBurst> underwaterBursts
) implements Copyable<MuzzleFlashSettings> {
    public static final Vector3f WHITE = new Vector3f(1f, 1f, 1f);
    public static final Vector3f UNTINTED = new Vector3f(-1f, -1f, -1f);
    public static final Supplier<MuzzleFlashSettings> DEFAULT = () -> of(MuzzleFlashType.TRIANGLE, MuzzleFlashType.SMALL_STAR);

    public static MuzzleFlashSettings of(MuzzleFlashType... types) {
        if (types.length == 0) {
            throw new IllegalArgumentException("Nonzero type count required");
        }
        return new MuzzleFlashSettings(
                EnumSet.copyOf(List.of(types)),
                new ArrayList<>(),
                new HashSet<>(),
                new HashSet<>(List.of(ParticleBurst.BUBBLES))
        );
    }

    public void addTint(Vector3f tint) {
        tints.add(tint);
    }

    public void addTint(int tint) {
        addTint(Utils.vector3fFromRGB24(tint));
    }

    public void addAirBurst(ParticleBurst burst) {
        airBursts.add(burst);
    }

    public void addUnderwaterBurst(ParticleBurst burst) {
        underwaterBursts.add(burst);
    }

    public Vector3f pickTint(RandomSource random) {
        if (tints.isEmpty()) {
            return UNTINTED;
        }
        return tints.get(random.nextInt(tints.size()));
    }

    public MuzzleFlashType pick(RandomSource random) {
        if (types.isEmpty()) {
            throw new IllegalStateException("MuzzleFlashSettings has no types to pick from");
        }
        return types.stream().skip(random.nextInt(types.size())).findFirst().orElseThrow();
    }

    public boolean hasVisuals() {
        return !types.isEmpty() || !airBursts.isEmpty() || !underwaterBursts.isEmpty();
    }

    @Override
    public MuzzleFlashSettings copy() {
        return new MuzzleFlashSettings(
                types.isEmpty() ? EnumSet.noneOf(MuzzleFlashType.class) : EnumSet.copyOf(types),
                new ArrayList<>(tints),
                new HashSet<>(airBursts),
                new HashSet<>(underwaterBursts)
        );
    }
}
